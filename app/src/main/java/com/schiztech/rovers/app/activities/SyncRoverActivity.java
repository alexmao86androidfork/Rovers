package com.schiztech.rovers.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.schiztech.rovers.app.activities.base.ActivityBase;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.windows.FloatingWindowsManager;
import com.schiztech.rovers.app.windows.helpers.RoverWindowHelper;

import wei.mark.standout.StandOutWindow;

public class SyncRoverActivity extends Activity{
    private static final String TAG = LogUtils.makeLogTag("SyncRoverActivity");

    public static final String TOGGLE_IS_SHOW_KEY = "toggle_is_show_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       boolean isShow = true;

        if(getIntent() != null && getIntent().getExtras() != null){
            isShow =  getIntent().getExtras().getBoolean(TOGGLE_IS_SHOW_KEY, isShow);
        }

        LogUtils.LOGD(TAG, "Toggle Rovers isShow: " + isShow);
        if(isShow){
            LogUtils.LOGV(TAG, "Showing Rovers window");
            StandOutWindow.show(this, FloatingWindowsManager.class, RoverWindowHelper.WINDOW_ID_ROVER);
        }

        else{
            LogUtils.LOGV(TAG, "Stopping Rovers service");
            stopService(new Intent(this, FloatingWindowsManager.class));
        }

        finish();

    }

}
