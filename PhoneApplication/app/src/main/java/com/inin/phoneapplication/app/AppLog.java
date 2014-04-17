package com.inin.phoneapplication.app;

import android.util.Log;
import android.widget.EditText;

/**
 * Created by kevin.glinski on 4/17/14.
 */
public final class AppLog {

    private static EditText _logTextBox;

    public static void init(EditText logText)
    {
        _logTextBox = logText;
    }
    public static int d(java.lang.String tag, java.lang.String msg)
    {
        String log = _logTextBox.getText().toString();
        log += "\n" + msg;
        _logTextBox.setText(log);
        _logTextBox.scrollBy(0,20);

        return Log.d(tag,msg);

    }

    public static int e(java.lang.String tag, java.lang.String msg)
    {
        String log = _logTextBox.getText().toString();
        log += "\n" + msg;
        _logTextBox.setText(log);
        _logTextBox.scrollBy(0,20);

        return Log.e(tag,msg);

    }

}
