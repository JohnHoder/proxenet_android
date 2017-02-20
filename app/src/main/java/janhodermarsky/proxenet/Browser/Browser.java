package janhodermarsky.proxenet.Browser;

/**
 * Created by john on 5.9.14.
 */

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import janhodermarsky.proxenet.R;

public class Browser extends Fragment implements View.OnClickListener, EditUrlFragment.EditUrlDialogListener {

    public Browser() {
    }

    String url = "";

    TextView check;
    WebView webView;
    public String completeCheck;
    Button back, forward, copy, refresh, handler;
    Button sdPrefs, sdCache, sdHistory;
    ProgressBar progressbar;
    String stringURL = null;
    TextView tvURL;

    @Override
    public void onDestroyView() {
        //ActionBar actionBar;
        //actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        //actionBar.show();
        super.onDestroyView();
    }

    public void goBack() {
        if(this.webView != null) {
            if(this.webView.canGoBack()) {
                this.webView.goBack();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //ActionBar actionBar;
        //actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        //actionBar.hide();

        View rootView = inflater.inflate(R.layout.webview, container, false);

        webView = (WebView) rootView.findViewById(R.id.webView);

        //webView.getSettings().setLoadWithOverviewMode(true);
        //webView.getSettings().setUseWideViewPort(true);
        //webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptEnabled(true);
        //webView.getSettings().setLoadsImagesAutomatically(true);
        //webView.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);
        //webView.getSettings().setSavePassword(true);
        //webView.getSettings().setUserAgentString("BreachRays 0.1");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressbar.setProgress(progress);

                if (progress == 100) {
                    progressbar.setProgress(100);
                    progressbar.setProgress(0);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                // Handle the error
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                stringURL = url;
                tvURL.setText(stringURL);
                Log.e("ellee", stringURL);
                view.loadUrl(url);
                return true;
            }
        });


        back = (Button) rootView.findViewById(R.id.bBack);
        back.setOnClickListener(this);
        forward = (Button) rootView.findViewById(R.id.bForward);
        forward.setOnClickListener(this);
        copy = (Button) rootView.findViewById(R.id.bCopyUrl);
        copy.setOnClickListener(this);
        refresh = (Button) rootView.findViewById(R.id.bRefreshBrowser);
        refresh.setOnClickListener(this);


        progressbar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        progressbar.setMax(100);

        tvURL = (TextView) rootView.findViewById(R.id.etURL);
        tvURL.setOnClickListener(this);

        webView.loadUrl("http://google.com");

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bRefreshBrowser:
                webView.loadUrl(stringURL);
                break;
            case R.id.bCopyUrl:
                copy(stringURL);
                Toast.makeText(getActivity(), "URL copied to ClipBoard!", Toast.LENGTH_LONG)
                        .show();
                break;
            case R.id.bBack:
                if (webView.canGoBack() == true)
                    webView.goBack();
                break;
            case R.id.bForward:
                if (webView.canGoForward() == true)
                    webView.goForward();
                break;
            case R.id.etURL:
                showEditDialog();
                //showNoticeDialog();
                //Toast.makeText(getActivity(), "lel", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void showEditDialog() {
        url = tvURL.getText().toString();
        FragmentManager fm = getChildFragmentManager();
        EditUrlFragment dialog = EditUrlFragment.newInstance(url);
        dialog.setTargetFragment(this, 0);
        dialog.show(fm, "fragment_edit_url");
    }

    @Override
    public void onFinishEditDialog(String inputText) {
        tvURL.setText(inputText);
        webView.loadUrl(inputText);
    }

    private void copy(String s) {
        // int sdk = android.os.Build.VERSION.SDK_INT;
        // if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
        ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(s);
        // } else {
        // android.content.ClipboardManager clipboard =
        // (android.content.ClipboardManager)
        // getSystemService(Context.CLIPBOARD_SERVICE);
        // android.content.ClipData clip =
        // android.content.ClipData.newPlainText("text label","text to clip");
        // clipboard.setPrimaryClip(clip);
        // }

    }
}