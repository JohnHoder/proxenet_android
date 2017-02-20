package janhodermarsky.proxenet;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by john on 19.6.15.
 */
public class DummyRestartServiceActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("DummyAct", "Lol in here");
        //Interactive.pendingIntent = PendingIntent.getActivity(                getBaseContext(), 0,                getIntent(),                PendingIntent.FLAG_CANCEL_CURRENT); //getIntent().getFlags()
        startService(new Intent(this, ProxenetService.class));
        finish();
    }
}
