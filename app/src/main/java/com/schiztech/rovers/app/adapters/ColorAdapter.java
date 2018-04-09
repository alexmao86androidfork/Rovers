package com.schiztech.rovers.app.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.support.annotation.ArrayRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.roveritems.IRover;
import com.schiztech.rovers.app.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.schiztech.rovers.app.utils.Utils;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersBaseAdapter;
/**
 * Created by schiz_000 on 10/24/2014.
 */
public class ColorAdapter extends BaseAdapter implements StickyGridHeadersBaseAdapter {
    private Context mContext ;
    private int mSelectedColor;
    private int[] mBasicColors;
    private Integer mDefaultColor = null;
    private List<Integer> mMaterialColors;
    private List<String> mHeaders = new ArrayList<String>();
    private List<Integer> mCounts;

    private static final int DEFAULT_COLOR_VALUE = IRover.ROVER_DEFAULT_COLOR_VALUE;

    public ColorAdapter(Context context, int selectedColor, Integer defaultColor) {
        mDefaultColor = defaultColor;


        init(context, selectedColor);
        populateDefaults(context);
    }

    public ColorAdapter(Context context, int selectedColor, int[] colorOptions) {
        init(context, selectedColor);

        mHeaders.add("");
        mBasicColors = colorOptions;

        //fixes weird problem with short color lists (1 row) that are cut on dialog view
        populateList("", new ArrayList<Integer>());

    }

    private void init(Context context, int selectedColor){
        mContext = context;

        if(mDefaultColor != null) {
            mHeaders.add(Utils.getString(mContext, R.string.colors_default));
        }

        mSelectedColor = selectedColor;

        mMaterialColors = new ArrayList<>();
        mCounts = new ArrayList<>();

    }

    private void populateDefaults(Context context){

        mHeaders.add(Utils.getString(mContext, R.string.colors_basic));
        mBasicColors = context.getResources().getIntArray(R.array.md_basic_colors_array);


        populateList(context, Utils.getString(mContext, R.string.colors_material), R.array.md_colors_reds_array);

        populateList(context, "", R.array.md_colors_pinks_array);

        populateList(context, "", R.array.md_colors_deep_purples_array);

        populateList(context, "", R.array.md_colors_indigos_array);

        populateList(context, "", R.array.md_colors_blues_array);

        populateList(context, "", R.array.md_colors_light_blues_array);

        populateList(context, "", R.array.md_colors_cyans_array);

        populateList(context, "", R.array.md_colors_teals_array);

        populateList(context, "", R.array.md_colors_greens_array);

        populateList(context, "", R.array.md_colors_light_greens_array);

        populateList(context, "", R.array.md_colors_limes_array);

        populateList(context, "", R.array.md_colors_yellows_array);

        populateList(context, "", R.array.md_colors_ambers_array);

        populateList(context, "", R.array.md_colors_oranges_array);

        populateList(context, "", R.array.md_colors_deep_oranges_array);

        populateList(context, "", R.array.md_colors_browns_array);

        populateList(context, "", R.array.md_colors_greys_array);

        populateList(context, "", R.array.md_colors_blue_greys_array);

        populateList(context, "", R.array.md_colors_blackwhite_array);
    }

    private void populateList(Context context, String title, @ArrayRes int colorsArray){
        populateList(title, Utils.asList(context.getResources().getIntArray(colorsArray)));
    }

    private void populateList(String title, Collection<? extends Integer> colors){
        mMaterialColors.addAll(colors);
        mCounts.add(colors.size());
        mHeaders.add(title);
    }

    @Override
    public int getCount() {
        return (mDefaultColor == null ? 0 : 1) + mBasicColors.length + mMaterialColors.size();
    }

    @Override
    public Object getItem(int position) {
        if (mDefaultColor != null) {
            position = position - 1;
        }

        if(position == -1) {
            return DEFAULT_COLOR_VALUE;
        }

        if (mBasicColors != null && position < mBasicColors.length && position >= 0)
            return mBasicColors[position];
        else {
            int fixedPos = position - (mBasicColors != null ? mBasicColors.length : 0);
            return mMaterialColors.get(fixedPos);
        }


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

        int color = (int)getItem(position);
        if(color != DEFAULT_COLOR_VALUE) {
            setColorViewWithValue(convertView.findViewById(R.id.color_view), color,
                    color == mSelectedColor);
        }
        else{
            setColorViewWithValue(convertView.findViewById(R.id.color_view), mDefaultColor,
                    color == mSelectedColor);
        }

        return convertView;
    }


    public void setSelectedColor(int selectedColor){
        mSelectedColor = selectedColor;
        notifyDataSetChanged();
    }


    public static void setColorViewWithValue(View view, int color, boolean selected) {
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            Resources res = imageView.getContext().getResources();

            Drawable currentDrawable = imageView.getDrawable();
            Drawable finishedDrawable;
            GradientDrawable colorChoiceDrawable;
            if (currentDrawable != null && currentDrawable instanceof GradientDrawable) {
                // Reuse drawable
                finishedDrawable = BitmapUtils.getCircularDrawable((GradientDrawable) currentDrawable, color, res, selected);
            } else {
                finishedDrawable = BitmapUtils.getCircularDrawable(color,res,selected);
            }

            imageView.setImageDrawable(finishedDrawable);

        } else if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        }
    }

    @Override
    public int getCountForHeader(int header) {
        if(mDefaultColor != null) {
            header = header - 1;
        }

        if(header == -1)
            return 1;//1 default color


        if(header == 0)
            return mBasicColors.length;


        return mCounts.get(header - 1);
    }

    @Override
    public int getNumHeaders() {
        return mHeaders.size();
    }


    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        TextView holder;
        if (convertView == null) {
            holder = (TextView) LayoutInflater.from(mContext).inflate(R.layout.gridview_header, parent, false);
        } else {
            holder = (TextView) convertView;
        }
        holder.setText(mHeaders.get(position));


        return holder;
    }

}