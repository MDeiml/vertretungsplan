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

public class SettingsActivity extends Activity {

    private SharedPreferences pref;
    private Spinner klassenstufe;
    private EditText klassenbuchstabe;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        pref = getSharedPreferences("com.mdeiml.vertretungsplan.Einstellungen", MODE_PRIVATE);
        int klassenstufeI = pref.getInt("klassenstufe", 0);
        String klassenbuchstabeS = pref.getString("klassenbuchstabe", "A");

        klassenbuchstabe = (EditText)findViewById(R.id.klassenbuchstabe);
        klassenbuchstabe.setText(klassenbuchstabeS);

        klassenstufe = (Spinner)findViewById(R.id.klassenstufe);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.klassenstufen, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        klassenstufe.setAdapter(adapter);
        klassenstufe.setSelection(klassenstufeI);
        klassenstufe.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if(pos > 5) { // Für Q11 und Q12
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

    @Override
    public void onBackPressed() {
        super.onStop();

        int ks = klassenstufe.getSelectedItemPosition();
        String kb = klassenbuchstabe.getText().toString();

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("klassenstufe", ks);
        editor.putString("klassenbuchstabe", kb);
        editor.commit();

        Intent intent = new Intent();
        intent.putExtra("klassenstufe", ks);
        intent.putExtra("klassenbuchstabe", kb);
        setResult(RESULT_OK,intent);
        super.onBackPressed();
    }

}
