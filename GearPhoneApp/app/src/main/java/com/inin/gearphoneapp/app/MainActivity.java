package com.inin.gearphoneapp.app;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAssignedNumbers;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.sip.SipAudioCall;
import android.net.sip.SipProfile;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.inin.gearphoneapp.app.icws.IcwsService;
import com.inin.gearphoneapp.app.plantronics.BluetoothHandler;
import com.inin.gearphoneapp.app.plantronics.PlantronicsReceiver;
import com.inin.gearphoneapp.app.pref.PreferencesActivity;
import com.inin.gearphoneapp.app.util.AppLog;
import com.inin.gearphoneapp.app.util.HelperModel;

public class MainActivity extends Activity {
    private GearAccessoryProviderService _service = null;
    private DrawerLayout mDrawerLayout;
    private BluetoothHandler btHandler = null;
    private PlantronicsReceiver btReceiver = null;

    private IntentFilter btIntentFilter = null;

    private static TextView _cicConnectionState;
    private static TextView _watchConnectionState;
    private static MainActivity _instance;

    public static void setCicConnectionState(final Boolean isConnected){
        _instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    _cicConnectionState.setText("CIC Connected");
                    _cicConnectionState.setTextColor(Color.GREEN);
                } else {
                    _cicConnectionState.setText("CIC NOT Connected");
                    _cicConnectionState.setTextColor(Color.RED);
                }
            }

        });

    }

    public static void setWatchConnectionState(final Boolean isConnected){
        _instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    _watchConnectionState.setText("Watch Connected");
                    _watchConnectionState.setTextColor(Color.GREEN);
                } else {
                    _watchConnectionState.setText("Watch NOT Connected");
                    _watchConnectionState.setTextColor(Color.RED);
                }
            }
            });
    }

    public static MainActivity getInstance(){
        return _instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            Log.v(HelperModel.TAG_MAIN, "onCreate");

            //TODO: This is here for debug purposes to keep the screen on. Remove for published builds.
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            //  _service = new HelloAccessoryProviderService();
            AppLog.init((TextView) findViewById(R.id.logText));


            _watchConnectionState = (TextView)findViewById(R.id.txtWatchConnection);
            _cicConnectionState = (TextView)findViewById(R.id.txtCicConnection);
            _instance = this;

            setWatchConnectionState(false);
            setCicConnectionState(false);

            Button btnFakeAlert = ((Button)findViewById(R.id.btnFakeAlert));
            btnFakeAlert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        GearAccessoryProviderService.instance.newAlert("Negative Sentiment", "Something else", "Agent: Kevin Glinski","1234");
                    } catch (Exception e){
                        Log.e(HelperModel.TAG_MAIN, "General error", e);
                    }
                }
            });


            Button btnClearAlert = ((Button)findViewById(R.id.btnClearAlert));
            btnClearAlert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        GearAccessoryProviderService.instance.clearAlerts();
                    } catch (Exception e){
                        Log.e(HelperModel.TAG_MAIN, "General error", e);
                    }
                }
            });

            Button btnNewCall = ((Button)findViewById(R.id.btnNewCall));
            btnNewCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        GearAccessoryProviderService.instance.newCall("Bob Loblaw", "(317) 222-2222", true, "1234");
                    } catch (Exception e){
                        Log.e(HelperModel.TAG_MAIN, "General error", e);
                    }
                }
            });

            Button btnConnect = ((Button)findViewById(R.id.btnConnect));
            btnConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        TelephonyManager phoneManager = (TelephonyManager)
                                getApplicationContext().getSystemService(TELEPHONY_SERVICE);
                        String phoneNumber = phoneManager.getLine1Number();

                       // phoneManager.a

                        IcwsService.instance.connect(phoneNumber);
                    } catch (Exception e){
                        Log.e(HelperModel.TAG_MAIN, "General error", e);
                    }
                }
            });

            // Start ICWS service
            Intent intent = new Intent(this, IcwsService.class);
            startService(intent);

            // Start Gear Accessory service
            intent = new Intent(this, GearAccessoryProviderService.class);
            startService(intent);

            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PHONE_STATE");
            initBluetooth();



        } catch (Exception e){
            Log.e(HelperModel.TAG_MAIN, "Error in onCreate.", e);
        }
    }

    private void initBluetooth() {
        btHandler = new BluetoothHandler();
        btReceiver = new PlantronicsReceiver(btHandler);

        btIntentFilter = new IntentFilter();
        btIntentFilter.addCategory(
                BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_COMPANY_ID_CATEGORY + "." +
                        BluetoothAssignedNumbers.PLANTRONICS);
        btIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        btIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        btIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        btIntentFilter.addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT);
        btIntentFilter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        btIntentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);

        registerReceiver(btReceiver, btIntentFilter);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try{
            Log.v(HelperModel.TAG_MAIN, "onConfigurationChanged");

        } catch (Exception e){
            Log.e(HelperModel.TAG_MAIN, "Error in onConfigurationChanged.", e);
        }
    }

    @Override
    public void onStart(){
        try{
            Log.v(HelperModel.TAG_MAIN, "onStart");
            super.onStart();

        } catch (Exception e){
            Log.e(HelperModel.TAG_MAIN, "Error in onStart.", e);
        }
    }

    @Override
    public void onStop(){
        try{
            Log.v(HelperModel.TAG_MAIN, "onStop");
            super.onStop();

        } catch (Exception e){
            Log.e(HelperModel.TAG_MAIN, "Error in onStop.", e);
        }
    }

    @Override
    public void onDestroy() {
        try{
            Log.v(HelperModel.TAG_MAIN, "onDestroy");


            // SUPER DESTROY!!!!
            super.onDestroy();
        } catch (Exception e){
            Log.e(HelperModel.TAG_MAIN, "Error in onDestroy.", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
