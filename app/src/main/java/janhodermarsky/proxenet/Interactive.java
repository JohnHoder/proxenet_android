package janhodermarsky.proxenet;

/**
 * Created by john on 3.6.15.
 */

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;
import janhodermarsky.proxenet.Browser.ProxyHelper;

public class Interactive extends Activity {

    static Interactive mainContext;

    public static TextView outputText;

    private static Shell.Interactive rootSession;

    private void updateResultStatus(boolean suAvailable, List<String> suResult) {
        StringBuilder sb = (new StringBuilder()).
                append("Root available? ").append(suAvailable ? "Yes" : "No").append((char) 10).
                append((char) 10);
        if (suResult != null) {
            for (String line : suResult) {
                sb.append(line).append((char) 10);
            }
        }
        outputText.setText(sb.toString());
    }

    private void appendLineToOutput(String line) {
        StringBuilder sb = (new StringBuilder()).
                append(line).
                append((char) 10);
        outputText.append(sb.toString());
    }

    private void reportError(String error) {
        List<String> errorInfo = new ArrayList<String>();
        errorInfo.add(error);
        updateResultStatus(false, errorInfo);
        rootSession = null;
    }

    private void sendRootCommand() {

        String proxenetPath = this.getApplicationInfo().dataDir;

        rootSession.addCommand(new String[]{"chmod 775 " + proxenetPath + "/proxenet"}, 0,
                new Shell.OnCommandResultListener() {
                    public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                        if (exitCode < 0) {
                            reportError("Error executing commands: exitCode " + exitCode);
                        } else {
                            updateResultStatus(true, output);
                            appendLineToOutput("----------");
                        }
                    }
                });

