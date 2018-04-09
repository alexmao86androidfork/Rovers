package com.schiztech.rovers.app.fragments.selectors;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.crashlytics.android.Crashlytics;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.adapters.ActivityListAdapter;
import com.schiztech.rovers.app.fragments.base.FragmentBase;
import com.schiztech.rovers.app.roveritems.IRover;
import com.schiztech.rovers.app.utils.ActivityInfo;
import com.schiztech.rovers.app.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schiz_000 on 10/24/2014.
 */
public class SelectRecentFragment extends FragmentBase implements AdapterView.OnItemClickListener{

    private View mLayout;
    protected ActivityListAdapter mAdapter;

    @Override
    protected String getFragmentTag() {
        return Utils.getString(getActivity().getApplicationContext(), R.string.fragment_select_recent);

    }

    public interface OnIntentSelectedListener {
        void onIntentSelected(Intent selectedIntent, IRover.RoverType roverItemType);
    }
    private OnIntentSelectedListener mListener;
    private GridView mGridView;
    private boolean mIsLightTheme = false;


    protected void callOnIntentSelected(Intent intent, IRover.RoverType roverItemType){
        if(mListener != null)
            mListener.onIntentSelected(intent, roverItemType);
    }


    public void setIsLightTheme(boolean isLightTheme){
        mIsLightTheme = isLightTheme;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
            callOnIntentSelected(mAdapter.getIntent(position), IRover.RoverType.Application);
    }

    //region Fragment

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnIntentSelectedListener) activity;
    }

    @Override
    public void onDetach(){
        mListener = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.fragment_select_intent, container, false);
        mGridView = (GridView) mLayout.findViewById(R.id.gridview);
        mGridView.setEmptyView(mLayout.findViewById(R.id.progressBar));
        mAdapter = new ActivityListAdapter(getActivity(), new ArrayList<ActivityInfo>(), mIsLightTheme);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        new LoadRecentInfoTask().execute();
        return mLayout;
    }

    @Override
    public void onDestroyView(){
        mListener = null;
        mGridView.setOnItemClickListener(null);
        mGridView.setAdapter(null);
        mAdapter.clear();
        mAdapter = null;
        mGridView = null;
        mListener = null;
        Utils.unbindDrawables(mLayout);
        mLayout = null;

        super.onDestroyView();
    }

    //endregion

    private void updateEmptyView(){
        if(mAdapter != null && mAdapter.getCount() == 0){
            mGridView.setEmptyView(mLayout.findViewById(R.id.empty_text));
            mLayout.findViewById(R.id.progressBar).setVisibility(View.GONE);
        }
    }

    private class LoadRecentInfoTask extends AsyncTask<Void, Void, Void> {
        private final int MAX_RECENT_APPS_DISPLAYED = 9;
        @Override
        protected void onPreExecute (){
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                //loading double what we need - so if we clear not wanted apps we still have the amount.
                List<ActivityManager.RecentTaskInfo> runningTaskInfos = null;

                if (activityManager != null) {
                    runningTaskInfos = activityManager.getRecentTasks(MAX_RECENT_APPS_DISPLAYED * 2, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
                }

                //create empty list if null
                if (runningTaskInfos == null) {
                    runningTaskInfos = new ArrayList<>();
                }

                if (runningTaskInfos.size() > 0) {
                    runningTaskInfos.remove(0);//always remove first - it's this activity...
                }

                for (ActivityManager.RecentTaskInfo ri : runningTaskInfos) {
                    ActivityInfo ai = new ActivityInfo();

                    Intent intent = new Intent(ri.baseIntent);
//                if ( ri.origActivity != null) {
//                    intent.setComponent( ri.origActivity);
//                }
                    if (intent.getCategories() != null && intent.getCategories().contains(Intent.CATEGORY_HOME)) {
                        continue;//don't add a launcher intent!
                    }

                    if (ri.baseIntent.getComponent() == null) {
                        continue;//don't try to find the launch intent for non-component task
                    }
                    if (getActivity() != null) {
                        PackageManager pm = getActivity().getPackageManager();
                        Intent launchIntent = pm.getLaunchIntentForPackage(ri.baseIntent.getComponent().getPackageName());
                        ResolveInfo resolveInfo = pm.resolveActivity(launchIntent, 0);

                        ai.icon = resolveInfo.loadIcon(pm);
                        ai.label = resolveInfo.loadLabel(pm);
                        ai.componentName = new ComponentName(resolveInfo.activityInfo.packageName,
                                resolveInfo.activityInfo.name);

                        mAdapter.addItem(ai);
                    }
                    if (mAdapter.getCount() == MAX_RECENT_APPS_DISPLAYED)
                        break;//stop adding when reached max.
                }
                runningTaskInfos.clear();
            }

            catch(Exception e){
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(mAdapter != null) {
                mAdapter.notifyDataSetChanged();
                updateEmptyView();
            }

        }

    }


}
