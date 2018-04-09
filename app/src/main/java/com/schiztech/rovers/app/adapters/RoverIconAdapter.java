package com.schiztech.rovers.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.utils.RoversUtils;
import com.schiztech.rovers.app.utils.Utils;

/**
 * Created by schiz_000 on 10/24/2014.
 */
public class RoverIconAdapter extends BaseAdapter {
    private RoversUtils.RoverIcon[] mIconKeys;
    private Context mContext ;


    public RoverIconAdapter(Context context) {
        mContext = context;
        mIconKeys = RoversUtils.RoverIcon.values();
    }

    @Override
    public int getCount() {
        return mIconKeys.length;
    }

    @Override
    public Object getItem(int position) {
        return mIconKeys[position];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.grid_item_color, container, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.color_view);
        imageView.setImageResource(mIconKeys[position].getResourceID());

        return convertView;
    }
}