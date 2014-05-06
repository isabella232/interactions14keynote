package com.inin.gearphoneapp.app.Sip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Listens for incoming SIP calls, intercepts and hands them off to MainActivity.
 */
public class IncomingCallReceiver extends BroadcastReceiver {
    private static final String TAG_ONRECEIVE = "*-SC/onReceive";
    /**
     * Processes the incoming call and hands it over to the MainActivity
     * @param context The context under which the receiver is running.
     * @param intent The intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.d(TAG_ONRECEIVE, "OnReceive invoked");
            SipModel.GetInstance(null).receiveCall(intent);

        } catch (Exception e) {
            Log.e(TAG_ONRECEIVE, "Error receiving call.", e);
        }
    }

}

