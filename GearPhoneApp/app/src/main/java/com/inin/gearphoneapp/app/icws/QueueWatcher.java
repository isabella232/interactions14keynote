package com.inin.gearphoneapp.app.icws;

import android.util.Log;

import com.inin.gearphoneapp.app.GearAccessoryProviderService;
import com.inin.gearphoneapp.app.util.AppLog;
import com.inin.gearphoneapp.app.util.HelperModel;

import org.json.JSONArray;
import org.json.JSONObject;
import 	java.util.HashMap;

/**
 * Created by kevin.glinski on 4/30/14.
 */
public class QueueWatcher implements IMessageReceiver {

    public String messageId(){
        return "urn:inin.com:queues:queueContentsMessage";
    }

    private IcwsClient _icwsClient = null;
    private boolean _shouldNotifyWatch = false;
    private HashMap<String, JSONObject> _interactions = new HashMap<String,JSONObject>();
    private String _queueName;

    public QueueWatcher(String queueName , String queueType, boolean shouldNotifyWatch, IcwsClient icwsClient){
        _icwsClient = icwsClient;
        _shouldNotifyWatch = shouldNotifyWatch;
        _queueName = queueName;

        try {
            JSONArray queueIDs = new JSONArray(); //new int[] {2,3,4,5}

            JSONObject queueId = new JSONObject();
            queueId.put("queueType", queueType);
            queueId.put("queueName", queueName);

            queueIDs.put(queueId);

            JSONArray attributeNames = new JSONArray();
            attributeNames.put("eic_state");
            attributeNames.put("Eic_KwsCustomerKeywords");
            attributeNames.put("Eic_KwsCustomerLastKeyword");
            attributeNames.put("Eic_KwsCustomerNegativeScore");
            attributeNames.put("Eic_KwsCustomerNumSpotted");
            attributeNames.put("Eic_KwsCustomerPositiveScore");
            attributeNames.put("Eic_CallStateString");
            attributeNames.put("Eic_RemoteID");
            attributeNames.put("Eic_RemoteName");
            attributeNames.put("Eic_UserName");

            JSONObject obj = new JSONObject();
            obj.put("queueIds", queueIDs);
            obj.put("attributeNames", attributeNames);
            _icwsClient.put("/messaging/subscriptions/queues/" + queueName, obj);
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
    }

    JSONObject _currentCall = null;

    public void MessageReceived(JSONObject data){
        try {

            if(!data.getString("subscriptionId").equalsIgnoreCase(_queueName)){
                return;
            }

            if(data.has("interactionsAdded")) {
                JSONArray added = data.getJSONArray("interactionsAdded");

                for (int x = 0; x < added.length(); x++) {
                    JSONObject call = added.getJSONObject(x);

                    String interactionId = call.getString("interactionId");

                    _interactions.put(interactionId, call);
                    AppLog.d(HelperModel.TAG_QUEUE_WATCHER, "call Added: " + call.getString("interactionId"));

                    if (_shouldNotifyWatch) {

                        GearAccessoryProviderService.instance.newCall(getRemoteName(interactionId), getRemoteNumber(interactionId), getIsListening(interactionId), interactionId);
                    }
                }
            }

            if(data.has("interactionsChanged")) {

                JSONArray changed = data.getJSONArray("interactionsChanged");

                for (int x = 0; x < changed.length(); x++) {
                    JSONObject call = changed.getJSONObject(x);

                    String interactionId = call.getString("interactionId");


                    JSONObject cachedInteraction = _interactions.get(interactionId);

                    java.util.Iterator<?> keys = call.getJSONObject("attributes").keys();

                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        cachedInteraction.getJSONObject("attributes").put(key, call.getJSONObject("attributes").getString(key));
                    }

                    AppLog.d(HelperModel.TAG_QUEUE_WATCHER, "call changed: " + interactionId);
                    _interactions.put(interactionId, cachedInteraction);

                    if (_shouldNotifyWatch && isDisconnected(interactionId)) {
                        GearAccessoryProviderService.instance.clearAlerts();
                    } else if (_shouldNotifyWatch ) {
                        GearAccessoryProviderService.instance.newCall(getRemoteName(interactionId), getRemoteNumber(interactionId), getIsListening(interactionId), interactionId);
                    }
                }
            }

            if(data.has("interactionsRemoved")) {

                JSONArray removed = data.getJSONArray("interactionsRemoved");

                for (int x = 0; x < removed.length(); x++) {
                    String call = removed.getString(x);
                    AppLog.d(HelperModel.TAG_QUEUE_WATCHER, "call removed: " + call);
                    _interactions.remove(call);
                }
            }
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
    }

    public String findCallWithLowestCustomerScore(){
        if(_interactions.size() == 0){
            return "";
        }

        String callToReturn = null;
        Integer returnScore=0;

        for(JSONObject call: _interactions.values() ){
            try {

                if(callToReturn == null){
                    callToReturn = call.getString("interactionId");
                    returnScore = -100;
                }

                Integer score = Integer.parseInt(call.getJSONObject("attributes").getString("Eic_KwsCustomerNegativeScore"));
                if (score > returnScore) {
                    returnScore = score;
                    callToReturn = call.getString("interactionId");
                }
            } catch (Exception e){
                Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
            }
        }

        return callToReturn;

    }

    public String findFirstNonDisconnectedCall(){
        try {
            if(_interactions.size() == 0){
                return "";
            }
            for(JSONObject call: _interactions.values() ){
                String interactionId = call.getString("interactionId");
                if (!isDisconnected(interactionId)) return interactionId;
            }
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
        return "";
    }

    public boolean isDisconnected(String interactionId){
        try {
            String state = _interactions.get(interactionId).getJSONObject("attributes").getString("Eic_State");

            return state == "E" || state == "I";
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
        return false;
    }

    public boolean isHeld(String interactionId){
        try {
            String state = _interactions.get(interactionId).getJSONObject("attributes").getString("Eic_State");

            return state == "H";
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
        return false;
    }

    public boolean isMuted(String interactionId){
        try {
            String state = _interactions.get(interactionId).getJSONObject("attributes").getString("Eic_Muted");

            return state == "1";
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
        return false;
    }

    public String getRemoteName(String interactionId){
        try {
            return _interactions.get(interactionId).getJSONObject("attributes").getString("Eic_RemoteName");
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
        return "";
    }

    public String getRemoteNumber(String interactionId){
        try {
            return _interactions.get(interactionId).getJSONObject("attributes").getString("Eic_RemoteId");
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
        return "";
    }

    public boolean getIsListening(String interactionId){
        try{
            return _interactions.get(interactionId).getJSONObject("attributes").getString("Eic_CallStateString").contains("Monitoring");
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
        return false;
    }

    public String getUserName(String interactionId){
        try{
            return _interactions.get(interactionId).getJSONObject("attributes").getString("Eic_UserName");
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
        return "";
    }

    public void pickupCall(){
        try{
            String interactionId = findFirstNonDisconnectedCall();
            if (interactionId == "") return;

            _icwsClient.post("/interactions/"+interactionId+"/pickup", new JSONObject());
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
    }

    public void disconnectCall(){
        try{
            String interactionId = findFirstNonDisconnectedCall();
            if (interactionId == "") return;

            _icwsClient.post("/interactions/"+interactionId+"/pickup", new JSONObject());
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
    }

    public void toggleHold(){
        try{
            String interactionId = findFirstNonDisconnectedCall();
            if (interactionId == "") return;

            holdCall(interactionId, !isHeld(interactionId));
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
    }

    public void holdCall(boolean on){
        try{
            String interactionId = findFirstNonDisconnectedCall();
            if (interactionId == "") return;

            holdCall(interactionId, on);
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
    }

    public void holdCall(String interactionId, boolean on){
        try{
            Log.v(HelperModel.TAG_QUEUE_WATCHER, "holdCall for " + interactionId+" set to: " + on);
            _icwsClient.post("/interactions/"+interactionId+"/hold", new JSONObject().put("on", on));
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
    }

    public void toggleMute(){
        try{
            String interactionId = findFirstNonDisconnectedCall();
            if (interactionId == "") return;

            muteCall(interactionId, !isMuted((interactionId)));
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
    }

    public void muteCall(boolean on){
        try{
            String interactionId = findFirstNonDisconnectedCall();
            if (interactionId == "") return;

            muteCall(interactionId, on);
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
    }

    public void muteCall(String interactionId, boolean on){
        try{
            Log.v(HelperModel.TAG_QUEUE_WATCHER, "muteCall for " + interactionId+" set to: " + on);
            _icwsClient.post("/interactions/"+interactionId+"/mute", new JSONObject().put("on", on));
        } catch (Exception e){
            Log.e(HelperModel.TAG_QUEUE_WATCHER, "General error.", e);
        }
    }
}
