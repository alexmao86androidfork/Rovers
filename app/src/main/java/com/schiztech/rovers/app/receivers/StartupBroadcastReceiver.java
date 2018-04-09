package com.schiztech.rovers.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.PrefUtils;
import com.schiztech.rovers.app.utils.Utils;
import com.schiztech.rovers.app.windows.FloatingWindowsManager;

public class StartupBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = LogUtils.makeLogTag("StartupBroadcastReceiver");


    @Override
    public void onReceive(Context context, Intent intent) {
        //launch rovers is is activated in settings
        boolean isActivated = PrefUtils.getRoversIsActivatedValue(context);

        LogUtils.LOGD(TAG, "Startup broadcast received");
        if(isActivated) {
            LogUtils.LOGI(TAG, "Launching Rovers service");
            if (!Utils.isServiceRunning(context, FloatingWindowsManager.class)) {
                Utils.syncRoverWindow(context, true);
            }
        }

    }
}
