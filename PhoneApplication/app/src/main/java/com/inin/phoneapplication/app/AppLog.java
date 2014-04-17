package com.inin.phoneapplication.app;

import android.os.Handler;
import android.os.Looper;
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
        LogText(msg);

        return Log.d(tag,msg);

    }

    public static int e(java.lang.String tag, java.lang.String msg)
    {
       LogText(msg);

        return Log.e(tag, msg);

    }

    private static void LogText(final String msg)
    {
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run()
            {
                String log = _logTextBox.getText().toString();
                log += "\n" + msg;
                _logTextBox.setText(log);
                _logTextBox.scrollBy(0,20);
            }
        });
    }
}
