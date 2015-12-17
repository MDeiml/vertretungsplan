package com.mdeiml.vertretungsplan;

import android.webkit.WebView;
import android.os.AsyncTask;
import android.net.Uri;
import android.widget.Toast;
import android.content.Context;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.util.Log;
import java.text.SimpleDateFormat;
import android.content.SharedPreferences;

public class UpdateVertretungsplan extends AsyncTask<Void, Void, Exception> {

    private Context c;
    private AsyncTask<Void, ?, ?> after;

    public UpdateVertretungsplan(Context c, AsyncTask<Void, ?, ?> after) {
        this.c = c;
        this.after = after;
    }

    @Override
    protected Exception doInBackground(Void... v) {
        String url = c.getResources().getString(R.string.vp_url);
        SharedPreferences pref = c.getSharedPreferences("com.mdeiml.vertretungsplan.Einstellungen", c.MODE_PRIVATE);
        String auth = pref.getString("auth", "");
        VertretungenOpenHelper openHelper = new VertretungenOpenHelper(c);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        openHelper.reset(db);
        try {
            Connection.Response response = Jsoup.connect(url).header("Authorization", "Basic "+auth).execute();
            //TODO nur parsen, wenn neuer Vertretungsplan vorhanden
            Document doc = response.parse();

            Elements tageE = doc.select("div");
            for(Element tagE : tageE) {
                String datumRaw = tagE.select("td.Datum").get(0).ownText();
                String[] datum1 = datumRaw.split(" ")[1].split("\\.");
                String datum = datum1[2]+"-"+datum1[1]+"-"+datum1[0];

                Elements bitteBeachtenBlock = tagE.select("table.BitteBeachtenBlock");
                String bitteBeachten = "";
                if(!bitteBeachtenBlock.isEmpty()) {
                    Element bitteBeachtenE = bitteBeachtenBlock.get(0).select("tr").get(0).children().get(1);
                    bitteBeachtenE.select("br").append("\\n");
                    bitteBeachten = bitteBeachtenE.text().replaceAll("\\\\n", "§");
                }

                ContentValues tagValues = new ContentValues();
                tagValues.put("tag", datum);
                tagValues.put("klasse", "all");
                tagValues.put("stunde", 0);
                tagValues.put("fach", datumRaw);
                tagValues.put("vlehrer", "");
                tagValues.put("vfach", "");
                tagValues.put("raum", "");
                tagValues.put("bemerkung", bitteBeachten);

                db.insert(VertretungenOpenHelper.TABLE_NAME, "null", tagValues);

                Elements vb = tagE.select("table.VBlock");
                if(vb.isEmpty())
                    continue;
                Element vblockE = vb.get(0);
                Elements vertretungenE = vblockE.select("tr");
                vertretungenE.remove(0); // table header weg lassen
                for(Element vertretungE : vertretungenE) {
                    Elements children = vertretungE.children();

                    ContentValues values = new ContentValues();
                    values.put("tag", datum);
                    values.put("klasse", children.get(0).ownText());
                    String stundeS = children.get(1).ownText();
                    int stundeI = Integer.parseInt(stundeS.substring(stundeS.indexOf(" ")+1, stundeS.indexOf(".")));
                    values.put("stunde", stundeI);
                    String[] lehrerFach = children.get(2).ownText().split(" / ");
                    values.put("lehrer", lehrerFach[0]);
                    values.put("fach", lehrerFach[1]);
                    values.put("vlehrer", children.get(3).ownText());
                    values.put("vfach", children.get(4).ownText());
                    values.put("raum", children.get(5).ownText());
                    values.put("bemerkung", children.get(6).ownText());

                    db.insert(VertretungenOpenHelper.TABLE_NAME, "null", values);
                }
            }
        }catch(Exception e) {
            return e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Exception e) {
        if(e != null)
            Log.e("UpdateVertretungsplan", "", e);
        if(after != null)
            after.execute();
    }

}
