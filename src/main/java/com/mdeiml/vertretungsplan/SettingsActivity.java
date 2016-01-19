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
    private Spinner klassenstufe; // Klassenstufe (0 -> "5")
    private EditText klassenbuchstabe;
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
        int klassenstufeI = pref.getInt("klassenstufe", 0); // Default: 5A
        String klassenbuchstabeS = pref.getString("klassenbuchstabe", "A");
        boolean notificationsB = pref.getBoolean("notifications", true);
        String urlS = pref.getString("url", getResources().getString(R.string.vp_url));

        klassenbuchstabe = (EditText)findViewById(R.id.klassenbuchstabe);
        klassenbuchstabe.setText(klassenbuchstabeS);
        if(klassenstufeI > 5) {
            klassenbuchstabe.setEnabled(false);
        }

        klassenstufe = (Spinner)findViewById(R.id.klassenstufe);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.klassenstufen, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        klassenstufe.setAdapter(adapter);
        klassenstufe.setSelection(klassenstufeI);
        klassenstufe.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if(pos > 5) { // Für Q11 und Q12 Eingabe des Buchstaben sperren
                    klassenbuchstabe.setText("");
                    klassenbuchstabe.setEnabled(false);
                }else {
                    klassenbuchstabe.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
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
        int ks = klassenstufe.getSelectedItemPosition();
        String kb = klassenbuchstabe.getText().toString();
        String urlS = url.getText().toString();
        /* boolean not = notifications.isChecked();
        if(not)
            NotificationEventReceiver.setupAlarm(this, 15);
        else
            NotificationEventReceiver.stopAlarm(this); */
        SharedPreferences prefs = getSharedPreferences("com.mdeiml.vertretungsplan.Einstellungen", MODE_PRIVATE);

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
        editor.putInt("klassenstufe", ks);
        editor.putString("klassenbuchstabe", kb.toUpperCase());
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
