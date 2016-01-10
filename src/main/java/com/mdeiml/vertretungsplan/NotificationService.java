package com.mdeiml.vertretungsplan;

import android.widget.Toast;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v4.app.NotificationCompat;
import android.database.sqlite.SQLiteDatabase;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import android.content.ContentValues;
import android.util.Log;
import java.io.IOException;

public class NotificationService extends IntentService {

    private static final int NOTIFICATION_ID = 1;
    private static final String ACTION_START = "ACTION_START";
    private static final String ACTION_STOP = "ACTION_STOP";

    public static Intent createStartIntent(Context context) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(ACTION_START);
        return intent;
    }

    public static Intent createStopIntent(Context context) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(ACTION_STOP);
        return intent;
    }

    public NotificationService() {
        super(NotificationService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String action = intent.getAction();
            Log.i("NotificationService", "Vertretungen werden geladen...");
            int newEntries = -2;
            String test = "";
            try {
                newEntries = updateVertretungen(); // Anzahl neuer Einträge für Filteroptionen
            }catch(IOException e) {
                Log.e("NotificationService", "", e); // Kein Internetverbindung
                if(e.getMessage().startsWith("Unable to resolve host"))
                    newEntries = -1;
            }
            Log.i("NotificationService", newEntries+" neue Einträge");
            if(action.equals(ACTION_START)) {
                if(newEntries > 0)
                    startNotification(newEntries);
            }else if(action.equals(ACTION_STOP)) {
                PendingIntent pendingIntent = intent.getParcelableExtra("callback");
                Log.i("NotificationService", "MainActivity sollte jetzt Vertretungen anzeigen");
                try {
                    Intent i = new Intent();
                    i.putExtra("newEntries", newEntries);
                    i.putExtra("a", test);
                    pendingIntent.send(this, MainActivity.RESULT_OK, i);
                }catch (PendingIntent.CanceledException e) {
                    Log.e("NotificationService", null, e);
                }
                deleteNotification();
            }
        }finally {
            WakefulBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    private int updateVertretungen() throws IOException {
        SharedPreferences pref = getSharedPreferences("com.mdeiml.vertretungsplan.Einstellungen", MODE_PRIVATE);
        String url = pref.getString("url", getResources().getString(R.string.vp_url));
        String auth = pref.getString("auth", "");
        int ks = pref.getInt("klassenstufe", 0);
        String kb = pref.getString("klassenbuchstabe", "A");
        VertretungenOpenHelper openHelper = new VertretungenOpenHelper(this);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        // Alte Einträge markieren
        ContentValues oldValues = new ContentValues();
        oldValues.put("old", 1);
        db.update(VertretungenOpenHelper.TABLE_NAME, oldValues, null, null);

        int newEntries = 0;
        // Seite Abrufen
        Connection.Response response = Jsoup.connect(url).header("Authorization", "Basic "+auth).execute();
        
        //TODO nur parsen, wenn neuer Vertretungsplan vorhanden
        Document doc = response.parse();

        // Auslesen der Seite
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
            tagValues.put("old", 0);

            db.insert(VertretungenOpenHelper.TABLE_NAME, "null", tagValues);

            Elements vb = tagE.select("table.VBlock");
            if(vb.isEmpty())
                continue;
            Element vblockE = vb.get(0);
            Elements vertretungenE = vblockE.select("tr");
            vertretungenE.remove(0); // table header weg lassen
            for(Element vertretungE : vertretungenE) {
                Elements children = vertretungE.children();

                String klasse = children.get(0).ownText();
                String stundeS = children.get(1).ownText();
                int stundeI = Integer.parseInt(stundeS.substring(stundeS.indexOf(" ")+1, stundeS.indexOf(".")));
                String[] lehrerFach = children.get(2).ownText().split(" / ");
                String lehrer = lehrerFach[0];
                String fach = lehrerFach[1];
                String vlehrer = children.get(3).ownText();
                String vfach = children.get(4).ownText();
                String raum = children.get(5).ownText();
                String bemerkung = children.get(6).ownText();

                if(klasse.equals("all") || (klasse.startsWith(""+(ks+5)) && klasse.contains(kb))) {
                    String[] projection = new String[] {};
                    String selection = "tag='" + datum + "' AND " +
                        "klasse='" + klasse + "' AND " +
                        "stunde=" + stundeI + " AND " +
                        "lehrer='" + lehrer + "' AND " +
                        "fach='" + fach + "' AND " +
                        "vlehrer='" + vlehrer + "' AND " +
                        "vfach='" + vfach + "' AND " +
                        "raum='" + raum + "' AND " +
                        "bemerkung='" + bemerkung + "' AND " +
                        "old=1";
                    Cursor cursor = db.query(VertretungenOpenHelper.TABLE_NAME, projection, selection, null, null, null, null, null);
                    if(cursor.getCount() == 0)
                        newEntries++;
                    cursor.close();
                }

                ContentValues values = new ContentValues();
                values.put("tag", datum);
                values.put("klasse", klasse);
                values.put("stunde", stundeI);
                values.put("lehrer", lehrer);
                values.put("fach", fach);
                values.put("vlehrer", vlehrer);
                values.put("vfach", vfach);
                values.put("raum", raum);
                values.put("bemerkung", bemerkung);
                values.put("old", 0);

                db.insert(VertretungenOpenHelper.TABLE_NAME, "null", values);
            }
        }
        db.delete(VertretungenOpenHelper.TABLE_NAME, "old=1", null);
        return newEntries;
    }

    private void startNotification(int newEntries) {
        SharedPreferences pref = getSharedPreferences("com.mdeiml.vertretungsplan.Einstellungen", MODE_PRIVATE);
        newEntries += pref.getInt("newEntries", 0);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Neue Vertretung")
               .setAutoCancel(true)
               .setColor(getResources().getColor(R.color.ColorPrimary))
               .setContentText(newEntries + " neue Vertretungen")
               .setSmallIcon(R.drawable.ic_notification);
        PendingIntent intent = PendingIntent.getActivity(this, NOTIFICATION_ID, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(intent);
        builder.setDeleteIntent(NotificationEventReceiver.getStopIntent(this));
        final NotificationManager manager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("newEntries", newEntries);
        editor.commit();
    }

    private void deleteNotification() {
        SharedPreferences pref = getSharedPreferences("com.mdeiml.vertretungsplan.Einstellungen", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("newEntries", 0);
        editor.commit();
        final NotificationManager manager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

}
