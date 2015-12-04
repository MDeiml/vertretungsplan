package com.mdeiml.vertretungsplan;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import java.net.URL;
import java.net.MalformedURLException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Intent;

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
        }catch(MalformedURLException e) {}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
