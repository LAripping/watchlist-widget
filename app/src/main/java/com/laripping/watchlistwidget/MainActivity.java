package com.laripping.watchlistwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.laripping.watchlistwidget.databinding.ActivityMainMonolithicBinding;
import com.nambimobile.widgets.efab.ExpandableFab;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // Request code for selecting a PDF document.
    private static final int PICK_CSV_FILE = 2;
    public static final String TAG = "Main";

    private ActivityMainMonolithicBinding binding;
    private Counter mCounter;
    private TextView mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bind the toolbar
        binding = ActivityMainMonolithicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // Set the UI counter
        mCounter = new Counter(this,0);
        mCounter.setCount(getTitleCount());
        mText = findViewById(R.id.textview_first);
        mText.setText(mCounter.getText());

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
                // TODO prompt for the URL with a dialog - store URL and initParse
            }
        });
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
        Log.d(TAG,"Updating Counter and Widget");
        mCounter.setCount(getTitleCount());
        mText.setText(mCounter.getText());

        AppWidgetManager mgr = AppWidgetManager.getInstance(this);
        ComponentName cn = new ComponentName(this, WatchlistWidget.class);
        mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.title_list);
    }


    private int getTitleCount() {
        Cursor cursor = this.getContentResolver().query(
                WatchlistProvider.CONTENT_URI,
                AppDatabaseSqlite.ALL_COLUMNS,
                null,
                new String[]{""},
                "_ID ASC");
        int count = cursor.getCount();
        Log.i(TAG, "Cursor returned " + count+ " rows");
        return count;
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

        //noinspection SimplifiableIfStatement
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
            Toast.makeText(this,
                    "All "+deleteCount+" titles cleared!",
                    Toast.LENGTH_SHORT
            ).show();
            updateCounterAndWidget();
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
//    /**
//     * Make sure to update the counter in the UI after the file has bee
//     */
//    @Override
//    protected void onResume() {
//        Log.d(TAG,"onResume()");
//        mCounter.setCount(getTitleCount());
//        mText.setText(mCounter.getText());
//        super.onResume();
//    }
}