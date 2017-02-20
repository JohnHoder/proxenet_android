package janhodermarsky.proxenet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

/**
 * Created by john on 19.6.15.
 */
public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    private PendingIntent _penIntent;
    Context cont;

    public CustomExceptionHandler(PendingIntent intent, Context cont) {
        this._penIntent = intent;
        this.cont=cont;
    }

    public void uncaughtException(Thread t, Throwable e) {
        Log.e("EXCEPTION CAUGHT!", "TRYING TO SCHEDULE RESTART --- CustomExceptionHandler");
        AlarmManager mgr = (AlarmManager) cont.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, this._penIntent);
        System.exit(2);
    }
}
