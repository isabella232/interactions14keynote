package com.inin.gearphoneapp.app.plantronics;

import android.os.Handler;
import android.os.Message;

import com.inin.gearphoneapp.app.icws.IcwsService;
import com.inin.gearphoneapp.app.plantronics.PlantronicsXEventMessage;
import com.inin.gearphoneapp.app.plantronics.PlantronicsReceiver;
import android.util.Log;
import com.inin.gearphoneapp.app.icws.QueueWatcher;

/**
 * Created by kevin.glinski on 5/16/14.
 */
public class BluetoothHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case PlantronicsReceiver.HEADSET_EVENT:
                PlantronicsXEventMessage message = (PlantronicsXEventMessage)msg.obj;

                if(message.getEventType().equalsIgnoreCase("DON")){
                    //Headset is now on
                    Log.d("BluetoothHandler", "Headset is now on");

                    if(IcwsService.instance != null && IcwsService.instance.WorkgroupQueueReceiver != null) {
                        IcwsService.instance.WorkgroupQueueReceiver.joinCall();
                    }
                }
                else if(message.getEventType().equalsIgnoreCase("DOFF")){
                    //Headset is now on
                    Log.d("BluetoothHandler", "Headset is now Off");

                    if(IcwsService.instance != null && IcwsService.instance.UserQueueReceiver != null) {
                        IcwsService.instance.UserQueueReceiver.disconnectCall();
                    }

                }

                Log.d("BluetoothHandler", message.toString());
                break;
            default:
                break;
        }
    }
}