package com.laripping.watchlistwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.laripping.watchlistwidget.databinding.WatchlistWidgetConfigureBinding;

import java.io.IOException;

/**
 * The configuration screen for the {@link WatchlistWidget WatchlistWidget} AppWidget.
 */
public class WatchlistWidgetConfigureActivity extends FragmentActivity implements OnTaskCompleteListener {
    private static final int PICK_CSV_FILE = 2;
    private static final String TAG = "WWConfigActivity";
    private WatchlistWidgetConfigureBinding binding;
    private CsvUtils mCsvUtils;
    private boolean listFromUl;
    private int mAppWidgetId;
    private AppState mAppState;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.d(TAG,"onCreate()");

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            Log.d(TAG,"AppWidgetId passed to ConfigActivity: "+mAppWidgetId);
        }

        mAppState = new AppState(this);
        int count = mAppState.getTitleCount();
        Log.i(TAG, "AppState reports " + count+ " rows");

        if(count>0){                // no need for config at all, user has already imported titles
            setResult(RESULT_OK);
            finish();
        }                           // else, setup the radioview and button handlers

        binding = WatchlistWidgetConfigureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mCsvUtils = new CsvUtils(this);

        findViewById(R.id.config_cancel).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        findViewById(R.id.config_ok).setOnClickListener(v -> {
            if(listFromUl){
                new URLDialog().show(getSupportFragmentManager(),"urldialog");
            } else {        // from file
                mCsvUtils.openFilePicker();
            }
//            setResult(RESULT_OK);
//            finish();
        });

//        mAppWidgetText = binding.appwidgetText;
//        binding.addButton.setOnClickListener(mOnClickListener);
//
//        // Find the widget id from the intent.
//        Intent intent = getIntent();
//        Bundle extras = intent.getExtras();
//        if (extras != null) {
//            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
//            Log.d(TAG,"AppWidgetId passed to Config Activity: "+mAppWidgetId);
//        }
//
//        // If this activity was started with an intent without an app widget ID, finish with an error.
//        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
//            Log.e(TAG,"Invalid AppWidgetId passed to Config Activity!");
//            finish();
//            return;
//        }
//
//        mAppWidgetText.setText(loadTitlePref(WatchlistWidgetConfigureActivity.this, mAppWidgetId));
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_file:
                if (checked)
                    listFromUl = false;
                    break;
            case R.id.radio_url:
                if (checked)
                    listFromUl = true;
                    break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        Log.d(TAG,String.format("onActivityResult(reqCode=%d, result=%d)",requestCode,resultCode));

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

                // build remoteviews for the widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                WatchlistWidget.initWidget(this, appWidgetManager, mAppWidgetId);

                setResult(RESULT_OK);
                finish();
            }
        }
    }

    @Override
    public void onComplete(boolean success) {
        if(success){
            Log.d(TAG,"onComplete() - succeeded! Updating Counter and Widget");

            // build remoteviews for the widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            WatchlistWidget.initWidget(this, appWidgetManager, mAppWidgetId);

            // schedule the periodic refresh of this list
            RefreshWorker.schedulePeriodicRefreshWorker(mAppState,this);

            setResult(RESULT_OK);
            finish();
        } else {
            Log.d(TAG,"onComplete() - failed! Not changing anything");
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}