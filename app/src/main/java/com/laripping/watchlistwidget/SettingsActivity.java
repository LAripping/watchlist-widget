package com.laripping.watchlistwidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.work.WorkManager;

public class SettingsActivity extends AppCompatActivity {
    public static final String SETTING_KEY_IVAL = "RefreshInterval";
    public static final int DEFAULT_IVAL = 1;
    private static String TAG = "Settings";

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
            String listTitle = mAppState.getListTitle();
            if( (listUrl==null) ) {
                findPreference(getResources().getString(R.string.key_imdb_list)).setEnabled(false);
                findPreference(getResources().getString(R.string.key_refresh_interval)).setEnabled(false);
                findPreference(getResources().getString(R.string.key_last_refresh)).setEnabled(false);
            } else {
                /**
                 * "IMDB List Tracking" Prefs
                 */
                Preference listPref = findPreference(getResources().getString(R.string.key_imdb_list));
                listPref.setSummary( listTitle );
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(listUrl));
                listPref.setIntent(intent);

                EditTextPreference refPref = findPreference(getResources().getString(R.string.key_refresh_interval));
                refPref.setDefaultValue(Integer.toString(DEFAULT_IVAL));
                refPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    /**
                     * Update the refresh worker with the new interval
                     * @param preference
                     * @param newValue
                     * @return
                     */
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        RefreshWorker.schedulePeriodicRefreshWorker(
                                mAppState,getContext(),
                                Integer.parseInt( (String)newValue )
                        );
                        return true;
                    }
                });
                refPref.setSummaryProvider(new Preference.SummaryProvider<EditTextPreference>() {
                    /**
                     * Overriding to add the " hours" postfix to the value (int as string) as a readable summary
                     * @param preference
                     * @return
                     */
                    @Override
                    public CharSequence provideSummary(EditTextPreference preference) {
                        String text = preference.getText();
                        if (TextUtils.isEmpty(text)){
                            return "NOT SET";
                        }
                        return text + " hours";
                    }
                });
                refPref.setOnBindEditTextListener( new EditTextPreference.OnBindEditTextListener() {
                    /**
                     * Overriding to make the input numeric
                     * @param editText
                     */
                    @Override
                    public void onBindEditText(@NonNull EditText editText) {
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    }
                });

                Preference lastrefPref = findPreference(getResources().getString(R.string.key_last_refresh));
//            LastRefreshDataStore lrds = new LastRefreshDataStore(getContext());
//            lastrefPref.setPreferenceDataStore(lrds);       // not sure this works, so let's make it manual
                String defaultSum = getResources().getString(R.string.no_refreshes_yet);
                lastrefPref.setDefaultValue(defaultSum);
                String summary = getContext()
                        .getSharedPreferences(AppState.PREF_FILE_NAME, Context.MODE_PRIVATE)
                        .getString(
                                AppState.PREF_REFRESH_KEY,
                                defaultSum
                        );
                lastrefPref.setSummary(summary);


            }



            /**
             * "Others" Prefs
             */
            SwitchPreferenceCompat moviesPref = findPreference(getResources().getString(R.string.key_show_only_movies));
            moviesPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                /**
                 * Signal the update to any widgets so that they can re-populate their cursors
                 * through an invocation of their onDataSetChanged()
                 * @param preference
                 * @param newValue
                 * @return
                 */
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    AppWidgetManager mgr = AppWidgetManager.getInstance(getContext());
                    ComponentName cn = new ComponentName(getContext(), WatchlistWidget.class);
                    mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.title_list);
                    Log.d(TAG,"onlyMovies.onPrefChanged() -> widget signalled");
                    return true;
                }
            });

            EditTextPreference keyPref = findPreference(getResources().getString(R.string.key_api_key));
            String apikey = getContext()
                    .getSharedPreferences(AppState.PREF_FILE_NAME,Context.MODE_PRIVATE)
                    .getString(
                            AppState.PREF_APIKEY_KEY,
                            null
                    );
            String yesButMasked = getResources().getString(R.string.configured);
            keyPref.setSummary(  apikey==null?"No API key specified!": yesButMasked );
            keyPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences.Editor editor = getContext()
                            .getSharedPreferences(AppState.PREF_FILE_NAME, Context.MODE_PRIVATE)
                            .edit();
                    editor.putString(AppState.PREF_APIKEY_KEY,newValue.toString());
                    editor.apply();

                    AppWidgetManager mgr = AppWidgetManager.getInstance(getContext());
                    ComponentName cn = new ComponentName(getContext(), WatchlistWidget.class);
                    mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.title_list);
                    Log.d(TAG,"apikey.onPrefChanged() -> widget signalled");

                    preference.setSummary(yesButMasked);
                    return true;
                }
            });
            keyPref.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                /**
                 * Overriding to show the API key once the dialog is open
                 * @param editText
                 */
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setText(apikey);
                }
            });

            /**
             * "About" Prefs
             */
            Preference versPref = findPreference(getResources().getString(R.string.key_version));
            // TODO set the version programmatically

        }
    }

    /**
     * Custom datastore for the "Last Auto-Refresh" preference
     * to override the default SharedPreferences data store
     * ...with another Sharedpreferences-backed datastore
     * - one that's editable and  whose keys and names we control though
     */
    public static class LastRefreshDataStore extends PreferenceDataStore {
        private static final String TAG = "LastRefresh";
        private Context mContext;

        public LastRefreshDataStore(Context ctx){
            this.mContext = ctx;
        }

        @Override
        @Nullable
        public String getString(String key, @Nullable String defValue) {
            Log.d(TAG,"getString()");       // never called
            return mContext
                    .getSharedPreferences(AppState.PREF_FILE_NAME, Context.MODE_PRIVATE)
                    .getString(
                            AppState.PREF_REFRESH_KEY,
                            "DEF"
                    );
        }
    }
}