package com.laripping.watchlistwidget;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderHeaderAware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;

public class CsvUtils {
    /**
     * CSV headers - as of Oct 2021
     */
    public static final String CSV_CONST = "Const";
    public static final String CSV_CREATED = "Created";
    public static final String CSV_TITLE = "Title";
    public static final String CSV_TYPE = "Title Type";
    public static final String CSV_RATING = "IMDb Rating";
    public static final String CSV_RUNTIME = "Runtime (mins)";
    public static final String CSV_YEAR = "Year";
    public static final String CSV_GENRES = "Genres";
    public static final String CSV_DIRECTORS = "Directors";
    public static final String TAG = "Csv";

    public Context context;

    public CsvUtils(Context ctx) {
        this.context = ctx;
    }


    void parseCsvFile(BufferedReader reader) throws IOException {
        CSVReaderHeaderAware csvReaderHeaderAware = new CSVReaderHeaderAware(reader);
        Map<String, String> csvLine;
        while (((csvLine = csvReaderHeaderAware.readMap())) != null)   // read line
        {
            // Defines an object to contain the new values to insert
            ContentValues newValues = new ContentValues();

            /*
             * Sets each column value (according to the CSV value) and inserts the title into the provider.
             */
            newValues.put(AppDatabaseSqlite.COLUMN_TITLE, csvLine.get(CSV_TITLE));
            newValues.put(AppDatabaseSqlite.COLUMN_CONST, csvLine.get(CSV_CONST));
            newValues.put(AppDatabaseSqlite.COLUMN_CREATED, csvLine.get(CSV_CREATED));
            newValues.put(AppDatabaseSqlite.COLUMN_DIRECTORS, csvLine.get(CSV_DIRECTORS));
            newValues.put(AppDatabaseSqlite.COLUMN_GENRES, csvLine.get(CSV_GENRES));
            newValues.put(AppDatabaseSqlite.COLUMN_RATING, csvLine.get(CSV_RATING));
            newValues.put(AppDatabaseSqlite.COLUMN_RUNTIME, csvLine.get(CSV_RUNTIME));
            newValues.put(AppDatabaseSqlite.COLUMN_TYPE, csvLine.get(CSV_TYPE));
            newValues.put(AppDatabaseSqlite.COLUMN_YEAR, csvLine.get(CSV_YEAR));

            boolean existing = false;
            Uri returi = null;
            try{
                 returi = this.context.getContentResolver().insert(
                        WatchlistProvider.CONTENT_URI,
                        newValues
                );
            } catch (Exception e) {
                Log.d(TAG,"Provider's insert() threw Exception...");
                existing = true;
            }
            if(returi==null){
                Log.d(TAG,"Provider's insert() returned null URI...");
                existing = true;
            }
            // in any case:
            if(existing) {
                Log.i(TAG, "Title existed in DB, Ignored");
            } else {
                Log.i(TAG, "Call to provider's insert() return URI:" + returi.toString());
            }

        } // while
    }


}
