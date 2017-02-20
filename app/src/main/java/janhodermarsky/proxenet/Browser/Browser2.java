package janhodermarsky.proxenet.Browser;

/**
 * Created by john on 5.9.14.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import janhodermarsky.proxenet.R;

public class Browser2 extends Fragment implements View.OnClickListener, EditUrlFragment.EditUrlDialogListener{

    public Browser2() {
    }

    String url = "";

    TextView check;
    WebView webView;
    public String completeCheck;
    Button back, forward, copy, refresh, handler;
    Button sdPrefs, sdCache, sdHistory;
    ProgressBar progressbar;
    String stringURL = null;
    TextView tvWebpageName;
    TextView edittextURL;

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

        View rootView = inflater.inflate(R.layout.newbrowser, container, false);

        webView = (WebView) rootView.findViewById(R.id.llwebview);


        //webView.getSettings().setLoadWithOverviewMode(true);
        //webView.getSettings().setUseWideViewPort(true);
        //webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptEnabled(true);
        //webView.getSettings().setLoadsImagesAutomatically(true);
        //webView.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);
        //webView.getSettings().setSavePassword(true);
        //webView.getSettings().setUserAgentString("BreachRays 0.1");

        /*webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressbar.setProgress(progress);

                if (progress == 100) {
                    progressbar.setProgress(100);
                    progressbar.setProgress(0);
                }
            }
        });*/

        tvWebpageName = (TextView)rootView.findViewById(R.id.tvWebpageName);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                // Handle the error
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                stringURL = url;
                edittextURL.setText(stringURL);
                Log.e("ellee", stringURL);
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                tvWebpageName.setText(view.getTitle());
            }
        });


        /*back = (Button) rootView.findViewById(R.id.bBack);
        back.setOnClickListener(this);
        forward = (Button) rootView.findViewById(R.id.bForward);
        forward.setOnClickListener(this);
        copy = (Button) rootView.findViewById(R.id.bCopyUrl);
        copy.setOnClickListener(this);
        refresh = (Button) rootView.findViewById(R.id.bRefreshBrowser);
        refresh.setOnClickListener(this);


        progressbar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        progressbar.setMax(100);*/

        edittextURL = (TextView) rootView.findViewById(R.id.etSearch);
        edittextURL.setFocusable(false);
        edittextURL.setClickable(true);
        edittextURL.setOnClickListener(this);

        webView.loadUrl("http://google.com");

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_refresh:
                webView.loadUrl(stringURL);
                break;
            case R.id.bCopyUrl:
                copy(stringURL);
                Toast.makeText(getActivity(), "URL copied to ClipBoard!", Toast.LENGTH_LONG)
                        .show();
                break;
            case R.id.button_back:
                if (webView.canGoBack() == true)
                    webView.goBack();
                break;
            case R.id.button_forward:
                if (webView.canGoForward() == true)
                    webView.goForward();
                break;
            case R.id.etSearch:
                showEditDialog();
                //showNoticeDialog();
                //Toast.makeText(getActivity(), "lel", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void showEditDialog() {
        url = edittextURL.getText().toString();
        FragmentManager fm = getChildFragmentManager();
        EditUrlFragment dialog = EditUrlFragment.newInstance(url);
        dialog.setTargetFragment(this, 0);
        dialog.show(fm, "fragment_edit_url");
    }

    @Override
    public void onFinishEditDialog(String inputText) {
        edittextURL.setText(inputText);
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

    String res = null;

    public String makeRequestMethod(String url) throws Exception {
        res = null;

        Ion.with(this)
                .load(url)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        String res = null;

                        // print the response code, ie, 200GET_DBNAME
                        //System.out.println(result.getHeaders().getResponseCode());
                        // print the String that was downloaded
                        //System.out.println(result.getResult());
                        //Logger.e(this, result.getResult());

                        res = result.getResult();
                        Log.e("RESULT LENGTH --- ", "" + res.length());
                    }
                }).get();
        return res;
}

    private class sendReqest extends AsyncTask<String, Integer, String> {
        String result = null;

        @Override
        protected String doInBackground(String... URL) {
            try {
                result = makeRequestMethod(URL[0]);
            } catch (Exception e) {
            }
            return result;
        }
        }

    }