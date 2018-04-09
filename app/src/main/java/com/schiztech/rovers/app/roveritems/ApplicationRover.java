package com.schiztech.rovers.app.roveritems;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.PrefUtils;

/**
 * Created by schiz_000 on 5/11/2014.
 */
public class ApplicationRover extends ExecutableRover {
    private static final String TAG = LogUtils.makeLogTag("ApplicationRover");

    /**
     * GSON NEEDS ONLY!
     */
    public ApplicationRover(){}

    public ApplicationRover(Intent launchIntent, Context context) {
        super(launchIntent, context);
    }

    //region IRover Methods

    @Override
    public Drawable getDefaultIcon(Context context) throws DefaultIconNotAvailableException{

        String packageName = getIntentPackageName(mIntent);
        try {
            return context.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.LOGE(TAG, "Couldn't get icon for package \"" + packageName + "\"");
            throw new DefaultIconNotAvailableException("Couldn't get icon for package \"" + packageName + "\"");
        }
    }

    @Override
    public String getDefaultLabel(Context context) {
        String packageName = getIntentPackageName(mIntent);
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            return (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (final PackageManager.NameNotFoundException e) {
            LogUtils.LOGE(TAG, "Couldn't get app name for package \"" + packageName + "\"");
            return "";
        }

    }

    @Override
    public int getDefaultColor(Context context) {
        return PrefUtils.getDefaultApplicationColorValue(context);
    }

    @Override
    public void onDelete() {

    }

    //endregion


}
