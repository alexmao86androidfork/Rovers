package com.schiztech.rovers.app.application;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.schiztech.rovers.app.BuildConfig;
import com.schiztech.rovers.app.managers.BatchManager;
import com.schiztech.rovers.app.managers.InAppConfig;
import com.schiztech.rovers.app.utils.LogUtils;

import io.fabric.sdk.android.Fabric;


/**
 * Created by schiz_000 on 5/16/2014.
 */

public class RoversApplication extends Application  {
    private static final String TAG = LogUtils.makeLogTag("RoversApplication");
    @Override
    public void onCreate(){
        // The following line triggers the initialization of ACRA
        super.onCreate();
        Fabric.with(this, new Crashlytics());
//        if(!BuildConfig.DEBUG) {
//            Fabric.with(this, new Crashlytics());
//        }
        //init IAB config
        InAppConfig.init();

        //init Batch
        BatchManager.getInstance(getApplicationContext()).setDebugging(BuildConfig.DEBUG);
        BatchManager.getInstance(getApplicationContext()).init();
//        testAcra();
    }


    //region Test null exception
    private void testAcra(){
        String s = getACRAString();
        String st =  s.toUpperCase();

    }

    private String getACRAString(){
        return null;
    }

    //endregion Test null exception





}
