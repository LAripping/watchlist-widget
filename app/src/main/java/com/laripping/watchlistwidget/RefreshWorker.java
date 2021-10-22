package com.laripping.watchlistwidget;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RefreshWorker extends Worker {

    public static final String DATA_KEY_LIST_URL = "LIST_URL";
    private static final String DATA_KEY_PROGRESS = "PROGRESS";
    private static final String TAG = "RefreshWorker";
    public static final String WORK_NAME = "refreshList";
    private Context mContext;

    public RefreshWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        mContext = context;
        Log.d(TAG,"ctor - System decided it's time for a watchlist update!");
    }

    @Override
    public Result doWork() {
        Log.d(TAG,"doWork()");
        setProgressAsync(new Data.Builder().putInt(DATA_KEY_PROGRESS, 0).build());

        String listUrl = getInputData().getString(DATA_KEY_LIST_URL);
        if(listUrl == null) {
            Log.e(TAG,"Woops! AppState returned null list URL! Investigate!");
            return Result.failure();
        }
        Log.d(TAG, "Processing list URL: " + listUrl);
        setProgressAsync(new Data.Builder().putInt(DATA_KEY_PROGRESS, 10).build());

        OkHttpClient mClient = new OkHttpClient();
        Request listRequest = null;
        Request exportRequest = null;
        Response listResponse = null;
        Response exportResponse = null;

        try {
            // Check URL format and normalise
            listUrl = URLDialog.checkImdbListUrl(listUrl);
            if (listUrl == null) return Result.failure();

            // Check if list is public
            /** Cheeky difference between Public and Private list
             * In [1]: import requests
             * In [2]: resp = requests.get('https://www.imdb.com/list/ls075069544/')
             * In [3]: "<title>IMDb</title>" in resp.text
             * Out[3]: True
             * In [4]: resp2 = requests.get('https://www.imdb.com/list/ls075069551/')
             * In [5]: "<title>IMDb</title>" in resp2.text
             * Out[5]: False
             */
            listRequest = new Request.Builder()
                    .url(listUrl)
                    .get()
                    .build();
            listResponse = mClient.newCall(listRequest).execute();
            if (listResponse.code() != 200) {
                android.util.Log.e(TAG, String.format("Request for %s returned response code %d!", listUrl, listResponse.code()));
                return Result.failure();
            }
            String body = listResponse.body().string();
            if (body.contains("<title>IMDb</title>")) {
                // List Non-Public
                return Result.failure();
            }
            setProgressAsync(new Data.Builder().putInt(DATA_KEY_PROGRESS, 25).build());

            // Fetch the list's CSV version
            Log.d(TAG, "Requesting the /export endpoint...");
            exportRequest = new Request.Builder()
                    .url(listUrl + "/export")
                    .get()
                    .build();
            exportResponse = mClient.newCall(exportRequest).execute();
            if (exportResponse.code() != 200) {
                Log.e(TAG, String.format("Export request returned response code %d!", exportResponse.code()));
                return Result.failure();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
        setProgressAsync(new Data.Builder().putInt(DATA_KEY_PROGRESS, 60).build());

        try {
            // Parse CSV file using old tricks
            InputStream inputStream = exportResponse.body().byteStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            CsvUtils csv = new CsvUtils(mContext);
            csv.parseCsvFile(reader);       // encapsulates the provider.insert()
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }

        Log.d(TAG,"finished successfully, exiting");
        setProgressAsync(new Data.Builder().putInt(DATA_KEY_PROGRESS, 100).build());
        return Result.success();
//            return Result.retry();    no retry conditions currently in-place
    }


    @Override
    public void onStopped() {
        Log.d(TAG,"onStopped()");
        // cancel the asynctask
        super.onStopped();
    }

    public static void schedulePeriodicRefreshWorker(AppState appState, Context activityContext) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)                  // only over Wifi
                .setRequiresBatteryNotLow(true)                                 // only when there's enough juice
                .build();
        int refreshIvalHrs = 1;                                                 // TODO parameterize : persist the Pref value and get from there programmatically
        PeriodicWorkRequest refreshWorkRequest = new PeriodicWorkRequest
                .Builder(RefreshWorker.class, refreshIvalHrs, TimeUnit.HOURS)   // refresh watchlist once a day
                .setConstraints(constraints)
                .setInitialDelay(refreshIvalHrs, TimeUnit.HOURS)                // spend a whole interval and then fire
//                    .setBackoffCriteria(                                          // no retry conditions currently in-place
//                            BackoffPolicy.LINEAR,
//                            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
//                            TimeUnit.MILLISECONDS)
                .setInputData(                                                  // pass it the List URL - can't we get this from shared prefs in doWork()?
                        new Data.Builder()
                                .putString(RefreshWorker.DATA_KEY_LIST_URL, appState.getListUrl())
                                .build()
                )
                .build();
        WorkManager.getInstance(activityContext)
                .enqueueUniquePeriodicWork(                                     // make sure there's only one work scheduled at any time
                        RefreshWorker.WORK_NAME,
                        ExistingPeriodicWorkPolicy.REPLACE,                     // but if I mess this up, keep the latest one as active
                        refreshWorkRequest);
    }
}
