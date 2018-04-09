package com.schiztech.rovers.app.managers;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.schiztech.rovers.app.BuildConfig;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.PrefUtils;

/**
 * Created by schiz_000 on 4/8/2015.
 */
public class AnalyticsManager {

    private static final String TAG = LogUtils.makeLogTag("AnalyticsManager");

    private static final String PROPERTY_ID = "UA-44913550-2";
    private Tracker mAnalyticsTracker;
    private Context mApplicationContext;

    //region Singelton Fields & C'tor
    private AnalyticsManager(Context applicationContext) {
        this.mApplicationContext = applicationContext;
    }

    private static AnalyticsManager sInstance;
    public static AnalyticsManager getInstance(Context applicationContext){
        if(sInstance == null) {
            sInstance = new AnalyticsManager(applicationContext);
            if(BuildConfig.DEBUG){
                sInstance.setOptOut(true);
            }
        }



        return sInstance;
    }

    //endregion

    public synchronized Tracker getTracker() {
        LogUtils.LOGV(TAG, "getTracker");
        if (mAnalyticsTracker == null) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(mApplicationContext);
            mAnalyticsTracker = analytics.newTracker(R.xml.analytics);

            // Enable Advertising Features.
            mAnalyticsTracker.enableAdvertisingIdCollection(true);

        }

        return mAnalyticsTracker;
//        return null;
    }

    public void setOptOut(boolean isOptOut){
        LogUtils.LOGV(TAG, "setOptOut = " + isOptOut);

        GoogleAnalytics.getInstance(mApplicationContext).setAppOptOut(isOptOut);

    }

    public void reportScreen(String screenName){
        LogUtils.LOGV(TAG, "reportScreen = " + screenName);

        Tracker t = getTracker();
        t.setScreenName(screenName);
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void reportActivityStart(Activity activity){
        LogUtils.LOGV(TAG, "reportActivityStart " + activity.getClass());

        GoogleAnalytics.getInstance(activity).reportActivityStart(activity);
    }

    public void reportActivityStop(Activity activity){
        LogUtils.LOGV(TAG, "reportActivityStop " + activity.getClass());
        GoogleAnalytics.getInstance(activity).reportActivityStop(activity);
    }

    public void destroy(){
        LogUtils.LOGV(TAG, "destroy");

        GoogleAnalytics.getInstance(mApplicationContext).dispatchLocalHits();
        mAnalyticsTracker = null;
        mApplicationContext = null;
        sInstance = null;

    }

    //region Events

    public enum Category {
        UX,
        Extensions,
        Purchases,
        Batch,
        Actions
    }

    public enum Action {
        //ux
        Button_Click,
        MenuItem_Click,
        Rovers,
        Colors,
        Icons,


        //extensions
        Buy_Request,
        Got_Changed,

        //purchases
        Flow_Result,
        Error_Type,

        //coupons
        Automatic,
        Promo_Submit,
        Promo_Result,
        Url_Submit,
        Url_Result,

        //actions
        Install_Request,
        Selected,

        //tutorial
        Walkthrough

    }

    public void reportEvent(Category category, Action action, String label){
        reportEvent(category,action,label,null);
    }

    public void reportEvent(Category category, Action action, String label, Long value){
        LogUtils.LOGV(TAG, "reportEvent | category: " + category+" | action: " + action + " | label: " + label + " | value: " + value);

        HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder()
                .setCategory(category.toString())
                .setAction(action.toString());
        if(label != null)
            builder.setLabel(label);

        if(value != null)
            builder.setValue(value);

        getTracker().send(builder.build());
    }

    //endregion Events
}
