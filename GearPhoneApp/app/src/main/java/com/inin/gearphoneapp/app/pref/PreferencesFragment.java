package com.inin.gearphoneapp.app.pref;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.inin.gearphoneapp.app.R;
import com.inin.gearphoneapp.app.util.HelperModel;

public class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener  {

    public static String KEY_PREF_CONNECTION_SERVER = "pref_connection_server";
    public static String KEY_PREF_CONNECTION_STATION_PORT = "pref_connection_station_port";
    public static String KEY_PREF_CONNECTION_STATION_ID = "pref_connection_station_identification_address";
    public static String KEY_PREF_CONNECTION_SERVER_USERNAME = "pref_connection_server_username";
    public static String KEY_PREF_CONNECTION_SERVER_PASSWORD = "pref_connection_server_password";
    public static String KEY_PREF_CONNECTION_ICWS_PORT = "pref_connection_icws_port";
    public static String KEY_PREF_CALL_SPEAKER_WHEN_ANSWER = "pref_call_speaker_when_answer";
    public static String KEY_PREF_CALL_AUTO_ANSWER = "pref_call_auto_answer";
    public static String KEY_PREF_CALL_START_CALLS_MUTED = "pref_call_start_calls_muted";
    public static String KEY_PREF_CALL_OPEN_CALL_CONTROLS_ON_ALERT = "pref_call_open_call_controls_on_alert";
    public static String KEY_PREF_ALERTS_QUEUE = "pref_alerts_queue";

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
            if (key.equals(KEY_PREF_CONNECTION_SERVER) ||
                    key.equals(KEY_PREF_CONNECTION_STATION_PORT) ||
                    key.equals(KEY_PREF_CONNECTION_STATION_ID) ||
                    key.equals(KEY_PREF_CONNECTION_SERVER_USERNAME) ||
                    key.equals(KEY_PREF_ALERTS_QUEUE) ||
                    key.equals(KEY_PREF_CONNECTION_ICWS_PORT)) {
                String value = sharedPreferences.getString(key, "");
                preference.setSummary(value);
            }
        } catch (Exception e){
            Log.e(HelperModel.TAG, "General error.", e);
        }
    }
}
