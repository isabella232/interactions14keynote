package com.inin.phoneapplication.app;

import org.json.JSONObject;
/**
 * Created by kevin.glinski on 3/25/14.
 *
 * An IMessageReceiver can register with the MessagePollService for various messages coming form CIC
 *
 */
public interface IMessageReceiver {
    //the ID of the message to listen for.  e.g. urn:inin.com:messaging:asyncOperationCompletedMessage
    String messageId();

    void MessageReceived(JSONObject data);
}
