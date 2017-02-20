package janhodermarsky.proxenet;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.util.Iterator;

/**
 * Created by john on 11.6.15.
 */
public class ProxenetService extends Service {

    public native void initJNICallback();
    public native void stringFromJNI();
    public native void fireProxenet();
    public native void displayToast(CharSequence s);
    public native void stringFromJNI(String s);

    static {
        System.loadLibrary("proxenet");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent("YouWillNeverKillMe"));
        Log.d("SERVICE", "Service Killed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("service", "working");

        if (intent != null) {
            if (intent.hasExtra("TASK")) {
                String target = (String) intent.getExtras().get("TASK");

                new Thread(new Runnable() {
                    public void run(){
                        initJNICallback();
                        //stringFromJNI();
                        fireProxenet();
                        //CharSequence s = "test";
                        //displayToast(s);
                        //stringFromJNI("test");
                    }
                }).start();
            }
        }
        //return START_STICKY;
        return START_NOT_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

}
