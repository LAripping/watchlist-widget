package com.laripping.watchlistwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;

import java.util.concurrent.ExecutionException;

/**
 * This is the service through which a remote adapter can request RemoteViews.
 * So it provides the {@link WatchlistRemoteViewsFactory factory} to be bound to the collection.
 */
public class WidgetService extends RemoteViewsService {
    private String TAG = "WService";

    @Override
    public IBinder onBind(Intent intent) {
        String action = intent.getAction();
        Log.d(TAG,"onBind() - Intent action: "+action);
        return super.onBind(intent);
    }


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d(TAG,"onGetViewfactory() - Intent action: "+intent.getAction());
        return new WatchlistRemoteViewsFactory(this.getApplicationContext(), intent);
    }


    /**
     *  This is the factory that will provide data to the collection widget
     *  see onDataSetChanged()
     */
    class WatchlistRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private static final String TAG = "WWRVFactory";
        private Context mContext;
        private Cursor mCursor = null;
        private int mAppWidgetId;


        public WatchlistRemoteViewsFactory(Context context, Intent intent) {
            Log.d(TAG,"ctor()");
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        /**
         *  Normally, in onCreate() you setup any connections / cursors to your data
         *  Heavy lifting, for example downloading or creating content
         *  etc, should be deferred to onDataSetChanged() or getViewAt().
         *  Taking more than 20 seconds in this call will result in an ANR.
         */
        @Override
        public void onCreate() {
            Log.d(TAG,"onCreate()");
            // Since we reload the cursor in onDataSetChanged() which gets called immediately after
            // onCreate(), we do nothing here.
        }

        /**
         * Refresh the cursor by asking the provider
         * Called when notifyDataSetChanged() is triggered on the remote adapter.
         */
        @Override
        public void onDataSetChanged() {
            Log.d(TAG,"onDataSetChanged()");
            if (mCursor != null) {
                mCursor.close();
            }

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            boolean onlyMovies = sharedPreferences.getBoolean(
                    getResources().getString(R.string.key_show_only_movies),
                    true
            );
            Log.d(TAG,"showing only movies? "+onlyMovies);
            String selectionClause = null;
            String[] selectionArgs = new String[]{""};

            if(onlyMovies){
                selectionClause = AppDatabaseSqlite.COLUMN_TYPE+" IN (?,?)";
                selectionArgs = new String[] {
                        Title.Type.movie.toString(),
                        Title.Type.tvMovie.toString()
                };
            }

            mCursor = mContext.getContentResolver().query(
                    WatchlistProvider.CONTENT_URI,
                    AppDatabaseSqlite.ALL_COLUMNS,
                    selectionClause,
                    selectionArgs ,
                    "_ID ASC");
            Log.i(TAG,"Cursor returned in time, includes "+mCursor.getCount()+" rows");
        }

        @Override
        public void onDestroy() {
            Log.d(TAG,"onDestroy()");
            if (mCursor != null) {
                mCursor.close();
            }
        }

        @Override
        public int getCount() {
            int count = mCursor.getCount();
            Log.d(TAG,"getCount(): "+count);
            return count;
        }

        /**
         * Get watchlist item for a given widget position from the cursor
         * @param i
         * @return a RemoteView populated with icon/text
         */
        @Override
        public RemoteViews getViewAt(int i) {
            Log.d(TAG,"getViewAt("+i+")");
            if (!mCursor.moveToPosition(i))
                Log.e(TAG,"Item not found at position "+i+"!");
            Title item = new Title(mCursor);

            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.watchlist_widget_list_item);
            String firstRow = String.format("%s (%d)", item.getTitle(), item.getYear());
            String secondRow = String.format("%d' \u2022 %s",item.getRuntime(), item.getGenres());
            String rating = String.format("%.1f",item.getRating());
            rv.setTextViewText(R.id.widget_item_title, firstRow);
            rv.setTextViewText(R.id.widget_item_subtitle, secondRow);
            rv.setTextViewText(R.id.widget_item_rating, rating);
            String omdbApiKey = new AppState(mContext).getApiKey();
            if(omdbApiKey!=null){       // only loadPoster() if an API key is set
                String posterUrl = String.format("https://img.omdbapi.com/?apikey=%s&i=%s",omdbApiKey,item.gettConst());
                loadPoster(rv, posterUrl, item.gettConst());
            }

            // Set the click FillIntent (similar to the PendingIntent one) so that we can handle it in the provider's onReceive()
            Intent fillInIntent = new Intent();
            Bundle extras = new Bundle();
            extras.putString(WatchlistWidget.EXTRA_CONST, item.gettConst());
            fillInIntent.putExtras(extras);
            rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);
            return rv;
        }

        /**
         *
         * @return null to get the default loading view
         */
        @Override
        public RemoteViews getLoadingView() {
            Log.d(TAG,"getLoadingView()");
            // TODO spinner
            return null;
        }

        @Override
        public int getViewTypeCount() {
            Log.d(TAG,"getViewTypeCount()");
            // the short and the wide format (TODO should we also count the dark variations?)
            return 2;
        }

        @Override
        public long getItemId(int i) {
            Log.d(TAG,"getitemId("+i+")");
            return i;
        }

        @Override
        public boolean hasStableIds() {
            Log.d(TAG,"hasStableIds()");
            return false;
        }

        private void loadPoster(RemoteViews remoteViews, String url, String tconst) {
            try {
                Log.d(TAG,"Fetching URL for "+tconst+"...");
                FutureTarget<Bitmap> submit = Glide
                        .with(this.mContext)
                        .asBitmap()
                        .load(url)
                        .submit(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL);
                remoteViews.setImageViewBitmap(R.id.widget_item_poster, submit.get());
                Log.i(TAG,"Managed to set Bitmap for "+tconst);
                Glide.with(this.mContext).clear(submit);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                // uses the default img we've created, so all good
            }
        }
    }
}