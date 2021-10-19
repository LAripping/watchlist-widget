package com.laripping.watchlistwidget;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.InputStream;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private String TAG = "DITask";
    private final String tconst;
    private RemoteViews rv;
    private String posterUrl;

    public DownloadImageTask(RemoteViews rv, String tconst) {
        this.rv = rv;
        this.tconst = tconst;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        this.posterUrl = urls[0];
        Bitmap posterBitmap = null;
        try {
            InputStream inputStream = new java.net.URL(posterUrl).openStream();
            Log.d(TAG,"Fetching URL for "+tconst+"...");
            posterBitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        return posterBitmap;
    }

    protected void onPostExecute(Bitmap result) {
        this.rv.setImageViewBitmap(R.id.widget_item_poster,result);
        Log.i(TAG,"Managed to set Bitmap for "+this.tconst);
    }
}
