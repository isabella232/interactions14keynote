package com.inin.gearphoneapp.app;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.inin.gearphoneapp.app.icws.IcwsClient;
import com.inin.gearphoneapp.app.util.AppLog;
import com.inin.gearphoneapp.app.util.HelperModel;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by kevin.glinski on 4/29/14.
 */
public class GearAccessoryProviderService extends SAAgent {
    public static GearAccessoryProviderService instance;

    public static final String TAG = "GearAccessoryProviderService";

    public static final int SERVICE_CONNECTION_RESULT_OK = 0;

    public static final int ACCESSORY_CHANNEL_ID = 104;

    HashMap<Integer, GearAccessoryProviderConnection> mConnectionsMap = new HashMap<Integer, GearAccessoryProviderConnection>();;

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public GearAccessoryProviderService getService() {
            return GearAccessoryProviderService.this;
        }
    }

    public GearAccessoryProviderService() {
        super(TAG, GearAccessoryProviderConnection.class);
        Log.v(HelperModel.TAG_ACCESSORY,"GearAccessoryProviderService::ctor");

        GearAccessoryProviderService.instance = this;
    }

    public class GearAccessoryProviderConnection extends SASocket {
        private int mConnectionId;

        public GearAccessoryProviderConnection() {

            super(GearAccessoryProviderConnection.class.getName());
        }

        @Override
        public void onError(int channelId, String errorString, int error) {
            AppLog.e(TAG, "Connection is not alive ERROR: " + errorString + "  "
                    + error);
        }

        @Override
        public void onReceive(int channelId, byte[] dataBytes) {
            AppLog.d(TAG, "onReceive");

            try {


                JSONObject data = new JSONObject(new String(dataBytes));

                if (data.getString("messageType").equalsIgnoreCase("listen")) {
                    AppLog.d(TAG, "Listen to " + data.getString("interactionId"));

                    try
                    {
                        JSONObject listenData = new JSONObject();
                        listenData.put("on","true");
                        listenData.put("supervisor", "true");

                        IcwsClient.instance.post("/interactions/" + data.getString("interactionId") + "/listen", listenData);


                    }catch(org.json.JSONException jex){}

                }
                else  if (data.getString("messageType").equalsIgnoreCase("join")) {
                    AppLog.d(TAG, "Listen to " + data.getString("interactionId"));

                    try
                    {
                        JSONObject listenData = new JSONObject();
                        IcwsClient.instance.post("/interactions/" + data.getString("interactionId") + "/join", listenData);
                    }catch(org.json.JSONException jex){}
                }
                else if (data.getString("messageType").equalsIgnoreCase("stoplisten")) {
                    AppLog.d(TAG, "Listen to " + data.getString("interactionId"));

                    try
                    {
                        JSONObject listenData = new JSONObject();
                        listenData.put("on","false");
                        listenData.put("supervisor", "galse");


                        IcwsClient.instance.post("/interactions/" + data.getString("interactionId") + "/listen", listenData);


                    }catch(org.json.JSONException jex){}

                }

                else if (data.getString("messageType").equalsIgnoreCase("disconnect")) {
                    AppLog.d(TAG, "Listen to " + data.getString("interactionId"));

                    try
                    {
                        JSONObject disconnectData = new JSONObject();

                        IcwsClient.instance.post("/interactions/" + data.getString("interactionId") + "/disconnect", disconnectData);


                    }catch(org.json.JSONException jex){}

                }
            }catch(org.json.JSONException jex){}


         //   sendMessageToWatch(message);
        }

        public void sendMessageToWatch(final String message){

            final GearAccessoryProviderConnection uHandler = mConnectionsMap.get(Integer
                    .parseInt(String.valueOf(mConnectionId)));
            if(uHandler == null){
                AppLog.e(TAG,"Error, can not get HelloAccessoryProviderConnection handler");
                return;
            }
            new Thread(new Runnable() {
                public void run() {
                    try {
                        uHandler.send(ACCESSORY_CHANNEL_ID, message.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }


        @Override
        protected void onServiceConnectionLost(int errorCode) {
            AppLog.e(TAG, "onServiceConectionLost  for peer = " + mConnectionId
                    + "error code =" + errorCode);

            if (mConnectionsMap != null) {
                mConnectionsMap.remove(mConnectionId);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate of smart view Provider Service");

        SA mAccessory = new SA();
        try {
            mAccessory.initialize(this);
        } catch (SsdkUnsupportedException e) {
            // Error Handling
        } catch (Exception e1) {
            AppLog.e(TAG, "Cannot initialize Accessory package.");
            e1.printStackTrace();

            stopSelf();
        }

    }

    @Override
    protected void onServiceConnectionRequested(SAPeerAgent peerAgent) {
        acceptServiceConnectionRequest(peerAgent);
    }

    @Override
    protected void onFindPeerAgentResponse(SAPeerAgent arg0, int arg1) {
        // TODO Auto-generated method stub
        AppLog.d(TAG, "onFindPeerAgentResponse  arg1 =" + arg1);
    }

    @Override
    protected void onServiceConnectionResponse(SASocket thisConnection,
                                               int result) {
        if (result == CONNECTION_SUCCESS) {
            if (thisConnection != null) {
                GearAccessoryProviderConnection myConnection = (GearAccessoryProviderConnection) thisConnection;

                if (mConnectionsMap == null) {
                    mConnectionsMap = new HashMap<Integer, GearAccessoryProviderConnection>();
                }

                myConnection.mConnectionId = (int) (System.currentTimeMillis() & 255);

                AppLog.d(TAG, "onServiceConnection connectionID = "
                        + myConnection.mConnectionId);

                mConnectionsMap.put(myConnection.mConnectionId, myConnection);

                Toast.makeText(getBaseContext(),
                        "R.string.ConnectionEstablishedMsg", Toast.LENGTH_LONG)
                        .show();
            } else {
                AppLog.e(TAG, "SASocket object is null");
            }
        } else if (result == CONNECTION_ALREADY_EXIST) {
            AppLog.e(TAG, "onServiceConnectionResponse, CONNECTION_ALREADY_EXIST");
        } else {
            AppLog.e(TAG, "onServiceConnectionResponse result error =" + result);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    public void newAlert(String title, String subTitle, String subTitle2, String interactionId){
        try {
            JSONObject data = new JSONObject();
            data.put("messageType", "newAlert");
            data.put("title", title);
            data.put("subTitle", subTitle);
            data.put("subTitle2", subTitle2);
            data.put("interactionId", interactionId);

            sendDataToRemoteDisplay(data.toString());
            for (GearAccessoryProviderConnection connection : mConnectionsMap.values()) {
                connection.sendMessageToWatch(data.toString());
            }
        }catch(Exception e){
            Log.e(HelperModel.TAG_ACCESSORY, "General error.", e);
        }

    }

    public void clearAlerts(){
        try {
        JSONObject data = new JSONObject();
        data.put("messageType", "clearAlert");

        sendDataToRemoteDisplay(data.toString());
        for(GearAccessoryProviderConnection connection : mConnectionsMap.values()){
            connection.sendMessageToWatch(data.toString() );
        }
        }catch(org.json.JSONException jex){}
    }

    public void newCall(String remoteName, String remoteNumber, boolean isListening, String interactionId){
        try {
            JSONObject data = new JSONObject();
            data.put("messageType", "newCall");
            data.put("remoteName", remoteName);
            data.put("remoteNumber", remoteNumber);
            data.put("isListening", isListening);
            data.put("interactionId", interactionId);

            sendDataToRemoteDisplay(data.toString());
            for (GearAccessoryProviderConnection connection : mConnectionsMap.values()) {
                connection.sendMessageToWatch(data.toString());
            }
        }catch(org.json.JSONException jex){}
    }

    public void clearCall(){

    }


    private class UpdateTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... data) {
            HttpClient httpClient = new DefaultHttpClient();

            HttpContext localContext = new BasicHttpContext();
            HttpPost post = new HttpPost("http://digclayapps.dev2000.com/RemoteWatchDisplay/rest.svc/postdata");
            String text = null;

            try {
                post.setEntity(new ByteArrayEntity(data[0].getBytes("UTF8")));

                HttpResponse response = httpClient.execute(post, localContext);
                HttpEntity entity = response.getEntity();

            } catch (Exception e) {
                AppLog.d(TAG,"unable to connect to the server " + e);
                return "";// e.getLocalizedMessage();
            }
            return "";
        }

    }

    private void sendDataToRemoteDisplay(String data){

        UpdateTask task = new UpdateTask();
        task.execute(new String[] { data });
    }
}