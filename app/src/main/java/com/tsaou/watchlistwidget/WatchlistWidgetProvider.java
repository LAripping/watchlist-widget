package com.tsaou.watchlistwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;


public class WatchlistWidgetProvider extends AppWidgetProvider {
    private static final String TAG = WatchlistWidgetProvider.class.toString();

    /**
     *  Called in first placement. Also called every "updatePeriodMillis" to refresh the widget
     *
     * @param context App context
     * @param appWidgetManager The reference to the Android-wide AppWidgetManager singleton that actually carries out the tasks
     * @param appWidgetIds The id of each of the Widgets currently added or to-add for my app
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i(TAG, "Inside onUpdate()");
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each Widget of my app
        for (int i=0; i<N; i++) {
            Log.i(TAG, "For widget "+i);
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            //Intent intent = new Intent(context, ExampleActivity.class);
            //PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Set the widget's layout
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_basic);
            Log.i(TAG, "Selected view with id: "+views.getLayoutId()+". ToString: "+views.toString());

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }


    /**
     * Called in first placement. Also called whenever widget is resized
     *
     * @param context App context
     * @param appWidgetManager The reference to the Android-wide AppWidgetManager singleton that actually carries out the tasks
     * @param appWidgetId The id of the widget being resized
     * @param newOptions
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Log.i(TAG, "Inside onAppWidgetOptionsChanged()");

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }
}
