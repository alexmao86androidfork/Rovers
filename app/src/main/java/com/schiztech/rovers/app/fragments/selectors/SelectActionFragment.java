package com.schiztech.rovers.app.fragments.selectors;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;

import com.schiztech.rovers.api.RoversConstants;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.adapters.ActivityListAdapter;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.roveritems.IRover;
import com.schiztech.rovers.app.roveritems.actions.ExtendedRoverActionBuilder;
import com.schiztech.rovers.app.utils.ActivityInfo;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.MarketUtils;
import com.schiztech.rovers.app.utils.SuggestedAction;
import com.schiztech.rovers.app.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by schiz_000 on 10/2/2014.
 */
public class SelectActionFragment  extends SelectIntentFragment {
    public static final String TAG = LogUtils.makeLogTag("SelectActionFragment");

    public static int REQUEST_CREATE_ACTION = 2;

    private Button mGetMoreButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater,container,savedInstanceState);

        mGetMoreButton = (Button)v.findViewById(R.id.button);
        mGetMoreButton.setText(R.string.actions_get_more);
        mGetMoreButton.setTextColor(Utils.getColor(getActivity().getApplicationContext(), R.color.md_green_500));
        mGetMoreButton.setVisibility(View.VISIBLE);
        mGetMoreButton.setOnClickListener(mGetMoreButtonClick);
        return v;
    }

    View.OnClickListener mGetMoreButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(getActivity() != null) {
                AnalyticsManager.getInstance(getActivity().getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                        AnalyticsManager.Action.Button_Click,
                        "AddRover_GetMoreActions");

                Utils.navigateTo(getActivity().getApplicationContext(), R.string.fragment_actions);
                getActivity().finish();
            }
        }
    };

    @Override
    public void onDestroyView(){
        if(mGetMoreButton != null)
        {
            mGetMoreButton.setOnClickListener(null);
            mGetMoreButton = null;
        }

        super.onDestroyView();
    }


    public static SelectIntentFragment newInstance() {
        return new SelectActionFragment();
    }

    @Override
    protected Intent getIntentQuery() {
        return new Intent(RoversConstants.INTENT_ACTION);
    }

    @Override
    protected int getQueryFlags() {
        return PackageManager.GET_META_DATA;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        ActivityInfo info = (ActivityInfo)mAdapter.getItem(position);

        if(info.isForDownload){//suggested action - needs to browse for download
            String downloadLink = MarketUtils.getPackagePlayStoreLink(info.componentName.getPackageName(), false);
            Utils.browseLink(getActivity().getApplicationContext(), downloadLink);

            getActivity().finish();
        }

        else {
            AnalyticsManager.getInstance(getActivity().getApplicationContext()).reportEvent(AnalyticsManager.Category.Actions,
                    AnalyticsManager.Action.Selected,
                    info.label.toString());
            LogUtils.LOGD(TAG, "calling onActivityResult");
            startActivityForResult(mAdapter.getIntent(position),
                    REQUEST_CREATE_ACTION);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.LOGD(TAG, "onActivityResult");
        if (requestCode == REQUEST_CREATE_ACTION && resultCode == Activity.RESULT_OK) {
            IRover.RoverType type = IRover.RoverType.BasicAction;
            if(data.getExtras() != null && data.getExtras().getBoolean(ExtendedRoverActionBuilder.KEY_IS_INTERACTIVE, false) == true){
                type = IRover.RoverType.InteractiveAction;
            }
            callOnIntentSelected(data, type);
        }
    }

    @Override
    protected String getFragmentTag() {
        return Utils.getString(getActivity().getApplicationContext(), R.string.fragment_select_action);
    }

    @Override
    public void onItemsLoaded(List<ActivityInfo> items) {
        if(getActivity() != null){
            new LoadSuggestedActions(getActivity().getApplicationContext(), items).execute();
        }
    }



    private class LoadSuggestedActions extends AsyncTask<Void, Void, Void> {
        List<SuggestedAction> mSuggestedActions;
        List<ActivityInfo> mInfos;
        Context mContext;

        public LoadSuggestedActions(Context context, List<ActivityInfo> items){
            mInfos = items;
            mContext = context;
        }
        @Override
        protected void onPreExecute (){
            LogUtils.LOGV(TAG, "loading suggested actions");
            mSuggestedActions = SuggestedAction.getSuggestedActions(mContext);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            LogUtils.LOGV(TAG, "adding suggested actions");
            if(mAdapter == null || mInfos == null)
                return null;

            for(SuggestedAction action : mSuggestedActions){
                if(!isInfosContainingPackage(action.packageName)){
                    mAdapter.addItem(action.toActivityInfo(mContext));
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mInfos = null;
            mContext = null;
            mSuggestedActions = null;
            mAdapter.notifyDataSetChanged();
        }

        private boolean isInfosContainingPackage(String packageName){
            for(ActivityInfo info : mInfos){
                if(info.componentName != null && info.componentName.getPackageName() != null &&
                        info.componentName.getPackageName().toLowerCase().equals(packageName)){

                    return true;
                }
            }

            return false;
        }

    }
}