package com.inin.gearphoneapp.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import 	android.content.Intent;
import com.inin.gearphoneapp.app.icws.IcwsService;

import com.inin.gearphoneapp.app.util.AppLog;

public class MainActivity extends Activity {
    private GearAccessoryProviderService _service = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      //  _service = new HelloAccessoryProviderService();
        AppLog.init((TextView) findViewById(R.id.logText));


        Button btnFakeAlert = ((Button)findViewById(R.id.btnFakeAlert));

        btnFakeAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GearAccessoryProviderService.instance.newAlert("Negative Sentiment", "Something else", "Agent: Kevin Glinski","1234");
            }
        });


        Button btnClearAlert = ((Button)findViewById(R.id.btnClearAlert));

        btnClearAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GearAccessoryProviderService.instance.clearAlerts();
            }
        });

        Button btnNewCall = ((Button)findViewById(R.id.btnNewCall));

        btnNewCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GearAccessoryProviderService.instance.newCall("Bob Loblaw", "(317) 222-2222", true, "1234");
            }
        });

        Button btnConnect = ((Button)findViewById(R.id.btnConnect));

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IcwsService.instance.connect();
            }
        });

        Intent intent = new Intent(this, IcwsService.class);
        startService(intent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
