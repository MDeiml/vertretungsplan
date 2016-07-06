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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.util.Log;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import java.util.ArrayList;
import android.app.PendingIntent;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ListView list;
    private SwipeRefreshLayout refresh;
    private Drawable entfaellt_ic;
    private Drawable raumaenderung_ic;
    private Drawable vertreten_ic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        entfaellt_ic = getResources().getDrawable(R.drawable.ic_event_busy_black_24dp);
        vertreten_ic = getResources().getDrawable(R.drawable.ic_sync_black_24dp);
        raumaenderung_ic = getResources().getDrawable(R.drawable.ic_swap_horiz_black_24dp);

        list = (ListView)findViewById(R.id.vertretungen_list);
        refresh = (SwipeRefreshLayout)findViewById(R.id.refresh);
        refresh.setColorScheme(R.color.ColorPrimary);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update();
            }
        });

        SharedPreferences pref = getSharedPreferences("com.mdeiml.vertretungsplan.Einstellungen",MODE_PRIVATE); // Einstellungen laden
        int ks = pref.getInt("klassenstufe", -1); // Default: -1 -> Einstellungen aufrufen
        if(ks != -1)
            update(); // den Vertretungsplan abrufen
        else
            startActivityForResult(new Intent(this, SettingsActivity.class), 1);
    }

    public void update() {
        new LoadVertretungen().execute();
        Log.i("MainActivity", "refresh");
        refresh.post(new Runnable() {
            @Override
            public void run() {
                refresh.setRefreshing(true);
            }
        });
        PendingIntent pendingIntent = createPendingResult(0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Intent intent = NotificationService.createStopIntent(this);
        intent.putExtra("callback", pendingIntent);
        startService(intent);
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
            case R.id.refresh_menu:
                update();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Bei neuen Einstellungen
        list.setAdapter(null);
        if(requestCode == 0) {
            int ne = data.getIntExtra("newEntries", 0);
            if(ne == -1) {
                Toast.makeText(this, "Vertretungen konnten nicht geladen werden", Toast.LENGTH_LONG).show();
            }else if(ne == -2) {
                Toast.makeText(this, "Passwort nicht korrekt! Bitte richtiges Passwort in den Einstellungen festlegen", Toast.LENGTH_LONG).show();
            }
            new LoadVertretungen().execute(); // Vertretungsplan aktualisieren
            Log.i("MainActivity", "refresh done");
            refresh.post(new Runnable() {
                @Override
                public void run() {
                    refresh.setRefreshing(false);
                }
            });
        }else if(requestCode == 1)
            startActivityForResult(new Intent(this, SettingsActivity.class), 2);
        else {
            update();
            // NotificationEventReceiver.setupAlarm(this, 15);
        }
    }

    private class LoadVertretungen extends AsyncTask<Void, Void, Cursor> {

        public Cursor doInBackground(Void... v) {
            try {
                SharedPreferences pref = getSharedPreferences("com.mdeiml.vertretungsplan.Einstellungen",MODE_PRIVATE); // Einstellungen laden
                String lehrer = pref.getString("lehrer", "");
                String vlehrer = pref.getString("lehrer_full", "");

                VertretungenOpenHelper openHelper = new VertretungenOpenHelper(MainActivity.this);
                SQLiteDatabase db = openHelper.getReadableDatabase();
                String[] projection = new String[] {"_id", "tag", "klasse", "stunde", "lehrer", "vlehrer", "vfach", "raum", "bemerkung"};
                String selection = "klasse == 'all' OR (lehrer == '"+lehrer+"' OR vlehrer == '"+lehrer+"')";
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
            super(MainActivity.this, cursor, false);
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
            ImageView icon = (ImageView)view.findViewById(R.id.vertretung_icon);

            int stunde = cursor.getInt(cursor.getColumnIndexOrThrow("stunde"));
            String lehrer = cursor.getString(cursor.getColumnIndexOrThrow("lehrer"));
            String vfach = cursor.getString(cursor.getColumnIndexOrThrow("vfach"));
            String vlehrer = cursor.getString(cursor.getColumnIndexOrThrow("vlehrer"));
            String raum = cursor.getString(cursor.getColumnIndexOrThrow("raum"));
            String bemerkung = cursor.getString(cursor.getColumnIndexOrThrow("bemerkung")).replaceAll("§", "\n");
            String klasse = cursor.getString(cursor.getColumnIndexOrThrow("klasse"));

            LinearLayout pane = (LinearLayout)view.findViewById(R.id.vertretung_pane);

            if(stunde == 0) {
                stundeV.setText(vlehrer);
                klasseV.setText("");
            }else {
                stundeV.setText((stunde > 0 ? stunde+". Stunde ":"")+lehrer);
                klasseV.setText(klasse);
            }

            if(stunde == 0) {
                pane.setBackgroundResource(R.color.Tag);
                icon.setImageDrawable(null);
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)icon.getLayoutParams();
                lp.leftMargin = 0;
                icon.setLayoutParams(lp);
            }else if(vlehrer.trim().equals("entfällt")) {
                pane.setBackgroundResource(R.color.Entfaellt);
                icon.setImageDrawable(entfaellt_ic);
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)icon.getLayoutParams();
                lp.leftMargin = (int)(5 * getResources().getDisplayMetrics().density);
                icon.setLayoutParams(lp);
            }else if(bemerkung.equals("Raumänderung")) {
                pane.setBackgroundResource(R.color.Raumaenderung);
                icon.setImageDrawable(raumaenderung_ic);
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)icon.getLayoutParams();
                lp.leftMargin = (int)(5 * getResources().getDisplayMetrics().density);
                icon.setLayoutParams(lp);
            }else {
                pane.setBackgroundResource(R.color.Vertreten);
                icon.setImageDrawable(vertreten_ic);
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)icon.getLayoutParams();
                lp.leftMargin = (int)(5 * getResources().getDisplayMetrics().density);
                icon.setLayoutParams(lp);
            }

            ArrayList<String> vertretungListe = new ArrayList<>();

            if(vlehrer.length() > 0) {
                vertretungListe.add(vlehrer);
            }

            if(vfach.length() > 0) {
                vertretungListe.add(vfach);
            }

            if(raum.length() > 0) {
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

            if(bemerkung.length() == 0) {
                bemerkungV.setVisibility(View.GONE);
            }else {
                bemerkungV.setText(bemerkung);
                bemerkungV.setVisibility(View.VISIBLE);
            }
        }
    }

}
