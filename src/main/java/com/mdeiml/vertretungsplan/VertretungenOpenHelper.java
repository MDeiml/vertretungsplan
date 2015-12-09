package com.mdeiml.vertretungsplan;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;

public class VertretungenOpenHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "vertretungen";
    private static final String CREATE_DATABASE = "CREATE TABLE "+TABLE_NAME+" (" +
                                               "_id INTEGER PRIMARY KEY," +
                                               "tag TEXT," +
                                               "klasse TEXT," +
                                               "stunde INTEGER," +
                                               "lehrer TEXT," +
                                               "fach TEXT," +
                                               "vlehrer TEXT," +
                                               "vfach TEXT," +
                                               "raum TEXT," +
                                               "bemerkung TEXT)";
    private static final String DELETE_DATABASE = "DROP TABLE IF EXISTS "+TABLE_NAME;

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Vertretungen.db";

    public VertretungenOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void clear(SQLiteDatabase db) {
        db.execSQL("DELETE * FROM "+TABLE_NAME);
    }

    public void reset(SQLiteDatabase db) {
        db.execSQL(DELETE_DATABASE);
        db.execSQL(CREATE_DATABASE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        reset(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        reset(db);
    }
}
