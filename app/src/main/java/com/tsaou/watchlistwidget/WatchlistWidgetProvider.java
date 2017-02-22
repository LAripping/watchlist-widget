package com.tsaou.watchlistwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Bundle;


public class WatchlistWidgetProvider extends AppWidgetProvider {
    /**
     *  Called in first placement. Also called every "updatePeriodMillis" to refresh the widget
     *
     * @param context App context
     * @param appWidgetManager The reference to the Android-wide AppWidgetManager singleton that actually carries out the tasks
     * @param appWidgetIds The id of each of the Widgets currently added or to-add for my app
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
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
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }
}
