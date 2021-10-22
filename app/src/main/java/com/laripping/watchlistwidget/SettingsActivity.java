package com.laripping.watchlistwidget;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
//            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }



    public static class SettingsFragment extends PreferenceFragmentCompat {
        private AppState mAppState;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            mAppState = new AppState(getContext());
            String listUrl = mAppState.getListUrl();
            boolean trackingList = (listUrl!=null);

            /**
             * "IMDB List Tracking" Prefs
             */
            Preference listPref = findPreference(getResources().getString(R.string.key_imdb_list));
            listPref.setEnabled( trackingList );
            // below assumes it's enabled
            listPref.setSummary( listUrl );        // TODO replace with List name
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(listUrl));
            listPref.setIntent(intent);

            EditTextPreference refPref = findPreference(getResources().getString(R.string.key_refresh_interval));
            refPref.setEnabled( trackingList );

            // below assumes it's enabled

            //TODO
            EditTextPreference lastrefPref = findPreference(getResources().getString(R.string.key_last_refresh));
            lastrefPref.setEnabled( trackingList );
            //TODO

            /**
             * "Others" Prefs
             */
            SwitchPreferenceCompat moviesPref = findPreference(getResources().getString(R.string.key_show_only_movies));
            //TODO
            EditTextPreference keyPref = findPreference(getResources().getString(R.string.key_api_key));
            //TODO

            /**
             * "About" Prefs
             */
            Preference versPref = findPreference(getResources().getString(R.string.key_version));
            // TODO set the version programmatically

        }
    }
}