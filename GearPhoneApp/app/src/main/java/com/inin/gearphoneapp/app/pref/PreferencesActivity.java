package com.inin.gearphoneapp.app.pref;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;


public class PreferencesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PreferencesFragment())
                .commit();

        //TODO: This is here for debug purposes to keep the phone awake. Remove for published builds
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
