package janhodermarsky.proxenet;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import janhodermarsky.proxenet.Browser.Browser;
import janhodermarsky.proxenet.Browser.ProxyHelper;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProxyHelper.setLPreViewWebViewProxy(this, "127.0.0.1", 8008);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.root_container, new Browser())
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return;
        }
        super.onBackPressed();
    }


}
