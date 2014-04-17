package com.inin.phoneapplication.app;

import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

import org.json.JSONObject;

/**
 * Created by kevin.glinski on 3/26/14.
 *
 * The MessagePollService is responsible for polling the CIC server for events
 * and then calling out to a IMessageReceiver that is listening for that event type.
 *
 */
public class MessagePollService {

    private List<IMessageReceiver> _receivers = new ArrayList<IMessageReceiver>();
    private IcwsClient _icwsClient = null;
    private Timer _timer;

    public MessagePollService(IcwsClient icwsClient){
        _icwsClient = icwsClient;

        _timer = new Timer();

        _timer.scheduleAtFixedRate(new TimerTask() {

            synchronized public void run() {

                JSONArray messages = _icwsClient.getArray("/messaging/messages");
                ArrayList<JSONObject> alertNotifications = new ArrayList<JSONObject>();

                for(int x=0; x< messages.length(); x++){
                    try {
                        JSONObject message = messages.getJSONObject(x);

                        String messageType = message.getString("__type");

                        if(messageType.equals("")){
                            //delay until later so we can ensure we process any catalog updates first
                            alertNotifications.add(message);
                        }
                        else{
                            notifyReceivers(messageType, message);
                        }

                    }catch(Exception e){}
                }

                for(int x=0; x< alertNotifications.size(); x++) {
                    try {
                        JSONObject message = alertNotifications.get(x);
                        String messageType = message.getString("__type");
                        notifyReceivers(messageType, message);
                    }
                    catch(Exception ex){}

                }
            }

        }, 3000, 1000);
    }

    public void stopTimer(){
        if(_timer != null) {
            _timer.cancel();
        }
    }

    public void notifyReceivers(String messageType, JSONObject message){
        for (int j = 0; j < _receivers.size(); j++) {
            IMessageReceiver receiver = _receivers.get(j);
            if (receiver.messageId().equals(messageType)) {
                receiver.MessageReceived(message);
            }
        }
    }

    public void RegisterReceiver(IMessageReceiver receiver){
        _receivers.add(receiver);
    }
}
