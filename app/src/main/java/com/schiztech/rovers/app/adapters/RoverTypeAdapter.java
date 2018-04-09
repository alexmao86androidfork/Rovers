package com.schiztech.rovers.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.schiztech.rovers.app.R;

/**
 * Created by schiz_000 on 10/24/2014.
 */
public class RoverTypeAdapter extends BaseAdapter {
    private Context mContext;

    // Constructor
    public RoverTypeAdapter(Context context) {
        mContext = context;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_intent, parent,false);

            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            ImageView imageView = (ImageView) convertView.findViewById(android.R.id.icon);

            textView.setText(mTextsIds[position]);
            textView.setAllCaps(true);
            imageView.setImageResource(mThumbIds[position]);
        }

        return convertView;
    }

    // Keep all Images in array
    public Integer[] mThumbIds = {
            R.drawable.ic_type_app, R.drawable.ic_type_shortcut,
            R.drawable.ic_type_action, R.drawable.ic_type_folder
    };

    public int[] mTextsIds = {
            R.string.addrover_apps, R.string.addrover_shortcuts, R.string.addrover_actions, R.string.addrover_folder
    };
}