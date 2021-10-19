package com.laripping.watchlistwidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

/**
 * Used to extract the Application wide data from the UI code
 * and to always provide the accurate state (titles loaded / list tracked)
 * by interfacing with app storage (Provider , SharedPref respectively)
 */
public class AppState {
    public static final String PREF_LIST_KEY = "ImdblistUrl";
    public static final String PREF_FILE_NAME = "WWLIST";
    private static final String TAG = "State";
    private Context context;

    public AppState(Context ctx) {
        this.context = ctx;
    }

    ////// Public Accessors

    public String getStatus() {
        int count = getTitleCount();
        Log.d(TAG,"getStatus() - count: "+ count);
        if(count >0){
            if(getListUrl()==null){
                return count +" titles imported";
            } else {
                return String.format("Tracking list containing %d titles", count);
            }
        } else {
            return this.context.getResources().getString(R.string.no_titles);
        }
    }

    /**
     * @return The list's URL if one is tracked, or Null
     */
    public String getListUrl() {
        return context
                .getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
                .getString(PREF_LIST_KEY,null);
    }



    /////// Private Helpers

    /**
     * The single point of truth, asks the provider for the current counter
     * @return
     */
    public int getTitleCount() {
        Cursor cursor = this.context.getContentResolver().query(
                WatchlistProvider.CONTENT_URI,
                AppDatabaseSqlite.ALL_COLUMNS,
                null,
                new String[]{""},
                "_ID ASC");
        int count = cursor.getCount();
        Log.i(TAG, "Cursor returned " + count+ " rows");
        return count;
    }
}