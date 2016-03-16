package com.mdeiml.vertretungsplan;

import android.widget.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import java.io.UnsupportedEncodingException;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences pref; // Einstellungen
    private EditText lehrer;
    private EditText vlehrer;
    // private CheckBox notifications;
    private EditText benutzername;
    private EditText passwort;
    private EditText url; 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        Toolbar toolbar = (Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        pref = getSharedPreferences("com.mdeiml.vertretungsplan.Einstellungen", MODE_PRIVATE); // alte Einstellungen laden
        String vlehrerS = pref.getString("lehrer_full", ""); // Default: 5A
        String lehrerS = pref.getString("lehrer", "");
        boolean notificationsB = pref.getBoolean("notifications", true);
        String urlS = pref.getString("url", getResources().getString(R.string.vp_url));

        lehrer = (EditText)findViewById(R.id.lehrer);
        lehrer.setText(lehrerS);

        vlehrer = (EditText)findViewById(R.id.lehrer_full);
        vlehrer.setText(vlehrerS);
        
        /* notifications = (CheckBox)findViewById(R.id.setting_notification);
        notifications.setChecked(notificationsB); */

        benutzername = (EditText)findViewById(R.id.benutzername);
        passwort = (EditText)findViewById(R.id.passwort);
        url = (EditText)findViewById(R.id.url);
        url.setText(urlS);
        
        Button ok = (Button)findViewById(R.id.einstellungen_ok);
        ok.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                save();
                finish();
            }
        });
    }

    public void save() {
        String lehrerS = lehrer.getText().toString();
        String vlehrerS = vlehrer.getText().toString();
        String urlS = url.getText().toString();
        /* boolean not = notifications.isChecked();
        if(not)
            NotificationEventReceiver.setupAlarm(this, 15);
        else
            NotificationEventReceiver.stopAlarm(this); */

        SharedPreferences.Editor editor = pref.edit();
        String b = benutzername.getText().toString();
        String p = passwort.getText().toString();
        if((!b.isEmpty()) && (!p.isEmpty())) {
            String auth = b+":"+p;
            String a = "";
            try {
                a = Base64.encodeToString(auth.getBytes("UTF-8"), Base64.NO_WRAP);
            } catch(UnsupportedEncodingException e) {}
            editor.putString("auth", a);
        }
        editor.putString("url", urlS);
        editor.putString("lehrer", lehrerS);
        editor.putString("lehrer_full", vlehrerS);
        // editor.putBoolean("notifications", not);
        editor.commit();
        // neue Einstellungen an MainActivity übergeben
        Intent intent = new Intent();
        setResult(RESULT_OK,intent);
    }

    @Override
    public void onBackPressed() {
        save();
        super.onBackPressed();
    }

}
