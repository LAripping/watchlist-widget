package com.laripping.watchlistwidget;

import android.content.Context;
import android.util.Log;

public class Counter{
    private static final String TAG = "Counter";
    private Context context;

    private int count;
    private String text;

    public Counter(Context ctx, int count) {
        this.context = ctx;
        this.count = count;
        this.text = this.context.getResources().getString(R.string.no_titles);
    }
    public String getText() {
        Log.d(TAG,"getText() - count: "+count);
        if(this.count>0)
            return count+" titles found in the database";
        else
            return this.context.getResources().getString(R.string.no_titles);
    }

    public int getCount() {
        Log.d(TAG,"getCount(): "+count);
        return count;
    }

    public void setCount(int cnt) {
        this.count = cnt;
    }
}