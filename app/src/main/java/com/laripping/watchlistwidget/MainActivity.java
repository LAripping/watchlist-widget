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
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.WorkManager;

import android.util.Log;

import com.laripping.watchlistwidget.databinding.ActivityMainMonolithicBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements OnTaskCompleteListener {

    public static final int PICK_CSV_FILE = 2;
    public static final String TAG = "Main";

    private ActivityMainMonolithicBinding binding;
    private AppState mAppState;
    private TextView mText;
    private OnTaskCompleteListener mListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CsvUtils mCsvUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCsvUtils = new CsvUtils(this);

        // Bind the toolbar
        binding = ActivityMainMonolithicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // Set the UI counter
        mAppState = new AppState(this);
        mText = findViewById(R.id.textview_first);
        mText.setText(mAppState.getStatus());

        // Bind the Floating Action Button options
        findViewById(R.id.faboption1_import).setOnClickListener(view -> mCsvUtils.openFilePicker());
        findViewById(R.id.faboption2_url).setOnClickListener(view -> new URLDialog().show(getSupportFragmentManager(), "urldialog"));

        // Get the SwipeRefreshLayout ref, to call setRefreshing() upon
        mSwipeRefreshLayout=findViewById(R.id.swiperefresh);
        if(mSwipeRefreshLayout==null){
            Log.e(TAG,"MSL null!");
        }
        mSwipeRefreshLayout.setOnRefreshListener(() -> manualRefresh());

        // We implement the interface ourselves, to have the callback invoked when the task finishes
        try {
            this.mListener = (OnTaskCompleteListener)this;
        } catch(ClassCastException e){
            Log.e(TAG, "It looks like we haven't implemented the interface!");
        }

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
                    mCsvUtils.parseFileFromUri(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                updateCounterAndWidget();
            }
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
//            Toast.makeText(this,
//                    "No settings at this time!",
//                    Toast.LENGTH_SHORT
//            ).show();
            startActivity(new Intent(MainActivity.this,SettingsActivity.class));
            return true;
        } else if (id == R.id.action_clear) {
            clearAll();
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

            // get the refresh interval
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            int refreshIvalHrs = sharedPreferences.getInt(
                    SettingsActivity.SETTING_KEY_IVAL,
                    SettingsActivity.DEFAULT_IVAL
            );
            // schedule the periodic refresh of this list
            RefreshWorker.schedulePeriodicRefreshWorker(mAppState, this, refreshIvalHrs);

        } else {
            Log.d(TAG,"onComplete() - failed! Not changing anything");
        }
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


    private void clearAll() {
        int deleteCount = this.getContentResolver().delete(
                WatchlistProvider.CONTENT_URI,
                null,
                null);
        StringBuilder toastBuilder = new StringBuilder().append("\u2713 All "+deleteCount+" titles cleared");

        if(mAppState.getListUrl()!=null){
            SharedPreferences.Editor editor = getSharedPreferences(AppState.PREF_FILE_NAME, Context.MODE_PRIVATE).edit();
            editor.remove(AppState.PREF_LIST_KEY);
            editor.remove(AppState.PREF_LIST_NAME);
            editor.remove(AppState.PREF_REFRESH_KEY);
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
    }
}