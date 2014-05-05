package com.inin.gearphoneapp.app.icws;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Map;
import java.util.HashMap;
import com.inin.gearphoneapp.app.util.AppLog;

/**
 * Created by kevin.glinski on 3/26/14.
 */
public class AlertCatalog implements IMessageReceiver {

    private Map<String,AlertAction> _catalog = new HashMap<String,AlertAction>();

    public String messageId(){
        return "urn:inin.com:alerts:alertCatalogChangedMessage";
    }

    public AlertAction getAlertAction(String alertId){
        if(_catalog.containsKey(alertId)){
            return _catalog.get(alertId);
        }
        return null;
    }

    public void MessageReceived(JSONObject data)
    {
        try {
            JSONArray addedAlerts = data.getJSONArray("alertSetsAdded");
            for (int x = 0; x < addedAlerts.length(); x++) {
                try {
                    JSONObject jsonAlert = addedAlerts.getJSONObject(x);


                    JSONArray alertDefinitions = jsonAlert.getJSONArray("alertDefinitions");

                    for(int y=0; y< alertDefinitions.length(); y++){
                        JSONObject alertDefinition = alertDefinitions.getJSONObject(y);
                        String statId = alertDefinition.getJSONObject("statisticKey").getString("statisticIdentifier");
                        JSONArray alertRules = alertDefinition.getJSONArray("alertRules");

                        for(int j=0; j<alertRules.length(); j++) {

                            JSONObject rule = alertRules.getJSONObject(j);

                            String alertRuleId = rule.getString("alertRuleId");

                            JSONArray actions = rule.getJSONArray("alertActions");

                            for (int z = 0; z < actions.length(); z++) {
                                JSONObject action = actions.getJSONObject(z);

                                /*
                                if (action.getString("targetId").equals("ININ.Supervisor.FontAlertAction")) {
                                    JSONObject values = action.getJSONObject("namedValues");
                                    AppLog.d("", alertRuleId + " : " + statId);
                                    AlertAction newAlertAction = new AlertAction(alertRuleId, statId, values.getString("ININ.Supervisor.FontAlertAction.TextColor"), values.getString("ININ.Supervisor.FontAlertAction.BackgroundColor"));
                                    _catalog.put(alertRuleId, newAlertAction);
                                }
                                else{
                                    AppLog.d("", "font alert action not defined: " + statId);

                                }
                                */
                                JSONObject values = action.getJSONObject("namedValues");
                               // AppLog.d("", alertRuleId + " : " + statId);
                                AlertAction newAlertAction = new AlertAction(alertRuleId, statId, "","");
                                _catalog.put(alertRuleId, newAlertAction);

                            }
                        }
                    }

                }
                catch (Exception ex) {
                }
            }
        }catch(Exception ex){}

    }
}
