package com.inin.gearphoneapp.app.icws;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.inin.gearphoneapp.app.MainActivity;
import com.inin.gearphoneapp.app.Sip.SipModel;
import com.inin.gearphoneapp.app.pref.PreferencesFragment;
import com.inin.gearphoneapp.app.util.HelperModel;

/**
 * Created by kevin.glinski on 4/30/14.
 */
public class IcwsService extends Service {
    public static IcwsService instance;

    private IcwsClient _icwsClient;
    private MessagePollService _messagePollService = null;
    private MainActivity _mainActivity;

    public QueueWatcher WorkgroupQueueReceiver;
    public QueueWatcher UserQueueReceiver;

    private class ConnectTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Log.v(HelperModel.TAG_ICWS, "Connecting...");
                ConnectionService c = new ConnectionService();

                // Get preferences for the connection
                //TODO: Good god that's gross... Need a better pattern for accessing settings.
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SipModel.GetInstance(null)._mainActivity);
                String server = prefs.getString(PreferencesFragment.KEY_PREF_CONNECTION_SERVER, "");
                String username = prefs.getString(PreferencesFragment.KEY_PREF_CONNECTION_SERVER_USERNAME, "");
                String password = prefs.getString(PreferencesFragment.KEY_PREF_CONNECTION_SERVER_PASSWORD, "");
                String queue = prefs.getString(PreferencesFragment.KEY_PREF_ALERTS_QUEUE, "");
                String port = prefs.getString(PreferencesFragment.KEY_PREF_CONNECTION_ICWS_PORT, "");

                // Connect
                server = "http://" + server + ":" + port;
                Log.v(HelperModel.TAG_ICWS, "Connecting to " + server + " as " + username);
                _icwsClient = c.connect(username, password, server);

                // Stop message poll service if it's running
                if (_messagePollService != null) {
                    _messagePollService.stopTimer();
                }

                // Create message poll service
                _messagePollService = new MessagePollService(_icwsClient);

                // Why sleep? Race condition?
                Thread.sleep(500);

                // ???
                IMessageReceiver alertCatalog = new AlertCatalog();
                _messagePollService.RegisterReceiver(alertCatalog);

                // Watch workgroup queue
                WorkgroupQueueReceiver = new QueueWatcher(queue, "2", false, _icwsClient);
                _messagePollService.RegisterReceiver(WorkgroupQueueReceiver);

                // Watch user queue
                UserQueueReceiver = new QueueWatcher(username, "1", true, _icwsClient);
                _messagePollService.RegisterReceiver(UserQueueReceiver);

                IMessageReceiver alertReceiver = new AlertWatchingService((AlertCatalog) alertCatalog, WorkgroupQueueReceiver, _icwsClient);
                _messagePollService.RegisterReceiver(alertReceiver);

                Log.v(HelperModel.TAG_ICWS, "Connect complete");
            }catch(Exception e){
                Log.e(HelperModel.TAG_ICWS, "Exception on connect.", e);
            }

            return "";
        }
    }

    public void connect(){
        try{
            // Execute connect async
            ConnectTask task = new ConnectTask();
            task.execute(new String[] { "" });
        }catch(Exception e){
            Log.e(HelperModel.TAG_ICWS, "General error.", e);
        }
    }

    @Override
    public void onCreate() {
        try{
            Log.v(HelperModel.TAG_ICWS, "onCreate");
            instance = this;
            Log.v(HelperModel.TAG_ICWS, "instance="+instance);
            Log.v(HelperModel.TAG_ICWS, "onCreate -- Done");
        }catch(Exception e){
            Log.e(HelperModel.TAG_ICWS, "General error.", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try{
            Log.v(HelperModel.TAG_ICWS, "onStartCommand");
            // If we get killed, after returning from here, restart
            return START_STICKY;
        }catch(Exception e){
            Log.e(HelperModel.TAG_ICWS, "General error.", e);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        try{
            Log.v(HelperModel.TAG_ICWS, "onBind");
            // We don't provide binding, so return null
            return null;
        }catch(Exception e){
            Log.e(HelperModel.TAG_ICWS, "General error.", e);
        }
        return null;
    }

    @Override
    public void onDestroy() {
        try{
            Log.v(HelperModel.TAG_ICWS, "onDestroy");
         //   Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            Log.e(HelperModel.TAG_ICWS, "General error.", e);
        }
    }
}