package com.laripping.watchlistwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Implementation of App Widget functionality.
 * NOT USED YET - App Widget Configuration implemented in {@link WatchlistWidgetConfigureActivity WatchlistWidgetConfigureActivity}
 */
public class WatchlistWidget extends AppWidgetProvider {

    private static final String REFRESH_ACTION = "com.laripping.watchlistwidget.REFRESH";
    private static final String CLICK_ACTION = "com.laripping.watchlistwidget.CLICK";
    public  static final String EXTRA_CONST = "com.laripping.watchlistwidget.const";
    private static final String TAG = "WWidget";

    private static HandlerThread sWorkerThread;
    private static Handler sWorkerQueue;
    private WatchlistDataProviderObserver sDataObserver;

    private boolean mIsWide = true; // is it wide enough for icon+text? Default sizing says yes


    public WatchlistWidget(){
        Log.d(TAG,"ctor()");
        // Start the worker thread
        sWorkerThread = new HandlerThread("WatchlistWidget-worker");
        sWorkerThread.start();
        sWorkerQueue = new Handler(sWorkerThread.getLooper());
    }

    /**
     * Called when the first widget is created.
     * Do heavy lifting here that only needs to occur once for all widgets like open DB
     *
     * Also used to listen for changes to the data
     * (e.g. through user interaction with the main app / a bg service /via the content provider)
     * and update ourselves accordingly
     * @param context
     */
    @Override
    public void onEnabled(Context context) {
        Log.d(TAG,"onEnabled()");
        ContentResolver r = context.getContentResolver();
        if (sDataObserver == null) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            ComponentName cn = new ComponentName(context, WatchlistWidget.class);
            sDataObserver = new WatchlistDataProviderObserver(mgr, cn, sWorkerQueue);
            r.registerContentObserver(WatchlistProvider.CONTENT_URI, true, sDataObserver);
        }
    }


    /**
     * Creates the {@link RemoteViews RemoteViews} of the widget
     * ...called from {@link WatchlistWidgetConfigureActivity ConfigActivity}
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     */
    public static void initWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d(TAG,"Initializing Widget with ID: "+appWidgetId);
        RemoteViews rv = buildRemoteViews(context, appWidgetId, true);
        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }

    /**
     * Called when the widget provider is asked to provide the {@link RemoteViews RemoteViews} of the widget
     * - AppWidgetManager#ACTION_APPWIDGET_UPDATE : widget created (if no ConfigActivity) / update interval elapsed / system boot
     * - AppWidgetManager#ACTION_APPWIDGET_RESTORED : widget re-created from backup -> not supported by this app
     * Updates the {@link RemoteViews RemoteViews} of each widget, through the {@link AppWidgetManager AppWidgetManager} APIs
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds the IDs of AppWidget instances to update. Obtained from the related intent Extra
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG,"onUpdate(). Size of AppWidgetIds[]: "+ appWidgetIds.length);
        for (int i = 0; i < appWidgetIds.length; ++i) {
            Log.d(TAG,"Updating widget #"+i+" with ID: "+ appWidgetIds[i]);
            RemoteViews rv = buildRemoteViews(context, appWidgetIds[i], mIsWide);
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * Creates the {@link RemoteViews RemoteViews} of the widget
     * ...called from onUpdate() / onAppWidgetOptionsChanged()
     * @param context
     * @param appWidgetId
     * @param isWide
     * @return
     */
    private static RemoteViews buildRemoteViews(Context context, int appWidgetId, boolean isWide) {
        Log.d(TAG,"Building RemoteView for widget with ID: "+appWidgetId);
        RemoteViews rv = null;
        if (isWide) {
            // 1 - Bind the service that will provide the adapter

            Intent serviceIntent = new Intent(context, WidgetService.class);
            // embed the appWidgetId via the data (otherwise it will be ignored)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
            rv = new RemoteViews(context.getPackageName(), R.layout.watchlist_widget_list_layout);
            rv.setRemoteAdapter(R.id.title_list, serviceIntent);
            rv.setEmptyView(R.id.title_list, R.id.empty_view);

            // 2 - Bind a click listener to each item

            Intent onClickIntent = new Intent(context, WatchlistWidget.class);
            onClickIntent.setAction(WatchlistWidget.CLICK_ACTION);
            // we need to update the intent's data if we set an extra (otherwise will be ignored)
            onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0,
                    onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.title_list, onClickPendingIntent);

//            // 3 - Bind the click intent for the refresh button on the widget
//            final Intent refreshIntent = new Intent(context, WeatherWidgetProvider.class);
//            refreshIntent.setAction(WeatherWidgetProvider.REFRESH_ACTION);
//            final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0,
//                    refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//            rv.setOnClickPendingIntent(R.id.refresh, refreshPendingIntent);

//            // Restore the minimal header
//            rv.setTextViewText(R.id.city_name, context.getString(R.string.city_name));

        } else {
//            TODO small widget layout
//            rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout_small);
//            // Update the header to reflect the weather for "today"
//            Cursor c = context.getContentResolver().query(WeatherDataProvider.CONTENT_URI, null,
//                    null, null, null);
//            if (c.moveToPosition(0)) {
//                int tempColIndex = c.getColumnIndex(WeatherDataProvider.Columns.TEMPERATURE);
//                int temp = c.getInt(tempColIndex);
//                String formatStr = context.getResources().getString(R.string.header_format_string);
//                String header = String.format(formatStr, temp,
//                        context.getString(R.string.city_name));
//                rv.setTextViewText(R.id.city_name, header);
//            }
//            c.close();
        }
        if(rv == null){
            throw new RuntimeException("RemoteView was not created!");
        }
        return rv;
    }

    /**
     * Called when the widget is resized.
     * - AppWidgetManager#ACTION_APPWIDGET_OPTIONS_CHANGED
     *
     * Decide if we're wide enough to include text, and update the widget anyway
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     * @param newOptions
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        Log.d(TAG,"onAppWidgetOptionsChanged()");
        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int maxWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
        RemoteViews remoteViews;
        if (minWidth < 100) {
            this.mIsWide = false;
        } else {
            this.mIsWide = true;
        }
        Log.d(TAG,"Widget with ID: "+appWidgetId+" resized. About to build RemoteView for "+(this.mIsWide?"wide":"short") + "layout");
        remoteViews = buildRemoteViews(context, appWidgetId, this.mIsWide);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    /**
     * Generically called when the widget's underlying Receiver is triggered. Implemented here to handle 2 custom actions
     * Might be followed by a call to onUpdate (data update) / onAppWidgetOptionsChanged (resize)
     * 1. REFRESH_ACTION : the data held by the Provider (hence the DB) are changed - NOT CURRENTLY USED
     * 2. CLICK_ACTION : an item of the widget is clicked
     *
     * @param context
     * @param intent The intent received by the underlying BroadcastReceiver
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG,"onReceive() intent action: "+action);
        if (action.equals(REFRESH_ACTION)) {
            // maybe all we need is :
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            ComponentName cn = new ComponentName(context, WatchlistWidget.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.title_list);

            // and none of the rest...

            // BroadcastReceivers have a limited amount of time to do work, so for this sample, we
            // are triggering an update of the data on another thread.  In practice, this update
            // can be triggered from a background service, or perhaps as a result of user actions
            // inside the main application.
//            sWorkerQueue.removeMessages(0);
//            sWorkerQueue.post(new Runnable() {
//                @Override
//                public void run() {
//                    ContentResolver resolver = context.getContentResolver();
//                    Cursor cursor = resolver.query(
//                            WatchlistProvider.CONTENT_URI,
//                            null,
//                            null,
//                            null,
//                            null);
//                    int count = cursor.getCount();
//                    // We disable the data changed observer temporarily since each of the updates
//                    // will trigger an onChange() in our data observer.
//                    resolver.unregisterContentObserver(sDataObserver);
//                    for (int i = 0; i < count; ++i) {
//                        Uri uri = ContentUris.withAppendedId(WatchlistProvider.CONTENT_URI, i);
//                        ContentValues values = new ContentValues();
//                        values.put(AppDatabaseSqlite.COLUMN_TITLE, csvLine.get(CSV_TITLE));
//                        values.put(AppDatabaseSqlite.COLUMN_CONST, csvLine.get(CSV_CONST));
//                        values.put(AppDatabaseSqlite.COLUMN_CREATED, csvLine.get(CSV_CREATED));
//                        values.put(AppDatabaseSqlite.COLUMN_DIRECTORS, csvLine.get(CSV_DIRECTORS));
//                        values.put(AppDatabaseSqlite.COLUMN_GENRES, csvLine.get(CSV_GENRES));
//                        values.put(AppDatabaseSqlite.COLUMN_RATING, csvLine.get(CSV_RATING));
//                        values.put(AppDatabaseSqlite.COLUMN_RUNTIME, csvLine.get(CSV_RUNTIME));
//                        values.put(AppDatabaseSqlite.COLUMN_TYPE, csvLine.get(CSV_TYPE));
//                        newValues.put(AppDatabaseSqlite.COLUMN_YEAR, csvLine.get(CSV_YEAR));
//
//                        values.put(Wa.Columns.TEMPERATURE,
//                                new Random().nextInt(sMaxDegrees));
//                        resolver.update(uri, values, null, null);
//                    }
//                    resolver.registerContentObserver(WeatherDataProvider.CONTENT_URI, true, sDataObserver);
//                    final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
//                    final ComponentName cn = new ComponentName(context, WeatherWidgetProvider.class);
//                    mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.weather_list);
//                }
//            });
//            final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
//                    AppWidgetManager.INVALID_APPWIDGET_ID);
        } else if (action.equals(CLICK_ACTION)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            String clickedConst = intent.getStringExtra(WatchlistWidget.EXTRA_CONST);

            String imdbUrl = String.format("https://www.imdb.com/title/%s/",clickedConst);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(imdbUrl));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

//            // for now, just show a toast
//            Toast.makeText(
//                    context,
//                    "Clicked title "+clickedConst+" on Widget #"+appWidgetId+". Not yet implemented!",
//                    Toast.LENGTH_SHORT
//            ).show();
        }
        super.onReceive(context, intent);
    }

//    /**
//     * When the user deletes the widget,
//     * Cleanup any work done in onEnabled(Context):
//     * - delete the preference associated with it (if it exists)
//     * - unregister content observer??
//     * @param context
//     * @param appWidgetIds
//     */
//    @Override
//    public void onDeleted(Context context, int[] appWidgetIds) {
//        Log.d(TAG,"onDeleted()");
//        for (int appWidgetId : appWidgetIds) {
//            WatchlistWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
//        }
//    }


    class WatchlistDataProviderObserver extends ContentObserver {
        private AppWidgetManager mAppWidgetManager;
        private ComponentName mComponentName;
        private String TAG = "WWObserver";

        WatchlistDataProviderObserver(AppWidgetManager mgr, ComponentName cn, Handler h) {
            super(h);
            Log.d(TAG,"ctor()");
            mAppWidgetManager = mgr;
            mComponentName = cn;
        }

        @Override
        public void onChange(boolean selfChange) {
            // The data has changed, so notify the widget that the collection view needs to be updated.
            // In response, the factory's onDataSetChanged() will be called which will require the
            // cursor for the new data.
            Log.d(TAG,"onChange()");
            mAppWidgetManager.notifyAppWidgetViewDataChanged(
                    mAppWidgetManager.getAppWidgetIds(mComponentName), R.id.title_list);
        }
    }
}