package com.laripping.watchlistwidget;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.Nullable;


public class WatchlistProvider extends ContentProvider {
    public static final String TAG = "WWProvider";
    public static final String AUTHORITY = "com.laripping.watchlistwidget";
    public static final String TABLE_NAME = "titles";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    public static long cur_id = 0;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        /*
         * all of the content URI patterns that the provider should recognize.
         * Sets the integer value for multiple rows in "titles" to 1.
         */
        uriMatcher.addURI(AUTHORITY, TABLE_NAME, 1);
    }

    private SQLiteDatabase dbInstance;


    public WatchlistProvider() {
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        switch (uriMatcher.match(uri)) {
            case 1:
                return "vnd.android.cursor.dir/titles";
            default:
                throw new IllegalArgumentException("This is an Unknown URI " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) == 1) {
            long id = dbInstance.insert(AppDatabaseSqlite.TABLE_NAME, null, values);

            if (id > 0) {
                Uri ret_uri = ContentUris.withAppendedId(CONTENT_URI, id);
                getContext().getContentResolver().notifyChange(ret_uri, null);

                return ret_uri;
            }
            throw new SQLException("Insertion Failed for URI :" + uri);
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

        Cursor cursor = null;
        // If the incoming URI was for all of "titles" - sole case so far
        if (uriMatcher.match(uri) == 1) {
            if (TextUtils.isEmpty(sortOrder)) sortOrder = "_ID ASC";
            cursor = dbInstance.query(AppDatabaseSqlite.TABLE_NAME, AppDatabaseSqlite.ALL_COLUMNS, selection, null, null, null," _ID ASC");
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }
        Log.e(TAG,"URI not matched as expected: "+uri);
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}