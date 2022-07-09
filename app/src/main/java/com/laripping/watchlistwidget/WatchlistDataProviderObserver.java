package com.laripping.watchlistwidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

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
        Log.d(TAG,"onChange() - component.getClassName(): "+mComponentName.getClassName());
        if(mComponentName.getClassName().equals("com.laripping.watchlistwidget.GridWidgetProvider")){
            mAppWidgetManager.notifyAppWidgetViewDataChanged(
                    mAppWidgetManager.getAppWidgetIds(mComponentName),
                    R.id.title_grid
            );
        } else {
            mAppWidgetManager.notifyAppWidgetViewDataChanged(
                    mAppWidgetManager.getAppWidgetIds(mComponentName),
                    R.id.title_list
            );
        }


    }
}
