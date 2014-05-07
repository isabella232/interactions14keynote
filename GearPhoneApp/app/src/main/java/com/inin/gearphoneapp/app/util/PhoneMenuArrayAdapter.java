package com.inin.gearphoneapp.app.util;

import android.content.Context;
import android.net.sip.SipAudioCall;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.inin.gearphoneapp.app.R;
import com.inin.gearphoneapp.app.Sip.HelperModel;
import com.inin.gearphoneapp.app.Sip.SipModel;

public class PhoneMenuArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public PhoneMenuArrayAdapter(Context context, String[] values) {
        super(context, R.layout.phone_menu_row_layout, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.v(HelperModel.TAG,"getView");
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.phone_menu_row_layout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        textView.setText(values[position]);

        SipModel sipModel = SipModel.GetInstance(null);
        SipAudioCall sipAudioCall = sipModel.getCall();

        //{"Answer","Mute","Hold","Speaker","Disconnect"}
        switch(position){
            case 0:{
                imageView.setImageResource(R.drawable.callcontrol_pickup2);
                break;
            }
            case 1:{
                imageView.setImageResource(R.drawable.callcontrol_mute);
                if (sipAudioCall != null && sipAudioCall.isMuted())
                        imageView.setBackgroundResource(R.drawable.border_background);
                break;
            }
            case 2:{
                imageView.setImageResource(R.drawable.callcontrol_hold);
                if (sipAudioCall != null && sipAudioCall.isOnHold())
                    imageView.setBackgroundResource(R.drawable.border_background);
                break;
            }
            case 3:{
                imageView.setImageResource(R.drawable.callcontrol_speaker);
                if (sipAudioCall != null && sipModel.isSpeakerphoneOn())
                    imageView.setBackgroundResource(R.drawable.border_background);
                break;
            }
            case 4:{
                imageView.setImageResource(R.drawable.callcontrol_disconnect);
                break;
            }
            case 5:{
                imageView.setImageResource(R.drawable.telephone);
                break;
            }
        }

        Log.v(HelperModel.TAG,"getViewDone");
        return rowView;
    }
}