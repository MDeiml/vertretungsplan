package com.mdeiml.vertretungsplan;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.content.SharedPreferences;
import android.content.Intent;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.TextView.SavedState;

public class SettingsActivity extends Activity {

    private SharedPreferences pref; // Einstellungen
    private Spinner klassenstufe; // Klassenstufe (0 -> "5")
    private EditText klassenbuchstabe;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        pref = getSharedPreferences("com.mdeiml.vertretungsplan.Einstellungen", MODE_PRIVATE); // alte Einstellungen laden
        int klassenstufeI = pref.getInt("klassenstufe", 0); // Default: 5A
        String klassenbuchstabeS = pref.getString("klassenbuchstabe", "A");

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
        // Einstellungen speichern
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("klassenstufe", ks);
        editor.putString("klassenbuchstabe", kb);
        editor.commit();
        // neue Einstellungen an MainActivity übergeben
        Intent intent = new Intent();
        intent.putExtra("klassenstufe", ks);
        intent.putExtra("klassenbuchstabe", kb);
        setResult(RESULT_OK,intent);
    }

    @Override
    public void onBackPressed() {
        save();
        super.onBackPressed();
    }

}
