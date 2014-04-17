package com.inin.phoneapplication.app;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;
/**
 * Created by kevin.glinski on 3/25/14.
 */
public class AlertWatchingService implements IMessageReceiver {
    IWatchService _watchService;
    IcwsClient _icwsClient;
    AlertCatalog _alertCatalog;

    public AlertWatchingService(AlertCatalog alertCatalog, IcwsClient icwsClient, IWatchService watchService){
        _watchService = watchService;
        _icwsClient = icwsClient;
        _alertCatalog = alertCatalog;

        try {
            JSONArray categories = new JSONArray(new int[] {2,3,4,5});
            JSONObject obj = new JSONObject();
            obj.put("alertSetCategories", categories);

            _icwsClient.put("/messaging/subscriptions/alerts/alert-catalog", obj);
        }
        catch(Exception ex){}
    }

    public String messageId(){
        return "urn:inin.com:alerts:alertNotificationMessage";
    }

    public void MessageReceived(JSONObject data){
        try {
            JSONArray alertSets = data.getJSONArray("alertNotificationList");

            for (int x = 0; x < alertSets.length(); x++) {
                JSONObject alert = alertSets.getJSONObject(x);
                String ruleId = alert.getString("alertRuleId");

                AlertAction action = _alertCatalog.getAlertAction(ruleId);

                if(action != null)
                {
                    AppLog.d("AlertWatchingService","Got Alert " + action.getText());
                    _watchService.alertAdded(action);
                }
            }
        }
        catch(Exception ex){}
    }
}
