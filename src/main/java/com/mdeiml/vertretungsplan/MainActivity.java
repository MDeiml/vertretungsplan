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
import android.os.AsyncTask;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.util.Log;

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
        int ks = pref.getInt("klassenstufe", -1); // Default: -1 -> Einstellungen aufrufen
        String klassenbuchstabe = pref.getString("klassenbuchstabe", "A");
        if(ks != -1)
            update(ks, klassenbuchstabe); // den Vertretungsplan abrufen
        else
            startActivityForResult(new Intent(this, SettingsActivity.class), 0);
    }

    public void update(int ks, String klassenbuchstabe) {
        new UpdateVertretungsplan(this).execute();
        new LoadVertretungsplan().execute();
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

    private class LoadVertretungsplan extends AsyncTask<Void, Void, Void> {

        private final String[] projection = new String[] {"tag", "stunde", "fach"};

        protected Void doInBackground(Void... v) {
            VertretungenOpenHelper openHelper = new VertretungenOpenHelper(MainActivity.this);
            SQLiteDatabase db = openHelper.getReadableDatabase();

            String selection = "klasse='Q11'";

            Cursor c = db.query(VertretungenOpenHelper.TABLE_NAME, projection, selection, new String[0], null, null, null);
            c.moveToFirst();
            Log.i("MainActivity", c.getString(c.getColumnIndexOrThrow("fach")));

            return null;
        }

    }

}
