package com.tsaou.watchlistwidget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.toString();
    private ListFileReader listFileReader = new ListFileReader(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Inside onCreate()");


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listFileReader.performFileSearch();
                Snackbar.make(view, "Watchlist Added!", Snackbar.LENGTH_LONG);
            }
        });
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
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultIntent) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == ListFileReader.READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultIntent.getData().
            Uri uri = null;
            String content = null;
            if (resultIntent != null) {
                uri = resultIntent.getData();
                Log.i(TAG, "Uri: " + uri.toString());

                if( !listFileReader.checkCsv(uri)){
                    Log.i(TAG, "Not a CSV file!");
                    // TODO popup error
                    return;
                }
                try {
                     content = listFileReader.readTextFromUri(uri);
                } catch (IOException e) {
                    Log.i(TAG, "IOException occured in file content reading: " + e.getMessage() );
                    e.printStackTrace();
                }
                Log.i(TAG, "File contents:\n" + content );
                // TODO simple CSV parser using split()
            }
        }
    }



}
