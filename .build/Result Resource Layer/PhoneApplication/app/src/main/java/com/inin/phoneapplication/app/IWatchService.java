package com.inin.phoneapplication.app;

/**
 * Created by kevin.glinski on 3/25/14.
 */
public interface IWatchService {
    void alertAdded(AlertAction alert);

    void headsetOn();
    void headsetOff();
    //void alertCleared(String callId);
    //void callAdded(String callId, String remoteName, String remoteNumber);
    //void callDisconnected(String callId);


    //TODO: handle Listen, Disconnect events

}
