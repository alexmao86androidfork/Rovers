package com.schiztech.rovers.app.activities.roversactions;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.base.ActivityBase;
import com.schiztech.rovers.app.adapters.ActivityListAdapter;
import com.schiztech.rovers.app.utils.ActivityInfo;
import com.schiztech.rovers.app.utils.Utils;

import java.util.List;

public class LastAppActionActivity extends ActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Utils.isAndroidVersionEqualOrAbove(Build.VERSION_CODES.LOLLIPOP)) {
            Toast.makeText(getApplicationContext(), R.string.roveraction_notsupported, Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            new RunLastAppTask().execute();
        }
    }

    private class RunLastAppTask extends AsyncTask<Void, Void, Intent> {
        private final int MAX_RECENT_APPS = 4;//this action,current app, launcher(if exists), last app
        @Override
        protected void onPreExecute (){
        }

        @Override
        protected Intent doInBackground(Void... voids) {
            Intent launchIntent = null;

            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            //loading double what we need - so if we clear not wanted apps we still have the amount.
            List<ActivityManager.RecentTaskInfo> runningTaskInfos = activityManager.getRecentTasks(MAX_RECENT_APPS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
            if (runningTaskInfos.size() > 2) {
                runningTaskInfos.remove(0);//always remove first - it's this activity...
                runningTaskInfos.remove(0);//always remove second - it's the front task...


            for (ActivityManager.RecentTaskInfo ri : runningTaskInfos) {
                    ActivityInfo ai = new ActivityInfo();

                Intent intent = new Intent(ri.baseIntent);

                if(intent.getCategories() != null  && intent.getCategories().contains(Intent.CATEGORY_HOME)){
                    continue;//don't add a launcher intent!
                }

                if(ri.baseIntent.getComponent() == null){
                    continue;//don't try to find the launch intent for non-component task
                }

                PackageManager pm = getPackageManager();
                launchIntent = pm.getLaunchIntentForPackage(ri.baseIntent.getComponent().getPackageName());
                break;//found my launch intent... no need to go on.
            }
            runningTaskInfos.clear();

            return launchIntent;
            }

            else{
                return null;//if array is too short, don't launch anything.
            }
        }

        @Override
        protected void onPostExecute(Intent result) {
            if(result != null) {
                startActivity(result);
            }
            finish();
        }

    }

}
