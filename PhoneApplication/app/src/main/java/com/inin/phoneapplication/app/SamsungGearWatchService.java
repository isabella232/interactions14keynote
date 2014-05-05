package com.inin.phoneapplication.app;

/**
 * Created by kevin.glinski on 3/31/14.
 */
import java.io.IOException;
import java.util.HashMap;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;
import android.app.NotificationManager;
import 	android.support.v4.app.NotificationCompat;
import 	android.content.Context;

public class SamsungGearWatchService extends SAAgent implements IWatchService {
    NotificationManager _notificationManager;

    public static final String TAG = "SamsungGearWatchService";
    Context mCurrentContext;

    public SamsungGearWatchService(NotificationManager notificationManager, Context currentContext) {
        super(TAG, SamsungGearWatchConnection.class);

        mCurrentContext = currentContext;
        _notificationManager = notificationManager;

      /*  NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");


        _notificationManager.notify(1, mBuilder.build());

*/
        onCreate();
    }

    public void alertAdded(AlertAction alert)
    {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Alert")
                        .setContentText(alert.getText());


        _notificationManager.notify(1, mBuilder.build());

    }

    public void headsetOn()
    {

    }
    public void headsetOff(){

    }


    public static final int SERVICE_CONNECTION_RESULT_OK = 0;
    public static final int ACCESSORY_CHANNEL_ID = 105;
    HashMap<Integer, SamsungGearWatchConnection> mConnectionsMap = null;

    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder{
        public SamsungGearWatchService getService(){
            return SamsungGearWatchService.this;
        }
    }

    public class SamsungGearWatchConnection extends SASocket {
        private int mConnectionId;
        public SamsungGearWatchConnection() {
            super(SamsungGearWatchConnection.class.getName());
        }
        @Override
        public void onError(int channelId, String errorString, int error) {
            AppLog.e(TAG, "Connection is not alive ERROR: " + errorString + " "
                    + error);
        }
        @Override
        public void onReceive(int channelId, byte[] data) {
            AppLog.d(TAG, "onReceive");
            Time time = new Time();
            time.set(System.currentTimeMillis());
            String timeStr = " " + String.valueOf(time.minute) + ":"
                    + String.valueOf(time.second);
            String strToUpdateUI = new String(data);
            String message = strToUpdateUI.concat(timeStr);
                SamsungGearWatchConnection uHandler = mConnectionsMap.get(Integer
                    .parseInt(String.valueOf(mConnectionId)));
            try {
                uHandler.send(ACCESSORY_CHANNEL_ID, message.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        protected void onServiceConnectionLost(int errorCode) {
            AppLog.e(TAG, "onServiceConectionLost for peer = " + mConnectionId
                    + "error code =" + errorCode);
            if (mConnectionsMap != null) {
                mConnectionsMap.remove(mConnectionId);
            }
        }
    }

    @Override
    public void onCreate() {
 //       super.onCreate();
        Log.i(TAG, "onCreate of smart view Provider Service");
        SA mAccessory = new SA();
        try {
         //   mAccessory.initialize(this);
            mAccessory.initialize(mCurrentContext);
            boolean isFeatureEnabled = mAccessory.isFeatureEnabled(SA.DEVICE_ACCESSORY);
        } catch (SsdkUnsupportedException e) {
// Error Handling
        } catch (Exception e1) {
            Log.e(TAG, "Cannot initialize SAccessory package.");
            e1.printStackTrace();
/*
* Your application cannot use Samsung Accessory SDK. You
* application should work smoothly without using this SDK, or you
* may want to notify user and close your app gracefully (release
* resources, stop Service threads, close UI thread, etc.)
*/
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
        Log.d(TAG, "onFindPeerAgentResponse arg1 =" + arg1);
    }
    @Override
    protected void onServiceConnectionResponse(SASocket thisConnection,
                                               int result) {
        if (result == CONNECTION_SUCCESS) {
            if (thisConnection != null) {
                SamsungGearWatchConnection myConnection = (SamsungGearWatchConnection) thisConnection;
                if (mConnectionsMap == null) {
                    mConnectionsMap = new HashMap<Integer, SamsungGearWatchConnection>();
                }
                myConnection.mConnectionId = (int) (System.currentTimeMillis() & 255);
                Log.d(TAG, "onServiceConnection connectionID = "
                        + myConnection.mConnectionId);
                mConnectionsMap.put(myConnection.mConnectionId, myConnection);
                Toast.makeText(getBaseContext(),
                        "Connection established", Toast.LENGTH_LONG)
                        .show();
            } else {
                Log.e(TAG, "SASocket object is null");
            }
        } else if (result == CONNECTION_ALREADY_EXIST) {
            Log.e(TAG, "onServiceConnectionResponse, CONNECTION_ALREADY_EXIST");
        } else {
            Log.e(TAG, "onServiceConnectionResponse result error =" + result);
        }
    }
    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }
}