package com.inin.gearphoneapp.app.pref;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.inin.gearphoneapp.app.R;

import java.net.InetAddress;

public class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener  {

    public static String KEY_PREF_SERVER = "pref_server";
    public static String KEY_PREF_SERVER_PORT = "pref_serverPort";
    public static String KEY_PREF_STATION_ID = "pref_stationIdentificationAddress";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        try{
        // Get preference
        Preference preference = findPreference(key);
        String value = sharedPreferences.getString(key, "");
        if (preference == null) return;

        // Determine which preference was changed
        if (key.equals(KEY_PREF_SERVER)) {
            preference.setSummary(value);
            Log.v("XXX", "SERVER CHANGED!!! -> " + value);
            //new IpLookupAsync().execute(value, KEY_PREF_SERVER);
        }
        else if (key.equals(KEY_PREF_SERVER_PORT)){
            preference.setSummary("Port " + value);
        }
        else if (key.equals(KEY_PREF_STATION_ID)){
            preference.setSummary(value);
        }
        } catch (Exception e){
            Log.e("XXX", "General error.", e);
        }
    }

    private class IpLookupAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                // Look up address
                InetAddress ipAddress = InetAddress.getByName(params[0]);
                Log.v("XXX", ipAddress.getHostAddress());
                return ipAddress.getHostAddress();
            } catch (Exception e){
                Log.e("XXX", "General error.", e);
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            // Update value
//            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
//            SharedPreferences.Editor editor1 = settings.edit();
//            editor1.putString(KEY_PREF_SERVER, result);
//            editor1.commit();

            // Set summary
            Preference preference = findPreference(KEY_PREF_SERVER);
            //EditTextPreference preference = (EditTextPreference) findPreference(KEY_PREF_SERVER);
            preference.setSummary(result);
            //preference.getEditor().putString(KEY_PREF_SERVER, result).commit();
        }
    }
}
