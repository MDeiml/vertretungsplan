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
import android.content.SharedPreferences;

public class MainActivity extends Activity {

    private WebView webview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webview = new WebView(this);
        webview.setInitialScale(50);
        setContentView(webview);
        // setContentView(R.layout.main);
        // webview = (WebView)findViewById(R.id.webview);
        update();
    }

    public void update() {
        SharedPreferences pref = getSharedPreferences("com.mdeiml.vertretungsplan.Einstellungen",MODE_PRIVATE);
        int ks = pref.getInt("klassenstufe", 0);
        String klassenstufe = getResources().getStringArray(R.array.klassenstufen)[ks];
        String klassenbuchstabe = pref.getString("klassenbuchstabe", "A");

        try {
            UpdateVertretungsplan task = new UpdateVertretungsplan(webview, this, klassenstufe, klassenbuchstabe);
            task.execute(new URL(getResources().getString(R.string.vp_url)));
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
