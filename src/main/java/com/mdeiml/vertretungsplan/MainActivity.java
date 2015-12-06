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

    private WebView webview; // WebView, das den Vertretungsplan anzeigt

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webview = new WebView(this); // manuelle Erzeugung des Layouts
        webview.setInitialScale(50); // Breite des Vertretungsplan entspricht Bilschirmbreite
        webview.getSettings().setBuiltInZoomControls(true);
        setContentView(webview);
        // setContentView(R.layout.main);
        // webview = (WebView)findViewById(R.id.webview);
        SharedPreferences pref = getSharedPreferences("com.mdeiml.vertretungsplan.Einstellungen",MODE_PRIVATE); // Einstellungen laden
        int ks = pref.getInt("klassenstufe", 0); // Default: 5A
        String klassenbuchstabe = pref.getString("klassenbuchstabe", "A");
        update(ks, klassenbuchstabe); // den Vertretungsplan abrufen
    }

    public void update(int ks, String klassenbuchstabe) {
        String klassenstufe = getResources().getStringArray(R.array.klassenstufen)[ks]; // Klassenstufe in String umwandeln(0 = "5")
        try {
            UpdateVertretungsplan task = new UpdateVertretungsplan(webview, this, klassenstufe, klassenbuchstabe);
            task.execute(new URL(getResources().getString(R.string.vp_url))); // Vertretungsplan im Hintergrund laden
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
                startActivityForResult(intent,0); // Einstellungen aufrufen
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Bei neuen Einstellungen
        int ks = data.getIntExtra("klassenstufe", 0);
        String kb = data.getStringExtra("klassenbuchstabe");
        webview.loadUrl("about:blank"); // leere Seite
        update(ks, kb); // Vertretungsplan aktualisieren
    }

}
