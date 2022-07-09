package com.laripping.watchlistwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.SizeF;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

/**
 * Implementation of App Widget functionality.
 */
public class GridWidgetProvider extends AppWidgetProvider {

    private static final String REFRESH_ACTION = "com.laripping.watchlistwidget.REFRESH";
    private static final String CLICK_ACTION2 = "com.laripping.watchlistwidget.CLICK2";
    public  static final String EXTRA_CONST = "com.laripping.watchlistwidget.const";
    private static final String TAG = "GridWidgetProvider";

    private static HandlerThread sWorkerThread;
    private static Handler sWorkerQueue;
    private WatchlistDataProviderObserver sDataObserver;


    public GridWidgetProvider(){
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
            ComponentName cn = new ComponentName(context, GridWidgetProvider.class);
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
        RemoteViews rv = buildRemoteViews(context, appWidgetId);
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
            RemoteViews rv = buildRemoteViews(context, appWidgetIds[i]);
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * Creates the {@link RemoteViews RemoteViews} of the widget
     * ...called from onUpdate() / onAppWidgetOptionsChanged()
     * @param context
     * @param appWidgetId
     * @return
     */
    private static RemoteViews buildRemoteViews(Context context, int appWidgetId) {
        Log.d(TAG,"Building RemoteView for widget with ID: "+appWidgetId);
        RemoteViews rv = null;

        // 1 - Bind the service that will provide the adapter

        Intent serviceIntent = new Intent(context, WidgetService.class);
        // embed the appWidgetId via the data (otherwise it will be ignored)
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.putExtra("listOrGridExtra", "grid");
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        rv = new RemoteViews(context.getPackageName(), R.layout.watchlist_widget_grid_layout);
        rv.setRemoteAdapter(R.id.title_grid, serviceIntent);
        rv.setEmptyView(R.id.title_grid, R.id.empty_view2);

        // 2 - Bind a click listener to each item

        Intent onClickIntent = new Intent(context, GridWidgetProvider.class);
        onClickIntent.setAction(GridWidgetProvider.CLICK_ACTION2);
        // we need to update the intent's data if we set an extra (otherwise will be ignored)
        onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0,
                onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.title_grid, onClickPendingIntent);

        // 3 - Bind the click intent for the widget header

        Intent startAppIntent = new Intent(context, MainActivity.class);
        PendingIntent startAppPendingIntent = PendingIntent.getActivity(context, 0, startAppIntent, 0);
        rv.setOnClickPendingIntent(R.id.theader2, startAppPendingIntent);

        Log.d(TAG,"Built!");
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
        Log.d(TAG,"onAppWidgetOptionsChanged(). Widget with ID: "+appWidgetId+" resized. About to build RemoteView");

        // TODO add logic to columns++ when width_cells++
        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int maxWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
        Log.d(TAG,String.format("Current WxH windows: [%d-%d]x[%d-%d]",minWidth, maxWidth, minHeight, maxHeight));

        /** Requires Android 12 (API 31) - Even my Pixel is 11
         * ArrayList<SizeF> sizes = newOptions.getParcelableArrayList(AppWidgetManager.OPTION_APPWIDGET_SIZES);
         *         if (sizes == null || sizes.isEmpty()) {
         *             Log.w(TAG, "Options | Sizes are null!");
         *         } else {
         *             Log.d(TAG, "Options | Sizes obtained:");
         *             for (SizeF s : sizes) {
         *                 Log.d(TAG, "\t" + s.toString());
         *             }
         *         }
         */

        RemoteViews remoteViews = buildRemoteViews(context, appWidgetId);
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
            ComponentName cn = new ComponentName(context, GridWidgetProvider.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.title_grid);
        } else if (action.equals(CLICK_ACTION2)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            String clickedConst = intent.getStringExtra(GridWidgetProvider.EXTRA_CONST);

            String imdbUrl = String.format("https://www.imdb.com/title/%s/", clickedConst);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(imdbUrl));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
        super.onReceive(context, intent);
    }
}