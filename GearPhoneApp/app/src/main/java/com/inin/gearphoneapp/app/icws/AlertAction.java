package com.inin.gearphoneapp.app.icws;
import android.graphics.Color;

/**
 * Created by kevin.glinski on 3/26/14.
 */
public class AlertAction {

    static String convertColorFromString(String colorString){

        return colorString.replace(":", "");
    }

    private String _backgroundColor;
    private String _textColor;
    private String _ruleId;
    private String _text;

    public AlertAction(String ruleId, String statisticIdentifier, String textColor, String backgroundColor){
        _backgroundColor = convertColorFromString(backgroundColor);
        _textColor = convertColorFromString(textColor);
        _ruleId = ruleId;

        _text = statisticIdentifier.substring(statisticIdentifier.lastIndexOf(':') + 1);
    }

    public String getRuleId(){
        return _ruleId;
    }

    public String getText(){
        return _text;
    }

    public String getTextColor(){
        return _textColor;
    }

    public String getBackgroundColor(){
        return _backgroundColor;
    }
}
