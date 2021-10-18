package com.laripping.watchlistwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Used to update the local watchlist when initially pointing to an IMDB list
 *
 * It's working alright and currently convenient,
 * but the code's duplicated in the {@link RefreshWorker WorkerThread}
 * so TODO try replacing with an immediate Worker to re-use same code
 */
public class ImdbListTask extends AsyncTask<Void,Integer,Integer> {
    private Context mContext;
    private OnTaskCompleteListener mListener;
    private OkHttpClient mClient;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private static final String TAG = "ImdbListTask";
    private static final String faq = "https://help.imdb.com/article/imdb/track-movies-tv/watchlist-faq/G9PA556494DM8YBA";

    private static final int RESULT_CODE_SUCCESS = 0;
    private static final int RESULT_CODE_NOT_PUBLIC = 1;
    private static final int RESULT_CODE_UNKNOWN = 2;
    private static final int RESULT_CODE_NETWORK_ERROR = 3;
    private static final int RESULT_CODE_CSV_PARSER = 4;
//
//    public static final int TASK_CODE_INIT = 0;
//    public static final int TASK_CODE_REFRESH = 1;


    public ImdbListTask(Context context, OnTaskCompleteListener listener){
        mContext = context;
        mClient = new OkHttpClient();
        mListener = listener;
    }

    public ImdbListTask(Context context, OnTaskCompleteListener listener, SwipeRefreshLayout srl) {
        mContext = context;
        mClient = new OkHttpClient();
        mListener = listener;
        mSwipeRefreshLayout = srl;
    }


    /**
     *
     * Check if list is public. If not show prompt
     * Download as CSV from the "/export" endpoint
     * Parse CSV and update Provider
     *
     */
    @Override
    protected Integer doInBackground(Void... params) {

            String listUrl = mContext
                    .getSharedPreferences(AppState.PREF_FILE_NAME, Context.MODE_PRIVATE)
                    .getString(AppState.PREF_LIST_KEY, null);
            if (listUrl == null) {
                Log.e(TAG, "Whoops! listUrl from sharedprefs is null!");
                return RESULT_CODE_UNKNOWN;         // This shouldn't happen... Let's investigate
            }

            Log.d(TAG, "Processing list URL: " + listUrl);
            Request listRequest = null;
            Request exportRequest = null;
            Response listResponse = null;
            Response exportResponse = null;

            try {
                // TODO ensure the URL is of proper format, without trailing GET params
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
                    Log.e(TAG, String.format("Request for %s returned response code %d!", listUrl, listResponse.code()));
                    return RESULT_CODE_UNKNOWN;     // This shouldn't happen... Let's investigate
                }
                String body = listResponse.body().string();
                if (body.contains("<title>IMDb</title>")) {
                    // List Non-Public
                    return RESULT_CODE_NOT_PUBLIC;
                }
                publishProgress(25);  // leads to an invocation of onProgressUpdate()

                // Fetch the list's CSV version
                Log.d(TAG, "Requesting the /export endpoint...");
                exportRequest = new Request.Builder()
                        .url(listUrl + "/export")
                        .get()
                        .build();
                exportResponse = mClient.newCall(exportRequest).execute();
                if (exportResponse.code() != 200) {
                    Log.e(TAG, String.format("Export request returned response code %d!", exportResponse.code()));
                    return RESULT_CODE_UNKNOWN;

                }
            } catch (IOException e) {
                e.printStackTrace();
                return RESULT_CODE_NETWORK_ERROR;
            }
            publishProgress(60);  // leads to an invocation of onProgressUpdate()

            try {
                // Parse CSV file using old tricks
                InputStream inputStream = exportResponse.body().byteStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                CsvUtils csv = new CsvUtils(mContext);
                csv.parseCsvFile(reader);       // encapsulates the provider.insert()
            } catch (IOException e) {
                e.printStackTrace();
                return RESULT_CODE_CSV_PARSER;
            }

            Log.i(TAG,"doInBackground() finished with no errors");
            publishProgress(100);  // leads to an invocation of onProgressUpdate()
            return RESULT_CODE_SUCCESS;

    }

    protected void onProgressUpdate(Integer... progress) {
            // TODO update Spinner/notification with progress[0] (0-100%)
    }


    @Override
    protected void onPostExecute(Integer resCode) {
            switch(resCode){
                case RESULT_CODE_SUCCESS:
                    Toast.makeText(mContext,
                            "IMDB list parsed!",
                            Toast.LENGTH_SHORT
                    ).show();

                    // Signal the change back to MainActivity
                    mListener.onComplete(true);

                    // Update the widget
                    AppWidgetManager mgr = AppWidgetManager.getInstance(mContext);
                    ComponentName cn = new ComponentName(mContext, WatchlistWidget.class);
                    mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.title_list);
                    break;
                case RESULT_CODE_NOT_PUBLIC:
                    // make the link clickable
                    LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                    View inflatedView = inflater.inflate(R.layout.dialog_private, null);
                    TextView linkTextView = (TextView) inflatedView.findViewById(R.id.link);
                    linkTextView.setMovementMethod(LinkMovementMethod.getInstance());
                    linkTextView.setLinkTextColor(Color.BLUE);

                    new AlertDialog.Builder(mContext)
                            .setTitle("List not public")
                            .setView(inflatedView)
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Do we need anything else?
                                }})
                            .create()
                            .show();
                    break;
                case RESULT_CODE_NETWORK_ERROR:
                    new AlertDialog.Builder(mContext)
                            .setTitle("No internet")
                            .setMessage("Check your network connectivity and try again")
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Do we need anything else?
                                }
                            })
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    new ImdbListTask(mContext, mListener).execute();
                                }})
                            .create()
                            .show();
                    break;

                case RESULT_CODE_UNKNOWN:
                    new AlertDialog.Builder(mContext)
                            .setTitle("Something went wrong")
                            .setMessage("An unexpected response was received from IMDB")
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Do we need anything else?
                                }})
                            .create()
                            .show();
                    break;

                case RESULT_CODE_CSV_PARSER:
                    new AlertDialog.Builder(mContext)
                            .setTitle("Couldn't read list")
                            .setMessage("Import list as a downloaded CSV file instead?")
                            .setNeutralButton("Download", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String exportUrl = mContext
                                            .getSharedPreferences(AppState.PREF_FILE_NAME,Context.MODE_PRIVATE)
                                            .getString(AppState.PREF_LIST_KEY,null)+"/export";
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse(exportUrl));
                                    mContext.startActivity(i);
                                }
                            })
                            .create()
                            .show();
                    break;
            }
        if(mSwipeRefreshLayout==null) {
            Log.d(TAG, "No Refresh layouts involved");
        } else {
            if(! mSwipeRefreshLayout.isRefreshing()){
                Log.d(TAG,"Was not refreshing");
            } else {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}
