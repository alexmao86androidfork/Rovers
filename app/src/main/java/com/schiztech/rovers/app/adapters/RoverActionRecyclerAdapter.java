package com.schiztech.rovers.app.adapters;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.schiztech.rovers.api.RoversConstants;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.MarketUtils;
import com.schiztech.rovers.app.utils.SuggestedAction;
import com.schiztech.rovers.app.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by schiz_000 on 1/18/2015.
 */
public class RoverActionRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = LogUtils.makeLogTag("RoverActionRecyclerAdapter");

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private Intent mQueryIntent;
    private Context mContext;
    private PackageManager mPackageManager;
    List<RoverActionHolder> mInfos;

    //region On Actions Loaded
    private OnActionsLoadedListener mListener;

    public interface OnActionsLoadedListener {
        void OnActionsLoaded(int installedActions, int promotedActions);
    }

    public void clearOnActionsLoadedListener() {
        mListener = null;
    }

    private void callOnActionsLoadedListener(int installedActions, int promotedActions) {
        if (mListener != null) {
            mListener.OnActionsLoaded(installedActions, promotedActions);
        }
    }

    //endregion On Actions Loaded

    //region ViewHolder


    public static class RoverActionHolder {
        String hTitle;
        String hDescription;
        Drawable hIcon;
        ComponentName hSettingsActivity;
        boolean hIsPromotional;
        String hPackage;

        public static RoverActionHolder getInstance() {
            return new RoverActionHolder();
        }
    }

    public static class RoverActionViewHolder extends RecyclerView.ViewHolder {
        protected TextView vTitle;
        protected TextView vDescription;
        protected ImageView vIcon;
        protected View vSettings;
        protected View vDetails;
        protected View vInstall;


        public RoverActionViewHolder(View v) {
            super(v);
            vTitle = (TextView) v.findViewById(R.id.action_title);
            vDescription = (TextView) v.findViewById(R.id.action_description);
            vIcon = (ImageView) v.findViewById(R.id.action_icon);
            vSettings = v.findViewById(R.id.action_settings);
            vDetails = v.findViewById(R.id.action_details);
            vInstall = v.findViewById(R.id.action_install);
        }
    }

    class HeaderActionViewHolder extends RecyclerView.ViewHolder {
        public HeaderActionViewHolder(View v) {
            super(v);
        }
    }

    //endregion ViewHolder

    //region RecyclerView Methods

    public RoverActionRecyclerAdapter(Context context, OnActionsLoadedListener listener) {
        mListener = listener;
        init(context);
    }

    public RoverActionRecyclerAdapter(Context context) {
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mQueryIntent = new Intent(RoversConstants.INTENT_ACTION);
        mPackageManager = mContext.getPackageManager();

        new LoadRoverActionsTask().execute();


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int i) {

        if (vh instanceof HeaderActionViewHolder) {
            return;
        }

        if (vh instanceof RoverActionViewHolder) {
            RoverActionViewHolder viewHolder = (RoverActionViewHolder) vh;

            final RoverActionHolder holder = getItem(i);

            viewHolder.vTitle.setText(holder.hTitle);
            viewHolder.vTitle.setSelected(true);
            viewHolder.vIcon.setImageDrawable(holder.hIcon);

            //description
            if (holder.hDescription != null) {
                viewHolder.vDescription.setVisibility(View.VISIBLE);
                viewHolder.vDescription.setText(holder.hDescription);
            } else {
                viewHolder.vDescription.setVisibility(View.GONE);
            }

            //promotional action
            if (holder.hIsPromotional) {
                viewHolder.vDetails.setOnClickListener(null);
                viewHolder.vDetails.setVisibility(View.GONE);
                viewHolder.vInstall.setVisibility(View.VISIBLE);
                viewHolder.vInstall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri = Uri.parse(MarketUtils.getPackagePlayStoreLink(holder.hPackage, false));
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            mContext.startActivity(goToMarket);
                        } catch (ActivityNotFoundException e) {}

                        AnalyticsManager.getInstance(mContext).reportEvent(AnalyticsManager.Category.Actions,
                                AnalyticsManager.Action.Install_Request,
                                holder.hPackage);
                    }
                });
            } else {
                //dont show app info button for internal actions
                if(mContext.getPackageName().toLowerCase().equals(holder.hPackage)){
                    viewHolder.vDetails.setOnClickListener(null);
                    viewHolder.vDetails.setVisibility(View.GONE);
                }
                else {
                    viewHolder.vDetails.setVisibility(View.VISIBLE);
                    viewHolder.vDetails.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", holder.hPackage, null);
                            intent.setData(uri);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }
                    });
                }
                viewHolder.vInstall.setVisibility(View.GONE);
                viewHolder.vInstall.setOnClickListener(null);

            }


            //settings activity
            if (holder.hSettingsActivity != null) {
                viewHolder.vSettings.setVisibility(View.VISIBLE);
                viewHolder.vSettings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_MAIN, null);
                            intent.setComponent(holder.hSettingsActivity);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(mContext, Utils.getString(mContext, R.string.actions_settings_launch_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                viewHolder.vSettings.setVisibility(View.GONE);
                viewHolder.vSettings.setOnClickListener(null);
            }
        }

    }


    @Override
    public int getItemCount() {
        if (mInfos == null) return 0;
        return mInfos.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    private RoverActionHolder getItem(int position) {
        return mInfos.get(position - 1);
    }

    public void clear() {
        if (mInfos != null)
            mInfos.clear();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        if (viewType == TYPE_ITEM) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.list_item_action, viewGroup, false);

            return new RoverActionViewHolder(itemView);
        } else if (viewType == TYPE_HEADER) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.header_item_actions, viewGroup, false);

            return new HeaderActionViewHolder(itemView);
        }


        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");


    }

    //endregion RecyclerView Methods

    //region Load Actions

    private class LoadRoverActionsTask extends AsyncTask<Void, Void, Void> {
        int suggestedActions;

        @Override
        protected void onPreExecute() {
            LogUtils.LOGV(TAG, "loading task preExecute");
            mInfos = new ArrayList<RoverActionHolder>();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            ArrayList<RoverActionHolder> installedActions= new ArrayList<>();
            List<SuggestedAction> promotionalActions = SuggestedAction.getSuggestedActions(mContext);

            //region Load Install Actions

            List<ResolveInfo> resolveInfos = mPackageManager.queryIntentActivities(mQueryIntent, PackageManager.GET_META_DATA);
            for (ResolveInfo ri : resolveInfos) {
                boolean isSupported = true;
                RoverActionHolder holder = RoverActionHolder.getInstance();
                holder.hIsPromotional = false;
                holder.hIcon = ri.loadIcon(mPackageManager);
                holder.hTitle = ri.loadLabel(mPackageManager).toString();
                holder.hPackage = ri.activityInfo.packageName;
                // ComponentName componentName = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);

                try {
                    Bundle metaData = ri.activityInfo.metaData;//ai.metaData;
                    if (metaData != null) {
                        holder.hDescription = metaData.getString(RoversConstants.METADATA_DESCRIPTION, null);

                        String settingsActivity = metaData.getString(RoversConstants.METADATA_SETTINGS_ACTIVITY, null);
                        if (settingsActivity != null) {
                            holder.hSettingsActivity = new ComponentName(ri.activityInfo.packageName, settingsActivity);
                        }

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
                    installedActions.add(holder);
                }
            }

            //sort before adding promos
            Collections.sort(installedActions, new Comparator<RoverActionHolder>() {
                @Override
                public int compare(RoverActionHolder info1, RoverActionHolder info2) {
                    return info1.hTitle.compareTo(info2.hTitle);
                }
            });

            resolveInfos.clear();
            //endregion Load Install Actions

            //region Load Promotional Actions


            //check if promo is already installed, if not - show in the list;
            for (SuggestedAction action : promotionalActions) {
                boolean isInstalled = false;
                for (RoverActionHolder holder : installedActions) {
                    if (holder.hPackage != null && action.packageName != null) {
                        if (holder.hPackage.toLowerCase().equals(action.packageName.toLowerCase())) {
                            isInstalled = true;
                            break;
                        }
                    }
                }

                if (!isInstalled) {
                    RoverActionHolder holder = RoverActionHolder.getInstance();
                    holder.hDescription = action.description;
                    holder.hPackage = action.packageName;
                    holder.hSettingsActivity = null;
                    holder.hIcon = Utils.getDrawable(mContext, action.iconRes);
                    holder.hTitle = action.title;
                    holder.hIsPromotional = true;

                    mInfos.add(holder);
                }

            }

            //endregion


            suggestedActions = mInfos.size();
            LogUtils.LOGV(TAG, "loading task (pre-installed) loaded  " + (mInfos.size() + 1) + " objects");

            mInfos.addAll(installedActions);

            LogUtils.LOGV(TAG, "loading task (post-installed) loaded  " + (mInfos.size() + 1) + " objects");

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            LogUtils.LOGV(TAG, "loading task postExecute, installed: "+ (mInfos.size() + 1 - suggestedActions) + " | suggested: "+suggestedActions);

            callOnActionsLoadedListener(mInfos.size() + 1 - suggestedActions,suggestedActions);
            notifyDataSetChanged();
        }

    }

    //endregion Load Actions



}
