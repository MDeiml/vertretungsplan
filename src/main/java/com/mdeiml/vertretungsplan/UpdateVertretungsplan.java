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
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.util.Log;

public class UpdateVertretungsplan extends AsyncTask<Void, Void, Exception> {

    private Context c;

    public UpdateVertretungsplan(Context c) {
        this.c = c;
    }

    @Override
    protected Exception doInBackground(Void... v) {
        String url = c.getResources().getString(R.string.vp_url);
        VertretungenOpenHelper openHelper = new VertretungenOpenHelper(c);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        openHelper.reset(db);
        try {
            Document doc = Jsoup.connect(url).header("Authorization", "Basic c2NodWVsZXI6d2ludGVyODYzMTY=").get();

            Elements tageE = doc.select("div");
            for(Element tagE : tageE) {
                String datum = tagE.select("td.Datum").get(0).ownText().split(" ")[1];

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
        Log.e("UpdateVertretungsplan", "", e);
    }

}
