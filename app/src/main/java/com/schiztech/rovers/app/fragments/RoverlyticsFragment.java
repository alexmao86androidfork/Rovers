package com.schiztech.rovers.app.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.RoverlyticsResultActivity;
import com.schiztech.rovers.app.fragments.base.SupportFragmentBase;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.utils.MarketUtils;
import com.schiztech.rovers.app.utils.PrefUtils;
import com.schiztech.rovers.app.utils.RoverlyticsUtils;
import com.schiztech.rovers.app.utils.Utils;

import java.text.DecimalFormat;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoverlyticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoverlyticsFragment extends SupportFragmentBase {
    View mRootView;
    int mTotalLaunches = 0;
    float mTotalTime = 0;
    float mTotalDistance = 0;

    static final int UPDATE_ROVERLYTICS_REQUEST = 888;


    //region Factory
    public static RoverlyticsFragment newInstance(String param1, String param2) {
        RoverlyticsFragment fragment = new RoverlyticsFragment();
        return fragment;
    }

    public RoverlyticsFragment() {
        // Required empty public constructor
    }

    //endregion

    //region Fragment States
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_roverlytics, container, false);
        initViews();
        return mRootView;
    }

    @Override
    public void onStart(){
        super.onStart();

        syncRoverlyticsFields();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == UPDATE_ROVERLYTICS_REQUEST) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                updateFields(data);
            }
        }
    }


    @Override
    public void onDestroyView() {
        if(mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        if(mRootView != null) {
            Utils.unbindDrawables(mRootView);
        }
        mShareClickListener = null;

        mRateClickListener = null;

        mStatsShareClickListener = null;


        Utils.unbindDrawables(mRootView);
        System.gc();
        super.onDestroyView();
    }

    //endregion

    //region Views Methods

    private void initViews(){
        View header = mRootView.findViewById(R.id.roverlytics_headerLayout);
        ViewCompat.setElevation(header, getResources().getDimensionPixelSize(R.dimen.toolbar_elevation));

        View statsLayout = mRootView.findViewById(R.id.roverlytics_statsLayout);
        ViewCompat.setElevation(statsLayout, getResources().getDimensionPixelSize(R.dimen.toolbar_elevation) / 2);

        mRootView.findViewById(R.id.roverlytics_shareBtn).setOnClickListener(mShareClickListener);
        mRootView.findViewById(R.id.roverlytics_rateBtn).setOnClickListener(mRateClickListener);

        mRootView.findViewById(R.id.roverlytics_avgTimeShareBtn).setOnClickListener(mStatsShareClickListener);
        mRootView.findViewById(R.id.roverlytics_totalDistanceShareBtn).setOnClickListener(mStatsShareClickListener);
        mRootView.findViewById(R.id.roverlytics_totalLaunchesShareBtn).setOnClickListener(mStatsShareClickListener);

    }

    private static final int SYNC_DELAY = 1000;
    Handler mHandler;
    private void syncRoverlyticsFields(){
        if(mHandler == null){
            mHandler = new Handler();
        }
        //when launched with walkthrough causes the main activity to show on top
        if(PrefUtils.getWalkthroughIsFinishedValue(getContext()) == true) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent roverlyticsResult = new Intent(getContext(), RoverlyticsResultActivity.class);
                    startActivityForResult(roverlyticsResult, UPDATE_ROVERLYTICS_REQUEST);
                }
            }, SYNC_DELAY);
        }
        else{
            updateViews();//update with initial values
        }
    }

    private void updateFields(Intent updatedData){
        if(updatedData.getExtras() != null){
            Bundle extras = updatedData.getExtras();
            mTotalLaunches = extras.getInt(RoverlyticsUtils.TOTAL_LAUNCHES, mTotalLaunches);
            mTotalTime= extras.getFloat(RoverlyticsUtils.TOTAL_TIME, mTotalTime);
            mTotalDistance = extras.getFloat(RoverlyticsUtils.TOTAL_DISTANCE, mTotalDistance);

            updateViews();
        }


    }

    private void updateViews(){
        DecimalFormat intFormatter = new DecimalFormat("#,###");

        if(mRootView != null) {
            ((TextView) mRootView.findViewById(R.id.roverlytics_totalLaunchesLabel)).setText(intFormatter.format(mTotalLaunches));
            ((TextView) mRootView.findViewById(R.id.roverlytics_avgTimeLabel)).setText(getTimeString());
            ((TextView) mRootView.findViewById(R.id.roverlytics_totalDistanceLabel)).setText(getDistanceString());
        }
    }


    private String getTimeString(){
        DecimalFormat floatFormatter = new DecimalFormat("#,###.00");
        String timeMessage = "-";
        float avgTime = 0;
        if(mTotalLaunches > 0) {
            avgTime = mTotalTime / mTotalLaunches;
        }

        if(RoverlyticsUtils.getDays(avgTime) > 1){
            timeMessage = floatFormatter.format(RoverlyticsUtils.getDays(avgTime)) + " " + Utils.getString(getContext(), R.string.roverlytics_days);
        }
        else if(RoverlyticsUtils.getHours(avgTime) > 1){
            timeMessage = floatFormatter.format(RoverlyticsUtils.getHours(avgTime)) + Utils.getString(getContext(), R.string.roverlytics_hours);
        }
        else if(RoverlyticsUtils.getMinutes(avgTime) > 1){
            timeMessage = floatFormatter.format(RoverlyticsUtils.getMinutes(avgTime)) + Utils.getString(getContext(), R.string.roverlytics_minutes);
        }
        else{
            timeMessage = floatFormatter.format(avgTime) + Utils.getString(getContext(), R.string.roverlytics_seconds);
        }

        return timeMessage;
    }
    private String getDistanceString(){
        float inchesDistance = mTotalDistance;
        if(RoverlyticsUtils.getDistanceUnitByLocale() == RoverlyticsUtils.DistanceUnits.Imperial){
            return getImperialDistanceString(inchesDistance);
        }

        return getMetricDistanceString(inchesDistance);

    }
    private String getMetricDistanceString(float inchesDistance){
        DecimalFormat floatFormatter = new DecimalFormat("#,###.00");
        String metricMessage = "-";

        if(RoverlyticsUtils.getKilometers(inchesDistance) > 1){
            metricMessage = floatFormatter.format(RoverlyticsUtils.getKilometers(inchesDistance)) + Utils.getString(getContext(), R.string.roverlytics_kilometers);
        }
        else if(RoverlyticsUtils.getMeters(inchesDistance) > 1){
            metricMessage = floatFormatter.format(RoverlyticsUtils.getMeters(inchesDistance)) + Utils.getString(getContext(), R.string.roverlytics_meters);
        }
        else{
            metricMessage = floatFormatter.format(RoverlyticsUtils.getCentemeters(inchesDistance)) + Utils.getString(getContext(), R.string.roverlytics_centimeters);
        }

        return metricMessage;
    }
    private String getImperialDistanceString(float inchesDistance){
        DecimalFormat floatFormatter = new DecimalFormat("#,###.00");
        String imperialMessage = "-";

        if(RoverlyticsUtils.getMiles(inchesDistance) > 1){
            imperialMessage = floatFormatter.format(RoverlyticsUtils.getMiles(inchesDistance)) + Utils.getString(getContext(), R.string.roverlytics_miles);
        }
        else if(RoverlyticsUtils.getYards(inchesDistance) > 1){
            imperialMessage = floatFormatter.format(RoverlyticsUtils.getYards(inchesDistance)) + Utils.getString(getContext(), R.string.roverlytics_yards);
        }
        else if(RoverlyticsUtils.getFeet(inchesDistance) > 1){
            imperialMessage = floatFormatter.format(RoverlyticsUtils.getYards(inchesDistance)) + Utils.getString(getContext(), R.string.roverlytics_feet);
        }
        else{
            imperialMessage = floatFormatter.format(inchesDistance) + Utils.getString(getContext(), R.string.roverlytics_inches);
        }

        return imperialMessage;

    }

    //endregion

    //region OnClickListeners

    View.OnClickListener mStatsShareClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String subject = "";
            String body = "";
            String label = "";
            Long value = null;
            switch (view.getId()){
                case R.id.roverlytics_totalLaunchesShareBtn:
                    subject = Utils.getString(getContext(), R.string.roverlytics_share_launches_title);
                    body = Utils.getString(getContext(), R.string.roverlytics_share_launches_desc_1)+" " +mTotalLaunches+" " +
                            Utils.getString(getContext(), R.string.roverlytics_share_launches_desc_2);
                    label = "Roverlytics_ShareTotalLaunches";
                    value = (long)mTotalLaunches;
                    break;
                case R.id.roverlytics_avgTimeShareBtn:
                    subject = Utils.getString(getContext(), R.string.roverlytics_share_time_title);
                    body = Utils.getString(getContext(), R.string.roverlytics_share_time_desc_1)+" " + getTimeString() + " " +
                            Utils.getString(getContext(), R.string.roverlytics_share_time_desc_2);
                    label = "Roverlytics_ShareAvgTime";
                    break;
                case R.id.roverlytics_totalDistanceShareBtn:
                    subject = Utils.getString(getContext(), R.string.roverlytics_share_distance_title);
                    body = Utils.getString(getContext(), R.string.roverlytics_share_distance_desc_1)+" " + getDistanceString() +" "+Utils.getString(getContext(), R.string.roverlytics_share_distance_desc_2);
                    label = "Roverlytics_ShareTotalDistance";
                    value = (long)mTotalDistance;
                    break;
            }

            body += " " + Utils.getString(getContext(), R.string.roverlytics_share_beat_that) + getShareSuffix();
            shareContent(subject, body);

            //report to analytics
            AnalyticsManager.getInstance(getContext()).reportEvent(AnalyticsManager.Category.UX,
                                                                    AnalyticsManager.Action.Button_Click,
                                                                    label,
                                                                    value);
        }
    };

    View.OnClickListener mShareClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AnalyticsManager.getInstance(getContext()).reportEvent(AnalyticsManager.Category.UX,
                    AnalyticsManager.Action.Button_Click,
                    "Roverlytics_Share");

            shareContent(Utils.getString(getContext(), R.string.roverlytics_share_title), Utils.getString(getContext(), R.string.roverlytics_share_desc) + getShareSuffix());
        }
    };


    View.OnClickListener mRateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AnalyticsManager.getInstance(getContext()).reportEvent(AnalyticsManager.Category.UX,
                    AnalyticsManager.Action.Button_Click,
                    "Roverlytics_Rate");

            Utils.browseLink(getContext(),MarketUtils.getPackagePlayStoreLink(getContext().getPackageName(),false));
        }
    };
    //endregion OnClickListeners

    private String getShareSuffix(){
        return "\n" + Utils.getString(getContext(), R.string.roverlytics_share_get_it) +" " +
                MarketUtils.getPackagePlayStoreLink(getContext().getPackageName(),true) + " " + Utils.getString(getContext(), R.string.hashtag);
    }

    private void shareContent(@StringRes int subjectRes, @StringRes int bodyRes) {
        shareContent(Utils.getString(getContext(), subjectRes), Utils.getString(getContext(), bodyRes));
    }
        private void shareContent(String subject, String body){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.putExtra(Intent.EXTRA_TEXT, body);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);

    }

    private Context getContext(){
        return getActivity().getApplicationContext();
    }

    @Override
    protected String getFragmentTag() {
        return Utils.getString(getContext(), R.string.fragment_roverlytics);

    }
}
