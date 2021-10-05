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
    public static final String TAG = "WWCsv";

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
            newValues.put(Title.COLUMN_TITLE, csvLine.get(CSV_TITLE));
            newValues.put(Title.COLUMN_CONST, csvLine.get(CSV_CONST));
            newValues.put(Title.COLUMN_CREATED, csvLine.get(CSV_CREATED));
            newValues.put(Title.COLUMN_DIRECTORS, csvLine.get(CSV_DIRECTORS));
            newValues.put(Title.COLUMN_GENRES, csvLine.get(CSV_GENRES));
            newValues.put(Title.COLUMN_RATING, csvLine.get(CSV_RATING));
            newValues.put(Title.COLUMN_RUNTIME, csvLine.get(CSV_RUNTIME));
            newValues.put(Title.COLUMN_TYPE, csvLine.get(CSV_TYPE));
            newValues.put(Title.COLUMN_YEAR, csvLine.get(CSV_YEAR));

            // TODO check Const and don't add existing ones

            Uri returi = this.context.getContentResolver().insert(
                    WatchlistProvider.CONTENT_URI,
                    newValues
            );
            Log.i(TAG,"Call to provider's insert() return URI:"+returi.toString());
        }

    }
}
