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

    private WebView webview;
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
            HttpURLConnection connection = (HttpURLConnection)url[0].openConnection();
            connection.setRequestProperty("Authorization", "Basic c2NodWVsZXI6d2ludGVyODYzMTY=");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "iso-8859-1"));
            String src = "";
            String line;
            while((line = in.readLine()) != null) {
                src += line + '\n';
            }

            int index = -1;
            while((index = src.indexOf("<tr class=\"normal", index+1)) >= 0) {
                int end = src.indexOf("</tr>", index);
                int i = src.indexOf("<td class=\"VBlock", index);
                if(i < 0 || i > end)
                    continue;
                i = src.indexOf(">", i);
                int j = src.indexOf("<", i);
                String classes = src.substring(i+1, j).trim();
                if(!(classes.startsWith(klassenstufe) && classes.contains(klassenbuchstabe))) {
                    src = src.substring(0, index) + src.substring(end+5);
                }
            }
            publishProgress(src);
        }catch(Exception e) {
            return e;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        webview.loadData(Uri.encode(progress[0]), "text/html; charset=UTF-8", null);
    }

    @Override
    protected void onPostExecute(Exception e) {
        if(e != null)
            Toast.makeText(c, e.getMessage(), Toast.LENGTH_LONG).show();
    }

}
