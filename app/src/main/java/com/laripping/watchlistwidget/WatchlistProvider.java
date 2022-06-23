package com.laripping.watchlistwidget;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.Nullable;

import java.util.Arrays;


public class WatchlistProvider extends ContentProvider {
    public static final String TAG = "WWProvider";
    public static final String AUTHORITY = "com.laripping.watchlistwidget";
    public static final String TABLE_NAME = "titles";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    public static long cur_id = 0;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int PATTERN_ALL = 1;
    private static final int PATTERN_SINGLE = 2;

    static {
        /*
         * all of the content URI patterns that the provider should recognize.
         * Sets the integer value for multiple rows in "titles" to 1.
         */
        uriMatcher.addURI(AUTHORITY, TABLE_NAME, PATTERN_ALL);
    }

    private SQLiteDatabase dbInstance;


    public WatchlistProvider() {
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        switch (uriMatcher.match(uri)) {
            case PATTERN_ALL:
                return "vnd.android.cursor.dir/titles";
            default:
                throw new IllegalArgumentException("This is an Unknown URI " + uri);
        }
    }

    /**
     * Delete one or more rows. Currently only PATTERN_ALL is supported
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG,"delete() URI: "+uri);
        int delCount = 0;
        if (uriMatcher.match(uri) == PATTERN_ALL) {
            delCount = dbInstance.delete(AppDatabaseSqlite.TABLE_NAME, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return delCount;
        }
        return 0;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG,"insert() URI: "+uri);
        if (uriMatcher.match(uri) == PATTERN_ALL) { // not technically an _ALL operation
            // This is now parameterised in settings, insert all - query acording to the Pref
//            Title.Type type = Enum.valueOf(Title.Type.class, values.getAsString(AppDatabaseSqlite.COLUMN_TYPE) );
//            if(type!=Title.Type.movie && type!=Title.Type.tvMovie){
//                Log.i(TAG,"Title "+values.getAsString(AppDatabaseSqlite.COLUMN_TITLE)+" was not a movie. Ignored");
//                return null;
//            }
            try{
                long id = dbInstance.insertOrThrow(AppDatabaseSqlite.TABLE_NAME, null, values);
                Uri ret_uri = ContentUris.withAppendedId(CONTENT_URI, id);
                getContext().getContentResolver().notifyChange(ret_uri, null);
                return ret_uri;
            } catch (SQLException e) {
                if(e.getClass() == SQLiteConstraintException.class){
//                    getMessage().contains("UNIQUE constraint failed")){
                    // all good, just an existing title in the DB
                    Log.i(TAG,"Title with const="+values.getAsString(AppDatabaseSqlite.COLUMN_CONST)+" existed. Ignored");
                } else{
                    Log.e(TAG,"INSERT failed but not because of existing CONST!",e);
                }
            }
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        AppDatabaseSqlite helper = new AppDatabaseSqlite(getContext());
        dbInstance = helper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(TAG,String.format("query() \n\tURI: %s\n\tselection: %s\n\tselectionArgs: %s",
                uri, selection, Arrays.toString(selectionArgs))
        );
        Log.d(TAG,"selection: query() URI: "+uri);
        Cursor cursor = null;
        // If the incoming URI was for all of "titles" - sole case so far
        if (uriMatcher.match(uri) == PATTERN_ALL) {
            if (TextUtils.isEmpty(sortOrder)) sortOrder = "_ID ASC";
            if (selection==null) selectionArgs = null;

            cursor = dbInstance.query(AppDatabaseSqlite.TABLE_NAME, AppDatabaseSqlite.ALL_COLUMNS, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }
        Log.e(TAG,"URI not matched as expected: "+uri);
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d(TAG,"update() URI: "+uri);
        throw new UnsupportedOperationException("Not yet implemented");
    }
}