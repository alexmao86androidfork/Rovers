package com.schiztech.rovers.app.activities.roversactions;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.base.ActivityBase;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.Utils;

import java.util.List;

public class ClearRamActionActivity extends ActivityBase {
    private static final String TAG = LogUtils.makeLogTag("ClearRamActionActivity");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ClearRamTask().execute();
    }

    private class ClearRamTask extends AsyncTask<Void, Void, Void> {
        long mInitialRam;
        ActivityManager mActivityManager;
        @Override
        protected void onPreExecute (){
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

            //get initial available RAM in Mb
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            mActivityManager.getMemoryInfo(mi);
            mInitialRam = mi.availMem / 1048576L; //1024 x 1024 (kilo * mega)

            List<ActivityManager.RunningAppProcessInfo> runningInfos =  mActivityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo ri : runningInfos) {
                if(ri.pkgList != null) {
                    mActivityManager.killBackgroundProcesses(ri.pkgList[0]);
                    LogUtils.LOGI(TAG, "Killed process: " + ri.pkgList[0]);
                }
            }
            runningInfos.clear();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Context context = getApplicationContext();
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            mActivityManager.getMemoryInfo(mi);
            long availableRam = mi.availMem / 1048576L; //1024 x 1024 (kilo * mega)
            String clearedString = Utils.getString(context, R.string.roveraction_clearram_cleared) +
                                    " "+(availableRam - mInitialRam)
                                    + Utils.getString(context, R.string.roveraction_clearram_mbram);

            Toast.makeText(context, clearedString, Toast.LENGTH_SHORT).show();
            mActivityManager = null;
            context = null;
            finish();
        }

    }
}
