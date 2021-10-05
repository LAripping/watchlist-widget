package com.laripping.watchlistwidget;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class AppDatabaseSqlite extends SQLiteOpenHelper {

    //Constants for db name and version
    private static final String DATABASE_NAME = "wwdb.sqlite";
    private static final int DATABASE_VERSION = 1;

    //Constants for table and columns
    public static final String TABLE_NAME = "titles";
    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONST = "const";
    public static final String COLUMN_CREATED = "created";
    public static final String COLUMN_TYPE = "title_type";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_RUNTIME = "runtime";
    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_GENRES = "genres";
    public static final String COLUMN_DIRECTORS = "directors";

    public static final String[] ALL_COLUMNS =
            {COLUMN_CONST,COLUMN_CREATED, COLUMN_GENRES, COLUMN_DIRECTORS, COLUMN_TITLE, COLUMN_YEAR, COLUMN_TYPE, COLUMN_RATING, COLUMN_RUNTIME, COLUMN_ID};

    //Create Table
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_CONST + " TEXT UNIQUE, " +
                    COLUMN_CREATED + " TEXT, " +
                    COLUMN_TYPE + " TEXT, " +
                    COLUMN_RATING + " REAL, " +
                    COLUMN_RUNTIME + " INTEGER, " +
                    COLUMN_YEAR + " INT, " +
                    COLUMN_GENRES + " TEXT, " +
                    COLUMN_DIRECTORS + " TEXT" +
                    ")";

    //Delete Table TODO use in Settings -> Clear all button
    private static final String DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public AppDatabaseSqlite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }
}
