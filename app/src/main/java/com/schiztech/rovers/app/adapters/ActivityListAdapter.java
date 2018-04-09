package com.schiztech.rovers.app.adapters;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.schiztech.rovers.api.RoversConstants;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.utils.ActivityInfo;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by schiz_000 on 10/24/2014.
 */
public class ActivityListAdapter extends BaseAdapter {
    private static final String TAG = LogUtils.makeLogTag("ActivityListAdapter");

    private Intent mQueryIntent;
    private int mQueryFlags;
    private Context mContext;
    private PackageManager mPackageManager;
    private List<ActivityInfo> mInfos;
    private boolean mIsLightText = false;

    //region OnItemsLoadedListener

    public interface OnItemsLoadedListener{
        void onItemsLoaded(List<ActivityInfo> items);
    }

    private OnItemsLoadedListener mListener;

    public void setOnItemsLoadedListener(OnItemsLoadedListener listener){
        mListener = listener;
    }

    //endregion OnItemsLoadedListener

    public ActivityListAdapter(Context context, List<ActivityInfo> infos, boolean isLightText) {
        init(context,isLightText);
        mQueryIntent = new Intent(Intent.ACTION_MAIN);
        mQueryFlags = 0;
        mInfos = infos;
    }

    public ActivityListAdapter(Context context, Intent queryIntent, int queryFlags, boolean isLightText, OnItemsLoadedListener listener) {
        init(context,isLightText);

        mQueryIntent = queryIntent;
        mQueryFlags = queryFlags;
        mPackageManager = mContext.getPackageManager();
        mListener = listener;
        new LoadActivityInfoTask().execute();
    }

    private void init(Context context, boolean isLightText){
        mContext = context;
        mIsLightText = isLightText;
    }

    @Override
    public int getCount() {
        if(mInfos == null) return 0;
        return mInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mInfos.get(position);
    }



    public Intent getIntent(int position) {
        ActivityInfo item = (ActivityInfo)getItem(position);
        if(item.isForDownload){
            return null;
        }

        //else
        return new Intent(mQueryIntent)
                .setComponent(item.componentName);
    }

    public void addItem(ActivityInfo newItem){
        mInfos.add(newItem);
    }

    public void addAll(Collection<? extends ActivityInfo> newItems){
        mInfos.addAll(newItems);
    }

    @Override
    public long getItemId(int position) {
        return mInfos.get(position).componentName.hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.list_item_intent, container, false);
        }

        ActivityInfo ai = mInfos.get(position);
        ((TextView) convertView.findViewById(android.R.id.text1))
                .setText(ai.label);
        if(mIsLightText){
            ((TextView) convertView.findViewById(android.R.id.text1))
                    .setTextAppearance(mContext,R.style.AppTheme_TextAppearance_LightWithShadow);
        }

        ((ImageView) convertView.findViewById(android.R.id.icon))
                .setImageDrawable(ai.icon);


        convertView.findViewById(R.id.downloadIcon).setVisibility(ai.isForDownload ? View.VISIBLE : View.GONE);


        return convertView;
    }

    public void clear(){
        if(mInfos != null)
            mInfos.clear();
    }

    private class LoadActivityInfoTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute (){
            LogUtils.LOGV(TAG, "loading task preExecute");
            mInfos = new ArrayList<ActivityInfo>();
        }

        @Override
        protected String doInBackground(Void... voids) {
            LogUtils.LOGV(TAG, "loading task background process");

            try {
//                mQueryFlags |= PackageManager.MATCH_DEFAULT_ONLY;
                List<ResolveInfo> resolveInfos = mPackageManager.queryIntentActivities(mQueryIntent, mQueryFlags);

                for (ResolveInfo ri : resolveInfos) {
                    boolean isSupported = true;
                    ActivityInfo ai = new ActivityInfo();
                    ai.icon = ri.loadIcon(mPackageManager);
                    ai.label = ri.loadLabel(mPackageManager);
                    ai.componentName = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);

                    try {
                        Bundle metaData = ri.activityInfo.metaData;//ai.metaData;
                        if (metaData != null) {
                            int minSDK = metaData.getInt(RoversConstants.METADATA_MIN_SDK, Integer.MIN_VALUE);
                            if (minSDK != Integer.MIN_VALUE) {
                                isSupported = Utils.isAndroidVersionEqualOrAbove(minSDK);
                            }

                            int maxSDK = metaData.getInt(RoversConstants.METADATA_MAX_SDK, Integer.MIN_VALUE);
                            if (maxSDK != Integer.MIN_VALUE) {
                                isSupported = Utils.isAndroidVersionEqualOrBelow(maxSDK);
                            }
                        }
                    } catch (Exception e) {
                        //error with the action's metadata
                    }
                    if (isSupported) {
                        mInfos.add(ai);
                    }
                }
                resolveInfos.clear();

                Collections.sort(mInfos, new Comparator<ActivityInfo>() {
                    @Override
                    public int compare(ActivityInfo activityInfo, ActivityInfo activityInfo2) {
                        return activityInfo.label.toString().compareTo(
                                activityInfo2.label.toString());
                    }
                });

                LogUtils.LOGV(TAG, "loading task loaded " + (resolveInfos.size() + 1) + " objects");
            }

            catch (Exception e){
                LogUtils.LOGE(TAG, "ERROR while loading activity list: " + e.getMessage());

                return e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            LogUtils.LOGV(TAG, "loading task postExecute");

            if(result != null && mContext != null){
                Toast.makeText(mContext, Utils.getString(mContext, R.string.error_items_load), Toast.LENGTH_SHORT).show();
            }
            else {//error happened

            }

            notifyDataSetChanged();

            if(mListener != null){
                mListener.onItemsLoaded(mInfos);
            }
        }

    }


}