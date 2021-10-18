package com.laripping.watchlistwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.View;

import com.laripping.watchlistwidget.databinding.ActivityMainMonolithicBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements OnTaskCompleteListener {

    // Request code for selecting a PDF document.
    private static final int PICK_CSV_FILE = 2;
    public static final String TAG = "Main";


    private ActivityMainMonolithicBinding binding;
    private AppState mAppState;
    private TextView mText;
    private OnTaskCompleteListener mListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bind the toolbar
        binding = ActivityMainMonolithicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // Set the UI counter
        mAppState = new AppState(this);
        mText = findViewById(R.id.textview_first);
        mText.setText(mAppState.getStatus());

        // Bind the Floating Action Button
//        binding.fab.setOnClickListener(new View.OnClickListener() {
        findViewById(R.id.faboption1_import).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                openFile();
            }
        });
        findViewById(R.id.faboption2_url).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                DialogFragment dialog = new URLDialog();
                dialog.show(getSupportFragmentManager(), "urldialog");
                //  store URL and initParse
            }
        });

        // Get the SwipeRefreshLayout ref, to call setRefreshing() upon
        mSwipeRefreshLayout=findViewById(R.id.swiperefresh);
        if(mSwipeRefreshLayout==null){
            Log.e(TAG,"MSL null!");
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG,"onRefresh()");
                manualRefresh();
            }
        });

        // We implement the interface ourselves, to have the callback invoked when the task finishes
        try {
            this.mListener = (OnTaskCompleteListener)this;
        } catch(ClassCastException e){
            Log.e(TAG, "It looks like we haven't implemented the interface!");
        }

    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");      // TODO it's greyed out when text/csv

        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
//        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, PICK_CSV_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == PICK_CSV_FILE && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();

                try {
                    parseFileFromUri(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                updateCounterAndWidget();
            }
        }
    }


    private void parseFileFromUri(Uri uri) throws IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream))
             )) {
            CsvUtils csv = new CsvUtils(this);
            // TODO add spinner here
            csv.parseCsvFile(reader);

            Toast.makeText(this,
                    "Watchlist file parsed!",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void updateCounterAndWidget() {
        Log.d(TAG,"Updating UI Counter and Widget");
        mText.setText(mAppState.getStatus());

        AppWidgetManager mgr = AppWidgetManager.getInstance(this);
        ComponentName cn = new ComponentName(this, WatchlistWidget.class);
        mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.title_list);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Toast.makeText(this,
                    "No settings at this time!",
                    Toast.LENGTH_SHORT
            ).show();
            return true;
        } else if (id == R.id.action_clear) {
            int deleteCount = this.getContentResolver().delete(
                    WatchlistProvider.CONTENT_URI,
                    null,
                    null);
            StringBuilder toastBuilder = new StringBuilder().append("\u2713 All "+deleteCount+" titles cleared");

            if(mAppState.getListUrl()!=null){
                SharedPreferences.Editor editor = getSharedPreferences(AppState.PREF_FILE_NAME, Context.MODE_PRIVATE).edit();
                editor.remove(AppState.PREF_LIST_KEY);
                editor.commit();
                toastBuilder.append("\n\u2713 Not tracking IMDB list any more");
                // calling apply() would do this async, risking a race condition on the immediately upcoming
                // getState() -> Prefs.getString() which would show stale prefs. Let's see if that has an
                // impact on the main thread

                WorkManager.getInstance(this).cancelUniqueWork(RefreshWorker.WORK_NAME);
                toastBuilder.append("\n\u2713 Periodic watchlist refresh canceled");
            }

            Toast.makeText(this,
                    toastBuilder.toString(),
                    Toast.LENGTH_SHORT
            ).show();
            updateCounterAndWidget();
            return true;
        } else if(id==R.id.action_refresh){
            mSwipeRefreshLayout.setRefreshing(true);       // START
            manualRefresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        return NavigationUI.navigateUp(navController, appBarConfiguration)
//                || super.onSupportNavigateUp();
//    }
//
    /**
     * Make sure to update the counter in the UI whenever the activity is brought back into the foreground (e.g. after a dialog)
     */
    @Override
    protected void onResume() {
        Log.d(TAG,"onResume() - Updating UI Counter");
        mText.setText(mAppState.getStatus());
        super.onResume();
    }


    /**
     * Implement this to capture the signal emitted after the finish of the {@link ImdbListTask ImdbListTask}
     * @param success
     */
    @Override
    public void onComplete(boolean success) {
        if(success){
            Log.d(TAG,"onComplete() - succeeded! Updating Counter and Widget");
            updateCounterAndWidget();

            // schedule the periodic refresh of this list
            schedulePeriodicRefreshWorker();

        } else {
            Log.d(TAG,"onComplete() - failed! Not changing anything");
        }
    }

    private void schedulePeriodicRefreshWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)                  // only over Wifi
                .setRequiresBatteryNotLow(true)                                 // only when there's enough juice
                .build();
        PeriodicWorkRequest refreshWorkRequest = new PeriodicWorkRequest
                .Builder(RefreshWorker.class, 1, TimeUnit.DAYS)     // refresh watchlist once a day
                .setConstraints(constraints)
                .setInitialDelay(12, TimeUnit.HOURS)                    // it's fine if first refresh is after 12h
//                    .setBackoffCriteria(                                          // no retry conditions currently in-place
//                            BackoffPolicy.LINEAR,
//                            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
//                            TimeUnit.MILLISECONDS)
                .setInputData(                                                  // pass it the List URL - can't we get this from shared prefs in doWork()?
                        new Data.Builder()
                                .putString(RefreshWorker.DATA_KEY_LIST_URL, mAppState.getListUrl())
                                .build()
                )
                .build();
        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(                                     // make sure there's only one work scheduled at any time
                        RefreshWorker.WORK_NAME,
                        ExistingPeriodicWorkPolicy.REPLACE,                     // but if I mess this up, keep the latest one as active
                        refreshWorkRequest);
    }

    private void manualRefresh(){
        Toast.makeText(this,
                "Refreshing watchlist and widget...",
                Toast.LENGTH_SHORT
        ).show();

        boolean trackingList = mAppState.getListUrl()!=null;
        Log.d(TAG,"manualRefresh() - trackingList: "+trackingList);

        if(trackingList){
            new ImdbListTask(this,mListener,mSwipeRefreshLayout).execute();
        } else {        // basically a no-op. Just UI refresh
            updateCounterAndWidget();
            if(mSwipeRefreshLayout.isRefreshing()){
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}