package com.laripping.watchlistwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

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
         */
        @Override
        public void onDataSetChanged() {
            Log.d(TAG,"onDataSetChanged()");
            if (mCursor != null) {
                mCursor.close();
            }
            mCursor = mContext.getContentResolver().query(
                    WatchlistProvider.CONTENT_URI,
                    AppDatabaseSqlite.ALL_COLUMNS,
                    null,
                    new String[]{""},
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
            Log.d(TAG,"getCount()");
            return mCursor.getCount();
        }

        /**
         * Get watchlist item for a given widget position from the cursor
         * @param i
         * @return a RemoteView populated with icon/text
         */
        @Override
        public RemoteViews getViewAt(int i) {
            Log.d(TAG,"getViewAt("+i+")");
            String itemTitle;
            String itemConst;
            if (mCursor.moveToPosition(i)) {
                int titleColIndex = mCursor.getColumnIndex(AppDatabaseSqlite.COLUMN_TITLE);
                itemTitle = mCursor.getString(titleColIndex);
                int constColIndex = mCursor.getColumnIndex(AppDatabaseSqlite.COLUMN_CONST);
                itemConst = mCursor.getString(constColIndex);
            } else {
                Log.e(TAG,"Item not found at position "+i+"!");
                itemTitle = "Unknown title!";
                itemConst = "Unknown const!";
            }

            // TODO add icon
            int itemId = R.layout.watchlist_widget_list_item;
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemId);
            rv.setTextViewText(R.id.widget_item, itemTitle);

            // Set the click FillIntent (similar to the PendingIntent one) so that we can handle it in the provider's onReceive()
            Intent fillInIntent = new Intent();
            Bundle extras = new Bundle();
            extras.putString(WatchlistWidget.EXTRA_CONST, itemConst);
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
    }
}