        rootSession.addCommand(new String[]{"id"}, 0,
                new Shell.OnCommandResultListener() {
                    public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                        if (exitCode < 0) {
                            reportError("Error executing commands: exitCode " + exitCode);
                        } else {
                            updateResultStatus(true, output);
                            appendLineToOutput("----------");
                        }
                    }
                });


        rootSession.addCommand(new String[]{"cd " + proxenetPath + " && ./proxenet"}, 1, new Shell.OnCommandLineListener() {

            @Override
            public void onCommandResult(int i, int i1) {
                Toast.makeText(getApplication(), "Proxenet exited", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLine(String line) {
                appendLineToOutput(line);
                Log.e("OUT", line);
            }

        });
    }

    private void openRootShell() {
        if (rootSession != null) {
            sendRootCommand();
        } else {
            // We're creating a progress dialog here because we want the user to wait.
            // If in your app your user can just continue on with clicking other things,
            // don't do the dialog thing.
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("Please wait");
            dialog.setMessage("Requesting root privilege...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();

            // start the shell in the background and keep it alive as long as the app is running
            rootSession = new Shell.Builder().
                    useSU().
                    setWantSTDERR(true).
                    setWatchdogTimeout(0).
                    setMinimalLogging(true).
                    open(new Shell.OnCommandResultListener() {

                        // Callback to report whether the shell was successfully started up
                        @Override
                        public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                            // note: this will FC if you rotate the phone while the dialog is up
                            dialog.dismiss();

                            if (exitCode != Shell.OnCommandResultListener.SHELL_RUNNING) {
                                reportError("Error opening root shell: exitCode " + exitCode);
                            } else {
                                // Shell is up: send our first request
                                sendRootCommand();
                            }
                        }
                    });
        }
    }

    private void copyFromRaw(int resource, String destinationWithFilename) {
        InputStream in = getResources().openRawResource(resource);
        try {
            FileOutputStream out = new FileOutputStream(destinationWithFilename);

            byte[] buff = new byte[1024];
            int read = 0;

            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            in.close();
            out.close();
        } catch (Exception ee) {
        }
        Log.d("TAG", "Copy success: ");
    }

    private void copyFromLib(File src, File dst) {
        InputStream in = null;
        try {
            in = new FileInputStream(src);

            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSymlink(File src, File dst){
        // do some dirty reflection to create the symbolic link
        try {
            final Class<?> libcore = Class.forName("libcore.io.Libcore");
            final Field fOs = libcore.getDeclaredField("os");
            fOs.setAccessible(true);
            final Object os = fOs.get(null);
            final Method method = os.getClass().getMethod("symlink", String.class, String.class);
            method.invoke(os, src.getAbsolutePath(), dst.getAbsolutePath());
        } catch (Exception e) {
            // TODO handle the exception
        }
    }

    //COPY FOR LIBRARY
    private void setupProxenet() {

        String pth = this.getApplicationInfo().dataDir;

        //copy binary
        //String executableFilePath = pth + "/proxenet";
        //copyFromRaw(R.raw.proxenet, executableFilePath);

        //create plugins dirs
        File plugins = new File(pth + "/proxenet-plugins");
        if (!plugins.exists()) {
            if (plugins.mkdir()) ; //directory is created;
        }
        File autoload = new File(pth + "/proxenet-plugins" + "/autoload");
        if (!autoload.exists()) {
            if (autoload.mkdir()) ; //directory is created;
        }

        //copy addon
        File addonPathSource = new File(pth + "/lib/libaddon.so");
        File addonPathDestination = new File(pth + "/proxenet-plugins/libaddon.so");
        copyFromLib(addonPathSource, addonPathDestination);
        //create symlink
        File addonPathSymlink = new File(pth + "/proxenet-plugins/autoload/libaddon.so");
        createSymlink(addonPathDestination, addonPathSymlink);

        //create keys folder
        File keys = new File(pth + "/keys");
        if (!keys.exists()) {
            if (keys.mkdir()) ; //directory is created;
        }
        File certs = new File(pth + "/keys/certs");
        if (!certs.exists()) {
            if (certs.mkdir()) ; //directory is created;
        }

        //copyCertFiles
        String certFilePath = pth + "/keys/proxenet.crt";
        copyFromRaw(R.raw.crtfile, certFilePath);
        String keyFilePath = pth + "/keys/proxenet.key";
        copyFromRaw(R.raw.keyfile, keyFilePath);

        //copy generic.key
        String genericKeyPath = pth + "/keys/certs/generic.key";
        copyFromRaw(R.raw.generickey, genericKeyPath);

        //copyLibProxenet
        //String libProxenetPath = pth + "/lib/libproxenet.so";
        //copyFromRaw(R.raw.libproxenet, libProxenetPath);
    }

    //SETUP EXECUTABLE
    /*private void setupProxenet() {

        String pth = this.getApplicationInfo().dataDir;

        //copy binary
        String executableFilePath = pth + "/proxenet";
        copyFromRaw(R.raw.proxenet, executableFilePath);

        //create plugins dirs
        File plugins = new File(pth + "/proxenet-plugins");
        if (!plugins.exists()) {
            if (plugins.mkdir()) ; //directory is created;
        }
        File autoload = new File(pth + "/proxenet-plugins" + "/autoload");
        if (!autoload.exists()) {
            if (autoload.mkdir()) ; //directory is created;
        }

        //copy addon
        File addonPathSource = new File(pth + "/lib/libaddon.so");
        File addonPathDestination = new File(pth + "/proxenet-plugins/libaddon.so");
        copyFromLib(addonPathSource, addonPathDestination);
        //create symlink
        File addonPathSymlink = new File(pth + "/proxenet-plugins/autoload/libaddon.so");
        createSymlink(addonPathDestination, addonPathSymlink);

        //create keys folder
        File keys = new File(pth + "/keys");
        if (!keys.exists()) {
            if (keys.mkdir()) ; //directory is created;
        }
        File certs = new File(pth + "/keys/certs");
        if (!certs.exists()) {
            if (certs.mkdir()) ; //directory is created;
        }

        //copyCertFiles
        String certFilePath = pth + "/keys/proxenet.crt";
        copyFromRaw(R.raw.crtfile, certFilePath);
        String keyFilePath = pth + "/keys/proxenet.key";
        copyFromRaw(R.raw.keyfile, keyFilePath);

        //copyLibProxenet
        //String libProxenetPath = pth + "/lib/libproxenet.so";
        //copyFromRaw(R.raw.libproxenet, libProxenetPath);
    }*/

    private class StartProxenet extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            //NativeWrapper.test();
            //Toast.makeText(getApplication(), fireProxenet() + "", Toast.LENGTH_SHORT).show();
            //fireProxenet();

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    public void StartProxenetService(Context c){
        Intent service = new Intent(c, ProxenetService.class);
        service.putExtra("TASK", "Start service");
        c.startService(service);
    }



    public static Context getAppContext(){
        try {
            final Class<?> activityThreadClass =
                    Class.forName("android.app.ActivityThread");
            final Method method = activityThreadClass.getMethod("currentApplication");
            return (Application) method.invoke(null, (Object[]) null);
        } catch (final ClassNotFoundException e) {
            // handle exception
        } catch (final NoSuchMethodException e) {
            // handle exception
        } catch (final IllegalArgumentException e) {
            // handle exception
        } catch (final IllegalAccessException e) {
            // handle exception
        } catch (final InvocationTargetException e) {
            // handle exception
        }
        return null;
    }

    //PendingIntent _pendingInt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interactive);

        mainContext=this;

        setupProxenet();


        Intent intent = new Intent(getAppContext(), DummyRestartServiceActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getAppContext(), 12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        //this._pendingInt=PendingIntent.getActivity(getAppContext(), 0, new Intent(getIntent()), getIntent().getFlags());
        // start handler which starts pending-intent after Application-Crash
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(pendingIntent, this.getApplicationContext()));


        /*initJNICallback();
        stringFromJNI();
        fireProxenet();*/

        //NativeWrapper.initJNICallback();
        //NativeWrapper.stringFromJNI();

        //new StartProxenet().execute();

        StartProxenetService(this);

        //////////////////////////////////////////////

        //ProxyHelper.setLPreViewWebViewProxy(this, "127.0.0.1", 8008);


        outputText = (TextView) findViewById(R.id.text);

        // mode switch button
        Button button = (Button) findViewById(R.id.switch_button);
        button.setText("disable_interactive_mode");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), MainActivity.class));
                finish();
            }
        });

        Button bKill = (Button) findViewById(R.id.kill_button);
        bKill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Process su = Runtime.getRuntime().exec("su pkill proxenet");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // refresh button
        ((Button) findViewById(R.id.refresh_button)).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openRootShell();
                    }
                });

        //openRootShell();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent("YouWillNeverKillMe"));
        Log.d("INTERACTIVE", "Service Killed");
    }
}
