package com.inin.gearphoneapp.app.icws;

import com.inin.gearphoneapp.app.GearAccessoryProviderService;
import com.inin.gearphoneapp.app.util.AppLog;

import org.json.JSONArray;
import org.json.JSONObject;
import 	java.util.HashMap;
/**
 * Created by kevin.glinski on 4/30/14.
 */
public class QueueWatcher implements IMessageReceiver {

    String TAG="QueueWatcher";
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
            attributeNames.put("Eic_ListeningFrom");
            attributeNames.put("Eic_RemoteID");
            attributeNames.put("Eic_RemoteName");
            attributeNames.put("Eic_UserName");

            JSONObject obj = new JSONObject();
            obj.put("queueIds", queueIDs);
            obj.put("attributeNames", attributeNames);
            _icwsClient.put("/messaging/subscriptions/queues/" + queueName, obj);
        }
        catch(Exception ex){}
    }

    public void MessageReceived(JSONObject data){
        try {

            if(!data.getString("subscriptionId").equalsIgnoreCase(_queueName)){
                return;
            }

            JSONArray added = data.getJSONArray("interactionsAdded");

            for (int x = 0; x < added.length(); x++) {
                JSONObject call = added.getJSONObject(x);

                String interactionId = call.getString("interactionId");

                _interactions.put(interactionId, call);
                AppLog.d(TAG, "call Added: " + call.getString("interactionId"));

                if(_shouldNotifyWatch){
                    GearAccessoryProviderService.instance.newCall(getRemoteName(interactionId), getRemoteNumber(interactionId), getIsListening(interactionId), interactionId);
                }
            }

            JSONArray changed = data.getJSONArray("interactionsChanged");

            for (int x = 0; x < changed.length(); x++) {
                JSONObject call = changed.getJSONObject(x);

                String interactionId = call.getString("interactionId");

                AppLog.d(TAG, "call changed: " + interactionId);
                _interactions.put(interactionId, call);

                if(_shouldNotifyWatch && isDisconnected(interactionId)){
                    GearAccessoryProviderService.instance.clearAlerts();
                }

                else if(_shouldNotifyWatch) {
                    GearAccessoryProviderService.instance.newCall(getRemoteName(interactionId), getRemoteNumber(interactionId), getIsListening(interactionId), interactionId);
                }
            }

            JSONArray removed = data.getJSONArray("interactionsAdded");

            for (int x = 0; x < removed.length(); x++) {
                String call = removed.getString(x);
                AppLog.d(TAG, "call removed: " + call);
                _interactions.remove(call);
            }
        }
        catch(Exception ex){}
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
            }
            catch(Exception ex){}
        }

        return callToReturn;

    }

    public boolean isDisconnected(String interactionId){
        try {
            String state = _interactions.get(interactionId).getJSONObject("attributes").getString("Eic_State");

            return state == "E" || state == "I";
        }
        catch(Exception ex){}
        return false;
    }

    public String getRemoteName(String interactionId){
        try {
            return _interactions.get(interactionId).getJSONObject("attributes").getString("Eic_RemoteName");
        }
        catch(Exception ex){}
        return "";
    }

    public String getRemoteNumber(String interactionId){
        try {
            return _interactions.get(interactionId).getJSONObject("attributes").getString("Eic_RemoteID");
        }
        catch(Exception ex){}
        return "";
    }

    public boolean getIsListening(String interactionId){
        try{
            return _interactions.get(interactionId).getJSONObject("attributes").getString("Eic_ListeningFrom").length() > 0;
        }
        catch(Exception ex){}
        return false;
    }

    public String getUserName(String interactionId){
        try{
            return _interactions.get(interactionId).getJSONObject("attributes").getString("Eic_UserName");
        }
        catch(Exception ex){}
        return "";
    }


}
