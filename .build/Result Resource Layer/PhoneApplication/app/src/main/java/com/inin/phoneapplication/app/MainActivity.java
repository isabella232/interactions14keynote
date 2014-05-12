package com.inin.phoneapplication.app;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preview.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Handler;
import android.view.View;
import android.os.AsyncTask;
import android.os.Message;
import android.content.IntentFilter;
import android.bluetooth.BluetoothAssignedNumbers;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.widget.EditText;

public class MainActivity extends Activity {
    private final String LOG_TAG = "Main Activity";
    private IcwsClient _icwsClient = null;
    private MessagePollService _messagePollService;
    private IWatchService _watch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _watch = new SamsungGearWatchService((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));

        AppLog.init((EditText)findViewById(R.id.logText));
        AppLog.d(LOG_TAG,"OnCreate");
        ConnectTask task = new ConnectTask();
        task.execute(new String[] { "" });

        initBluetooth();
    }

    private class ConnectTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            ConnectionService c = new ConnectionService();
            _icwsClient = c.connect("devlab_user", "1234", "http://172.19.34.165:8018");

            _messagePollService = new MessagePollService(_icwsClient);

            IMessageReceiver alertCatalog = new AlertCatalog();
            _messagePollService.RegisterReceiver(alertCatalog);

            IMessageReceiver alertReceiver = new AlertWatchingService((AlertCatalog)alertCatalog, _icwsClient, _watch);
            _messagePollService.RegisterReceiver(alertReceiver);


            return "";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();  // Always call the superclass

        AppLog.d("","onDestroy");
        if(_messagePollService != null) {
            _messagePollService.stopTimer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        AppLog.d("","onPause");
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        AppLog.d("","onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        AppLog.d("","onStop");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class BluetoothHandler extends Handler {

        View _putOnHeadset = null;
        View _headsetConnected = null;
        IWatchService _watch = null;
        public BluetoothHandler(IWatchService watch){
            _watch = watch;
            //_putOnHeadset = putOnHeadset;
            //_headsetConnected = headsetConnected;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PlantronicsReceiver.HEADSET_EVENT:
                    PlantronicsXEventMessage message = (PlantronicsXEventMessage)msg.obj;
                    AppLog.d("Plantronics", message.toString());

                    if(message.getEventType().equals("DON")){
                        //_mainView.setBackgroundColor(Color.BLUE);
                        //_putOnHeadset.setVisibility(View.GONE);
                        //_headsetConnected.setVisibility(View.VISIBLE);
                        _watch.headsetOn();
                    }
                    else if(message.getEventType().equals("DOFF")){
                        //_putOnHeadset.setVisibility(View.VISIBLE);
                        //_headsetConnected.setVisibility(View.GONE);
                        _watch.headsetOff();
                    }

                    break;
                default:
                    break;
            }
        }
    }

    private PlantronicsReceiver btReceiver;
    private BluetoothHandler btHandler;
    private IntentFilter btIntentFilter;

    private void initBluetooth() {
        btHandler = new BluetoothHandler(_watch);
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
}
