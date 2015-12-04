package com.mdeiml.vertretungsplan;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class SettingsActivity extends Activity {

    private String klassenstufeS;
    private String klassenbuchstabeS;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        final EditText klassenbuchstabe = (EditText)findViewById(R.id.klassenbuchstabe);

        Spinner klassenstufe = (Spinner) findViewById(R.id.klassenstufe);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.klassenstufen, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        klassenstufe.setAdapter(adapter);
        klassenstufe.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                klassenstufeS = (String)parent.getItemAtPosition(pos);
                if(klassenstufeS.equals("Q11") || klassenstufeS.equals("Q12")) {
                    klassenbuchstabeS = "";
                    klassenbuchstabe.setText("");
                    klassenbuchstabe.setEnabled(false);
                }else {
                    klassenbuchstabe.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

}
