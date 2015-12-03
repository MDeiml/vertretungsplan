package com.mdeiml.vertretungsplan;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import java.net.URL;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webview = new WebView(this);
        webview.setInitialScale(50);
        setContentView(webview);
        // setContentView(R.layout.main);
        // WebView webview = (WebView)findViewById(R.id.webview);
        try {
            new UpdateVertretungsplan(webview, this).execute(new URL(getResources().getString(R.string.vp_url)));
        }catch(Exception e) {}
    }

}
