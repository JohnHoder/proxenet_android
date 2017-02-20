package janhodermarsky.proxenet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by john on 18.6.15.
 */
public class RestartServiceReceiver extends BroadcastReceiver
{

    private static final String TAG = "RestartServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive");
        //context.startService(new Intent(context.getApplicationContext(), StickyService.class));

        Intent service = new Intent(context, ProxenetService.class);
        service.putExtra("TASK", "Start service");
        context.startService(service);
    }

}
