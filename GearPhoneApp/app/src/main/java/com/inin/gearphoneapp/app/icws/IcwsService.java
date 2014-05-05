package com.inin.gearphoneapp.app.icws;

import android.os.AsyncTask;
import 	android.app.Service;
import 	android.content.Intent;
import android.os.IBinder;

/**
 * Created by kevin.glinski on 4/30/14.
 */
public class IcwsService extends Service {
    private IcwsClient _icwsClient;
    private MessagePollService _messagePollService = null;

    private class ConnectTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                ConnectionService c = new ConnectionService();

                String userName = "devlab_user";
                String password = "1234";

                //_icwsClient = c.connect(userName, password, "http://172.19.34.165:8018");
                _icwsClient = c.connect(userName, password, "http://172.19.34.165:8888");

                if (_messagePollService != null) {
                    _messagePollService.stopTimer();
                }

                _messagePollService = new MessagePollService(_icwsClient);

                Thread.sleep(500);

                IMessageReceiver alertCatalog = new AlertCatalog();
                _messagePollService.RegisterReceiver(alertCatalog);

                QueueWatcher workgroupQueueReceiver = new QueueWatcher("marketing", "2", false, _icwsClient);
                _messagePollService.RegisterReceiver(workgroupQueueReceiver);


                QueueWatcher userQueueReceiver = new QueueWatcher(userName, "1", true, _icwsClient);
                _messagePollService.RegisterReceiver(userQueueReceiver);

                IMessageReceiver alertReceiver = new AlertWatchingService((AlertCatalog) alertCatalog, workgroupQueueReceiver, _icwsClient);
                _messagePollService.RegisterReceiver(alertReceiver);


            }catch(Exception ex){}

            return "";
        }
    }

    public static IcwsService instance;

    public void connect(){
        ConnectTask task = new ConnectTask();
        task.execute(new String[] { "" });
    }

    @Override
    public void onCreate() {
       instance = this;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
     //   Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }
}