package com.mdeiml.vertretungsplan;

import android.webkit.WebView;
import android.os.AsyncTask;
import android.net.Uri;
import android.widget.Toast;
import android.content.Context;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class UpdateVertretungsplan extends AsyncTask<URL, String, Exception> {

    private WebView webview; // WebView, das den Vertretungsplan entahlten soll
    private Context c;
    private String klassenstufe;
    private String klassenbuchstabe;

    public UpdateVertretungsplan(WebView webview, Context c, String klassenstufe, String klassenbuchstabe) {
        this.webview = webview;
        this.c = c;
        this.klassenstufe = klassenstufe;
        this.klassenbuchstabe = klassenbuchstabe;
    }

    @Override
    protected Exception doInBackground(URL... url) {
        //TODO: Das hier schreiben
        try {
            HttpURLConnection connection = (HttpURLConnection)url[0].openConnection(); // Verbindung mit Server aufbauen
            connection.setRequestProperty("Authorization", "Basic c2NodWVsZXI6d2ludGVyODYzMTY=");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "iso-8859-1")); // InputStream mit Umlauten lesen
            String src = "";
            String line;
            while((line = in.readLine()) != null) { // alles laden
                src += line + '\n';
            }

            int dayIndex = src.indexOf("<div>");
            int index = -1;
            boolean emptyDay = true;
            while((index = src.indexOf("<tr class=\"normal", index+1)) >= 0) { // nächster Eintrag
                int nextDayIndex = src.indexOf("<div>", dayIndex + 1);
                if(nextDayIndex != -1 && index > nextDayIndex) {
                    int beachtenI = src.indexOf("<table class=\"BitteBeachten", dayIndex);
                    if(beachtenI != -1 && beachtenI < nextDayIndex)
                        emptyDay = false;
                    if(emptyDay) {
                        src = src.substring(0, dayIndex) + src.substring(nextDayIndex);
                        index = dayIndex;
                    }else {
                        dayIndex = nextDayIndex;
                    }
                    emptyDay = true;
                }
                int end = src.indexOf("</tr>", index); // Ende des Eintrags
                int i = src.indexOf("<td class=\"VBlock", index); // 1. Spalte des Eintrags (Klasse)
                if(i < 0 || i > end) // wenn keine Klasse in dem Eintrag ist überspringen
                    continue;
                i = src.indexOf(">", i);
                int j = src.indexOf("<", i);
                String classes = src.substring(i+1, j).trim(); // Inhalt der 1. Spalte
                if(!(classes.startsWith(klassenstufe) && classes.contains(klassenbuchstabe))) {
                    src = src.substring(0, index) + src.substring(end+5); // wenn nicht die eingestellte Klasse Zeile entfernen
                }else {
                    emptyDay = false;
                }
            }
            int beachtenI = src.indexOf("<table class=\"BitteBeachten", dayIndex);
            if(beachtenI != -1)
                emptyDay = false;
            if(emptyDay) {
                int nextDayIndex = src.indexOf("</div>", dayIndex + 1)+6;
                src = src.substring(0, dayIndex) + src.substring(nextDayIndex);
                index = dayIndex;
            }
            publishProgress(src); // Vertretungsplan anzeigen
        }catch(Exception e) {
            return e;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        webview.loadData(Uri.encode(progress[0]), "text/html; charset=UTF-8", null); // progress als html anzeigen
    }

    @Override
    protected void onPostExecute(Exception e) {
        if(e != null) // wenn Fehler vorhanden diesen anzeigen
            Toast.makeText(c, e.getMessage(), Toast.LENGTH_LONG).show();
    }

}
