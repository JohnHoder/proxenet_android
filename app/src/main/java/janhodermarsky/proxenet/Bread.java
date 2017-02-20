package janhodermarsky.proxenet;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by john on 8.6.15.
 */

public class Bread {
    public static String makeText(final Context context, final byte[] array) { //CharSequence text

        /*((Activity)context).runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });*/

        Log.e("BREAD JAVA", array.toString());

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                //Toast.makeText(Interactive.mainContext, text, Toast.LENGTH_SHORT).show();

                try {
                    String decoded = new String(array, "UTF-8");
                    byte[] data = Base64.decode(decoded, Base64.DEFAULT);

                    String text = new String(data, "UTF-8");
                    Interactive.outputText.append(text + "\n");
                    Log.e("BREAD JAVA", text);
                } catch (UnsupportedEncodingException e) {
                    Log.e("BREAD ERROR", "UTF-8 PROBLEM!");
                    e.printStackTrace();
                }

                // scroll to the bottom of the log display
                final ScrollView scroll = ((ScrollView)(Interactive.outputText.getParent()).getParent());
                scroll.post(new Runnable() {
                    @Override
                    public void run() {
                        scroll.fullScroll(View.FOCUS_DOWN);
                    }
                });
                //String s = new String(array);
                //Interactive.outputText.append(text);

                /*NiftyDialogBuilder dialogBuilder=NiftyDialogBuilder.getInstance(Interactive.mainContext);

                dialogBuilder
                        .withTitle("Proxenet")
                        .withMessage(text)
                        .show();*/
            }
        });

        return null;
    }

    public static String makeText(final Context context, final CharSequence text) { //CharSequence text

        /*((Activity)context).runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });*/

        Log.e("BREAD JAVA", text.toString());

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                //Toast.makeText(Interactive.mainContext, text, Toast.LENGTH_SHORT).show();

                try {
                    byte[] data = Base64.decode(text.toString(), Base64.DEFAULT);
                    String text = new String(data, "UTF-8");
                    Interactive.outputText.append(text + "\n");
                    Log.e("BREAD JAVA", text);
                } catch (UnsupportedEncodingException e) {
                    Log.e("BREAD ERROR", "UTF-8 PROBLEM!");
                    e.printStackTrace();
                }

                // scroll to the bottom of the log display
                final ScrollView scroll = ((ScrollView)(Interactive.outputText.getParent()).getParent());
                scroll.post(new Runnable() {
                    @Override
                    public void run() {
                        scroll.fullScroll(View.FOCUS_DOWN);
                    }
                });
                //String s = new String(array);
                //Interactive.outputText.append(text);

                /*NiftyDialogBuilder dialogBuilder=NiftyDialogBuilder.getInstance(Interactive.mainContext);

                dialogBuilder
                        .withTitle("Proxenet")
                        .withMessage(text)
                        .show();*/
            }
        });

        return null;
    }
}
