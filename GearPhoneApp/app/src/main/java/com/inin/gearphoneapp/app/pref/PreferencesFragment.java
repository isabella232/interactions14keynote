package com.inin.gearphoneapp.app.pref;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.inin.gearphoneapp.app.R;
import com.inin.gearphoneapp.app.Sip.HelperModel;

import java.net.InetAddress;

public class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener  {

    public static String KEY_PREF_SERVER = "pref_server";
    public static String KEY_PREF_SERVER_PORT = "pref_serverPort";
    public static String KEY_PREF_STATION_ID = "pref_stationIdentificationAddress";
    public static String KEY_PREF_SPEAKER_WHEN_ANSWER = "pref_speaker_when_answer";
    public static String KEY_PREF_AUTO_ANSWER = "pref_auto_answer";
    public static String KEY_PREF_START_CALLS_MUTED = "pref_start_calls_muted";
    public static String KEY_PREF_OPEN_CALL_CONTROLS_ON_ALERT = "pref_open_call_controls_on_alert";

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
            if (preference == null) return;

            // Determine which preference was changed
            if (key.equals(KEY_PREF_SERVER)) {
                String value = sharedPreferences.getString(key, "");
                preference.setSummary(value);
                Log.v(HelperModel.TAG, "SERVER CHANGED!!! -> " + value);
            }
            else if (key.equals(KEY_PREF_SERVER_PORT)){
                String value = sharedPreferences.getString(key, "");
                preference.setSummary("Port " + value);
            }
            else if (key.equals(KEY_PREF_STATION_ID)){
                String value = sharedPreferences.getString(key, "");
                preference.setSummary(value);
            }
        } catch (Exception e){
            Log.e(HelperModel.TAG, "General error.", e);
        }
    }

//    private class IpLookupAsync extends AsyncTask<String, Void, String> {
//
//        @Override
//        protected String doInBackground(String... params) {
//            try {
//                // Look up address
//                InetAddress ipAddress = InetAddress.getByName(params[0]);
//                Log.v(HelperModel.TAG, ipAddress.getHostAddress());
//                return ipAddress.getHostAddress();
//            } catch (Exception e){
//                Log.e(HelperModel.TAG, "General error.", e);
//            }
//            return "";
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            try {
//                // Set summary
//                Preference preference = findPreference(KEY_PREF_SERVER);
//                preference.setSummary(result);
//            } catch (Exception e){
//                Log.e(HelperModel.TAG, "General error.", e);
//            }
//        }
//    }
}
