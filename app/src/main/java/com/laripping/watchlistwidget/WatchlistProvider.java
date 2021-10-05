package com.laripping.watchlistwidget;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class WatchlistProvider extends ContentProvider {
    public static final String TAG = "WWProvider";
    public static long cur_id = 0;
    public static final String AUTHORITY = "com.laripping.watchlistwidget";
    public static final String TABLE_NAME = "titles";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        /*
         * all of the content URI patterns that the provider should recognize.
         * Sets the integer value for multiple rows in "titles" to 1.
         */
        uriMatcher.addURI(AUTHORITY, TABLE_NAME, 1);
    }

    public WatchlistProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) == 1) {
            final Context context = getContext();
            if (context == null) return null;

            // https://stackoverflow.com/questions/56043437/getting-insert-id-with-rxjava-room
            // problem was how to get ID from insert() which normally returns Completeable
            AppDatabase.getInstance(context).titleDao()
                    .insert(Title.fromContentValues(values))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Long>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }
                        @Override
                        public void onSuccess(Long id) {
                            cur_id = id;
                            Log.i(TAG,"Successfully inserted title. ID: "+id);
                        }
                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG,"Failed to insert",e);
                        }
                    });
            context.getContentResolver().notifyChange(uri, null);

            return ContentUris.withAppendedId(uri, cur_id);
        }
        return null;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] valuesArray) {
        if (uriMatcher.match(uri) == 1) {
            final Context context = getContext();
            if (context == null) return 0;

            final AppDatabase database = AppDatabase.getInstance(context);
            List<Title> titles = new ArrayList<Title>(valuesArray.length);
//            final Title[] titles = new Title[valuesArray.length];
            for (ContentValues contentValues : valuesArray) {
                titles.add(Title.fromContentValues(contentValues));
            }
            Log.i(TAG,"About to blkInsert "+titles.size()+" titles");
            database.titleDao().insertAll(titles);
            return valuesArray.length; // TODO somehow find the actual value
        }
        return 0;
    }

    @Override
    public boolean onCreate() {
        return true; // created lazily in AppDatabase.getInstance()
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // If the incoming URI was for all of "titles" - sole case so far
        if (uriMatcher.match(uri) == 1) {
            if (TextUtils.isEmpty(sortOrder)) sortOrder = "_ID ASC";
        }

        final Context context = getContext();
        if (context == null) return null;
        TitleDao titleDao = AppDatabase.getInstance(context).titleDao();
        final Cursor cursor = null;
        titleDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Title>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.v(TAG,"getAll() - onSubscribe");
                    }

                    @Override
                    public void onSuccess(@NonNull List<Title> titles) {
                        Log.i(TAG,"getAll() - onSuccess. List size: "+titles.size());

                        // cursor??
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG,"getAll() - onError",e);
                    }
                });


        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}