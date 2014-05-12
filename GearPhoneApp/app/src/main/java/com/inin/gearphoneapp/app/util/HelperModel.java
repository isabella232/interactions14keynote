package com.inin.gearphoneapp.app.util;

import android.os.Looper;

public class HelperModel {

    public static final String TAG = "*-GP";
    public static final String TAG_MAIN = "*-GP/Main";
    public static final String TAG_SIP = "*-GP/SIP";
    public static final String TAG_CALLS = "*-GP/Calls";
    public static final String TAG_REGISTRATION_LISTENER = "*-GP/RegL";
    public static final String TAG_SESSION_LISTENER = "*-GP/SesL";
    public static final String TAG_ICWS = "*-GP/ICWS";
    public static final String TAG_ACCESSORY = "*-GP/AC";
    public static final String TAG_QUEUE_WATCHER = "*-GP/QueueWatcher";

    public static boolean isOnUiThread(){
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
