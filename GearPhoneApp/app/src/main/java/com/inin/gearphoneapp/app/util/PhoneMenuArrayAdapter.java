package com.inin.gearphoneapp.app.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.inin.gearphoneapp.app.R;

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
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.phone_menu_row_layout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        textView.setText(values[position]);

        //{"Answer","Mute","Hold","Speaker","Disconnect"}
        switch(position){
            case 0:{
                imageView.setImageResource(R.drawable.phone_call);
                break;
            }
            case 1:{
                imageView.setImageResource(R.drawable.callcontrol_mute);
                break;
            }
            case 2:{
                imageView.setImageResource(R.drawable.callcontrol_hold);
                break;
            }
            case 3:{
                imageView.setImageResource(R.drawable.callcontrol_speaker);
                break;
            }
            case 4:{
                imageView.setImageResource(R.drawable.callcontrol_disconnect);
                break;
            }
        }

        return rowView;
    }
}