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
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        list = (ListView)findViewById(R.id.vertretungen_list);

        SharedPreferences pref = getSharedPreferences("com.mdeiml.vertretungsplan.Einstellungen",MODE_PRIVATE); // Einstellungen laden
        int ks = pref.getInt("klassenstufe", -1); // Default: -1 -> Einstellungen aufrufen
        if(ks != -1)
            update(); // den Vertretungsplan abrufen
        else
            startActivityForResult(new Intent(this, SettingsActivity.class), 0);
    }

    public void update() {
        new UpdateVertretungsplan(this, new LoadVertretungen()).execute();
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
        list.setAdapter(null);
        new LoadVertretungen().execute(); // Vertretungsplan aktualisieren
    }

    private class LoadVertretungen extends AsyncTask<Void, Void, Cursor> {

        public Cursor doInBackground(Void... v) {
            try {
                SharedPreferences pref = getSharedPreferences("com.mdeiml.vertretungsplan.Einstellungen",MODE_PRIVATE); // Einstellungen laden
                int ksI = pref.getInt("klassenstufe", 0); //Default: 5. Klasse
                String ks = getResources().getStringArray(R.array.klassenstufen)[ksI];
                String kb = pref.getString("klassenbuchstabe", "A");

                VertretungenOpenHelper openHelper = new VertretungenOpenHelper(MainActivity.this);
                SQLiteDatabase db = openHelper.getReadableDatabase();
                String[] projection = new String[] {"_id", "tag", "klasse", "stunde", "fach", "lehrer", "vlehrer", "vfach", "raum", "bemerkung"};
                String selection = "klasse == 'all' OR (klasse LIKE '"+ks+"%' AND klasse LIKE '%"+kb+"%')";
                String orderBy = "date(tag), stunde";
                Cursor cursor = db.query(VertretungenOpenHelper.TABLE_NAME, projection, selection, null, null, null, orderBy, null);
                Log.i("MainActivity", cursor.getCount()+" Vertretungen geladen");
                return cursor;
            }catch(Exception e) {
                Log.e("MainActivity", "", e);
            }
            return null;
        }

        public void onPostExecute(Cursor cursor) {
            if(cursor == null)
                return;
            VertretungenAdapter adapter = new VertretungenAdapter(cursor);
            list.setAdapter(adapter);
        }

    }

    private class VertretungenAdapter extends CursorAdapter {

        public VertretungenAdapter(Cursor cursor) {
            super(MainActivity.this, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.vertretung_list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView stundeV = (TextView)view.findViewById(R.id.vertretung_stunde);
            TextView klasseV = (TextView)view.findViewById(R.id.vertretung_klasse);
            TextView vertretungV = (TextView)view.findViewById(R.id.vertretung);
            TextView bemerkungV = (TextView)view.findViewById(R.id.vertretung_bemerkung);

            int stunde = cursor.getInt(cursor.getColumnIndexOrThrow("stunde"));
            String lehrer = cursor.getString(cursor.getColumnIndexOrThrow("lehrer"));
            String fach = cursor.getString(cursor.getColumnIndexOrThrow("fach"));
            String vfach = cursor.getString(cursor.getColumnIndexOrThrow("vfach"));
            String vlehrer = cursor.getString(cursor.getColumnIndexOrThrow("vlehrer"));
            String raum = cursor.getString(cursor.getColumnIndexOrThrow("raum"));
            String bemerkung = cursor.getString(cursor.getColumnIndexOrThrow("bemerkung")).replaceAll("§", "\n");
            String klasse = cursor.getString(cursor.getColumnIndexOrThrow("klasse"));

            LinearLayout pane = (LinearLayout)view.findViewById(R.id.vertretung_pane);

            if(stunde == 0) {
                stundeV.setText(fach);
                klasseV.setText("");
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)pane.getLayoutParams();
                lp.leftMargin = 0;
                pane.setLayoutParams(lp);
            }else {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)pane.getLayoutParams();
                lp.leftMargin = (int)(20 * getResources().getDisplayMetrics().density);
                pane.setLayoutParams(lp);
                stundeV.setText(stunde+". Stunde ("+lehrer+" / "+fach+")");
                klasseV.setText(klasse);
            }

            if(stunde == 0) {
                pane.setBackgroundResource(R.color.Tag);
            }else if(vlehrer.trim().equals("entfällt")) {
                pane.setBackgroundResource(R.color.Entfaellt);
            }else if(bemerkung.equals("Raumänderung")) {
                pane.setBackgroundResource(R.color.Raumaenderung);
            }else {
                pane.setBackgroundResource(R.color.Vertreten);
            }

            Log.i("MainActivity", stunde+", "+fach);

            ArrayList<String> vertretungListe = new ArrayList<>();

            if(!vlehrer.isEmpty()) {
                vertretungListe.add(vlehrer);
            }

            if(!vfach.isEmpty()) {
                vertretungListe.add(vfach);
            }

            if(!raum.isEmpty()) {
                vertretungListe.add(raum);
            }

            if(vertretungListe.isEmpty()) {
                vertretungV.setVisibility(View.GONE);
            }else {
                vertretungV.setVisibility(View.VISIBLE);
                String s = "";
                for(int i = 0; i < vertretungListe.size(); i++) {
                    s = s + vertretungListe.get(i);
                    if(i < vertretungListe.size()-1)
                        s = s + " / ";
                }
                vertretungV.setText(s);
            }

            if(bemerkung.isEmpty()) {
                bemerkungV.setVisibility(View.GONE);
            }else {
                bemerkungV.setText(bemerkung);
                bemerkungV.setVisibility(View.VISIBLE);
            }
        }
    }

}
