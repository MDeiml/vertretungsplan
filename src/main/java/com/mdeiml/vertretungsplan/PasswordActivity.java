package com.mdeiml.vertretungsplan;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import java.io.UnsupportedEncodingException;
import android.widget.Toast;

public class PasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password);
        Toolbar toolbar = (Toolbar)findViewById(R.id.password_toolbar);
        setSupportActionBar(toolbar);
        Button ok = (Button)findViewById(R.id.passwort_ok);
        ok.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                save();
            }
        });
    }

    @Override
    public void save() {
        SharedPreferences prefs = getSharedPreferences("com.mdeiml.vertretungsplan.Einstellungen", MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        EditText benutzername = (EditText)findViewById(R.id.benutzername);
        EditText passwort = (EditText)findViewById(R.id.passwort);
        String b = benutzername.getText().toString();
        String p = passwort.getText().toString();
        String auth = b+":"+p+"86316";
        String a = "";
        try {
            a = Base64.encodeToString(auth.getBytes("UTF-8"), Base64.NO_WRAP);
        } catch(UnsupportedEncodingException e) {}
        if(a.equals("c2NodWVsZXI6d2ludGVyODYzMTY=")) {
            edit.putString("auth", a);
            edit.commit();
            setResult(RESULT_OK,new Intent());
            finish();
        }else {
            Toast.makeText(this, a, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {}
    
}
