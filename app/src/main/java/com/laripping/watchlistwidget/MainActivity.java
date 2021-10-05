package com.laripping.watchlistwidget;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import com.opencsv.CSVReader;


import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.laripping.watchlistwidget.databinding.ActivityMainBinding;
import com.opencsv.CSVReaderHeaderAware;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    // Request code for selecting a PDF document.
    private static final int PICK_CSV_FILE = 2;
    public static final String TAG = "WWMain";
    public static boolean loaded = false;
    public static int count = 0;

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private Throwable throwable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                openFile();
            }
        });
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

                loaded = true;
//                NavHostFragment.findNavController(SecondFragment.this).navigate(R.id.action_SecondFragment_to_FirstFragment);

                int countProvider = getTitleCount();

                Log.i(TAG,"File Parsed");
                Log.i(TAG,"\tCount by provider: "+countProvider);
                Log.i(TAG,"\tCount by var (and maybe Room): "+count);

                TextView text = findViewById(R.id.textview_first);
                text.setText("List opened! "+count+" titles");


            }
        }
    }

    private int getTitleCount() {
        Cursor cursor = this.getContentResolver().query(
                WatchlistProvider.CONTENT_URI,
                new String[]{Title.COLUMN_TITLE},
                null,
                new String[]{""},
                "_ID ASC");
        return cursor.getCount();
    }

    private void parseFileFromUri(Uri uri) throws IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream))
             )) {
            CsvUtils csv = new CsvUtils(this);
            csv.parseCsvFile(reader);


            AppDatabase database = AppDatabase.getInstance(this);
            database.titleDao().count()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(countRet -> {
                                count = countRet;
                                Log.i(TAG,"Count (Single): "+countRet);
                            },
                            throwable -> Log.e(TAG, "Unable to get count", throwable)
                            );
        }
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
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
}