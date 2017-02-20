package janhodermarsky.proxenet;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by john on 12.6.15.
 */
public class MyApplication extends Application {

    private static Context context;

    private Thread.UncaughtExceptionHandler mOnRuntimeError;

    public void onCreate(){
        super.onCreate();
        MyApplication.context = getApplicationContext();

        mOnRuntimeError = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.d("ShowboxApplication", "crash caught");
                AlarmManager mgr = (AlarmManager) getSystemService(
                        getApplicationContext().ALARM_SERVICE);

                Log.e("EXCEPTION CAUGHT!", "TRYING TO SCHEDULE RESTART");

                //mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, Interactive.pendingIntent);
                System.exit(2);
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(mOnRuntimeError);

        Log.e("MyApplication", "Lol in here");

    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
