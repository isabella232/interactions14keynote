package com.inin.gearphoneapp.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.sip.SipAudioCall;
import android.net.sip.SipProfile;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import 	android.content.Intent;

import com.inin.gearphoneapp.app.Sip.HelperModel;
import com.inin.gearphoneapp.app.Sip.SipModel;
import com.inin.gearphoneapp.app.icws.IcwsService;

import com.inin.gearphoneapp.app.pref.PreferencesActivity;
import com.inin.gearphoneapp.app.util.AppLog;
import com.inin.gearphoneapp.app.util.PhoneMenuArrayAdapter;

public class MainActivity extends Activity {
    private GearAccessoryProviderService _service = null;
    private SipModel _sipModel = null;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

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


            Button btnFakeAlert = ((Button)findViewById(R.id.btnFakeAlert));

            btnFakeAlert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GearAccessoryProviderService.instance.newAlert("Negative Sentiment", "Something else", "Agent: Kevin Glinski","1234");
                }
            });


            Button btnClearAlert = ((Button)findViewById(R.id.btnClearAlert));

            btnClearAlert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GearAccessoryProviderService.instance.clearAlerts();
                }
            });

            Button btnNewCall = ((Button)findViewById(R.id.btnNewCall));

            btnNewCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GearAccessoryProviderService.instance.newCall("Bob Loblaw", "(317) 222-2222", true, "1234");
                }
            });

            Button btnConnect = ((Button)findViewById(R.id.btnConnect));

            btnConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IcwsService.instance.connect();
                }
            });

            Intent intent = new Intent(this, IcwsService.class);
            startService(intent);

            // Initialize drawer menu
            String[] items = {"Answer","Mute","Hold","Speaker","Disconnect","Test call"};
            ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
            mDrawerList.setAdapter(new PhoneMenuArrayAdapter(this, items));

            // Set list item click listener
            mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

            // Set listener for drawer open/close
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

                /** Called when a drawer has settled in a completely closed state. */
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    //getActionBar().setTitle(mTitle);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                /** Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    //getActionBar().setTitle(mDrawerTitle);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                    updateCallStatus(_sipModel.getCall());
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);

            // Get/initialize the model
            _sipModel = SipModel.GetInstance(this);
        } catch (Exception e){
            Log.e(HelperModel.TAG_MAIN, "Error in onCreate.", e);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        try{
            // Sync the toggle state after onRestoreInstanceState has occurred.
            mDrawerToggle.syncState();
        } catch (Exception e){
            Log.e(HelperModel.TAG_MAIN, "Error in onPostCreate.", e);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try{
            Log.v(HelperModel.TAG_MAIN, "onConfigurationChanged");
            mDrawerToggle.onConfigurationChanged(newConfig);
        } catch (Exception e){
            Log.e(HelperModel.TAG_MAIN, "Error in onConfigurationChanged.", e);
        }
    }

    @Override
    public void onStart(){
        try{
            Log.v(HelperModel.TAG_MAIN, "onStart");
            super.onStart();

            // Register the call receiver intent to THIS ui
            _sipModel.registerCallReceiver();

            // Update UI with current info
            updateStationState();
            updateCallStatus(_sipModel.getCall());
        } catch (Exception e){
            Log.e(HelperModel.TAG_MAIN, "Error in onStart.", e);
        }
    }

    @Override
    public void onStop(){
        try{
            Log.v(HelperModel.TAG_MAIN, "onStop");
            super.onStop();

            // Unregister the call receiver intent from the UI before it is destroyed
            _sipModel.unregisterCallReceiver();
        } catch (Exception e){
            Log.e(HelperModel.TAG_MAIN, "Error in onStop.", e);
        }
    }

    @Override
    public void onDestroy() {
        try{
            Log.v(HelperModel.TAG_MAIN, "onDestroy");

            //TODO: Figure out how to deregister when the app is actually exited -- this is called in multiple scenarios that are not exiting the app
            // Force deregistration
            //_sipModel.registerSipProfile(true);

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
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

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




    public void registerStation(View view){
        try{
            _sipModel.registerSipProfile(false);
        } catch (Exception e){
            Log.e(HelperModel.TAG_MAIN, "Error registering station.", e);
        }
    }

    public class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            try{
                switch (position){
                    case 0:{
                        _sipModel.callAnswer();
                        break;
                    }
                    case 1:{
                        _sipModel.callToggleMute();
                        break;
                    }
                    case 2:{
                        _sipModel.callToggleHold();
                        break;
                    }
                    case 3:{
                        _sipModel.callToggleSpeaker();
                        break;
                    }
                    case 4:{
                        _sipModel.callDisconnect();
                        mDrawerLayout.closeDrawers();
                        break;
                    }
                    case 5:{
                        _sipModel.makeCall("5000");
                        break;
                    }
                }
            } catch (Exception e){
                Log.e(HelperModel.TAG_MAIN, "Error executing call control action.", e);
            }
        }
    }

    public void openCallControls(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    mDrawerLayout.openDrawer(findViewById(R.id.left_drawer));
                } catch (Exception e){
                    Log.e(HelperModel.TAG_MAIN, "General exception.", e);
                }
            }
        });
    }

    public void updateCallStatus(final SipAudioCall call) {
        //Log.d(HelperModel.TAG_CALLS, "updateCallStatus: " + Arrays.toString(Thread.currentThread().getStackTrace()));
        this.runOnUiThread(new Runnable() {
            public void run() {
                try{
                    String info_statusMessage = "";
                    String info_remoteAddress = "";

                    if (call !=null){
                        // Call status
                        /*
                        Note that the context of SipAudioCall.isInCall() is the audio connection between
                        the Android phone and the server to which the call was placed and not between
                        CIC and the PSTN. This is important  because of how CIC handles outbound
                        calls. The audio path will be connected between the phone and CIC before the
                        outbound PSTN call is connected. Because of this, the call will show as
                        connected [to CIC] even when the call is still ringing the endpoint
                        beyond the outbound gateway to the PSTN.
                         */
                        info_statusMessage = "Is Connected: " + call.isInCall();

                        // Remote address
                        SipProfile remoteProfile = call.getPeerProfile();
                        if (remoteProfile != null){
                            info_remoteAddress = remoteProfile.getDisplayName();
                            if (info_remoteAddress == null){
                                info_remoteAddress = remoteProfile.getUserName();
                            }
                        }
                    } else {
                        Log.v(HelperModel.TAG_MAIN, "Call was null, no data will be displayed.");
                    }

                    // Update on phone icon
                    ImageView imgOnPhone = (ImageView) findViewById(R.id.image_OnPhone);
                    if (call == null){
                        imgOnPhone.setVisibility(View.GONE);
                    }
                    else if (!call.isInCall()){
                        imgOnPhone.setVisibility(View.VISIBLE);
                        imgOnPhone.setImageResource(R.drawable.phone_receiver);
                    }
                    else{
                        imgOnPhone.setVisibility(View.VISIBLE);
                        imgOnPhone.setImageResource(R.drawable.phone_call);
                    }

                    // Update drawer call controls
                    //{"Answer","Mute","Hold","Speaker","Disconnect"}
                    ListView listView = (ListView) findViewById(R.id.left_drawer);
                    if (listView.getChildAt(0) != null){
                        View rowView;
                        //TextView rowText;
                        //rowText= (TextView) rowView.findViewById(R.id.label);
                        ImageView rowImage;

                        // MUTE
                        rowView = listView.getChildAt(1);
                        rowImage = (ImageView) rowView.findViewById(R.id.icon);
                        if (call != null && call.isMuted())
                            rowImage.setBackgroundResource(R.drawable.border_background);
                        else
                            rowImage.setBackgroundColor(Color.TRANSPARENT);

                        // HOLD
                        rowView = listView.getChildAt(2);
                        rowImage = (ImageView) rowView.findViewById(R.id.icon);
                        if (call != null && call.isOnHold())
                            rowImage.setBackgroundResource(R.drawable.border_background);
                        else
                            rowImage.setBackgroundColor(Color.TRANSPARENT);

                        // SPEAKER
                        rowView = listView.getChildAt(3);
                        rowImage = (ImageView) rowView.findViewById(R.id.icon);
                        if (call != null && _sipModel.isSpeakerphoneOn())
                            rowImage.setBackgroundResource(R.drawable.border_background);
                        else
                            rowImage.setBackgroundColor(Color.TRANSPARENT);
                    }else{
                        Log.w(HelperModel.TAG_MAIN, "Unable to find list view items!");
                    }
                } catch (Exception e){
                    Log.e(HelperModel.TAG_MAIN, "General exception.", e);
                }
            }
        });
    }

    public void updateStationState(){
        this.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    Log.v(HelperModel.TAG_MAIN, "Station state is now: " + _sipModel.getStationState() + " (" + _sipModel.getStationMessage() + ")");

                    ImageButton registerButton = (ImageButton) findViewById(R.id.button_RegisterStation);
                    TextView stationStateText = (TextView) findViewById(R.id.stationStateText);

                    switch(_sipModel.getStationState()){
                        case Unknown:{
                            registerButton.setImageResource(R.drawable.bullet_square_glass_grey);
                            stationStateText.setText(_sipModel.getStationMessage());
                            break;
                        }
                        case Registered:{
                            registerButton.setImageResource(R.drawable.bullet_square_glass_green);
                            stationStateText.setText("Registered " + _sipModel.getStationMessage());
                            break;
                        }
                        case Unregistered:{
                            registerButton.setImageResource(R.drawable.bullet_square_glass_yellow);
                            stationStateText.setText("Unregistered " + _sipModel.getStationMessage());
                            break;
                        }
                        case Attempting:{
                            registerButton.setImageResource(R.drawable.bullet_square_glass_blue);
                            stationStateText.setText("Attempting " + _sipModel.getStationMessage());
                            break;
                        }
                        case Error:{
                            registerButton.setImageResource(R.drawable.bullet_square_glass_red);
                            stationStateText.setText("Error: " + _sipModel.getStationMessage());
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(HelperModel.TAG_MAIN, "General exception.", e);
                }
            }
        });
    }
}
