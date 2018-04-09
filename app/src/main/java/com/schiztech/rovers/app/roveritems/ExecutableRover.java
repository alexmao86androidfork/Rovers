package com.schiztech.rovers.app.roveritems;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.schiztech.rovers.app.utils.BitmapUtils;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.RoversUtils;
import com.schiztech.rovers.app.utils.Utils;

/**
 * Created by schiz_000 on 5/11/2014.
 */
public abstract class ExecutableRover implements IRover {

    private static int EXECUTABLE_CURRENT_VERSION = 1;

    private int mCurrentVersion;
    private static final String TAG = LogUtils.makeLogTag("ExecutableRover");
    protected Intent mIntent;
    protected int mBackgroundColor = IRover.ROVER_DEFAULT_COLOR_VALUE;
    protected RoversUtils.RoverIcon mRoverIcon = IRover.ROVER_DEFAULT_ICON_VALUE;
    protected String mLabel;
    protected long mRoverID;

    /**
     * GSON NEEDS ONLY!
     */
    protected ExecutableRover(){
        //version control - changes & updates here
        if(mCurrentVersion < EXECUTABLE_CURRENT_VERSION){
            mRoverIcon = RoversUtils.RoverIcon.Apps_Android;

            mCurrentVersion = EXECUTABLE_CURRENT_VERSION;
        }


    }

    protected ExecutableRover(Intent intent, Context context) {

        this.mIntent = intent;
        this.mRoverID = RoversManager.generateNewRoverID(context);
    }

    public Intent getLaunchIntent() {
        return mIntent;
    }

    protected static String getIntentPackageName(Intent sintent) {
        if (sintent == null) return null;

        String packageName = sintent.getPackage();

        if (packageName == null && sintent.getComponent() != null) {
            packageName = sintent.getComponent().getPackageName();
        }

        return packageName;
    }

    //region Color
    @Override
    public boolean isColorChangeable() {
        return true;
    }

    @Override
    public void setColor(int color){
        mBackgroundColor = color;
    }

    @Override
    public int getColor(Context context) {
        if(mBackgroundColor == IRover.ROVER_DEFAULT_COLOR_VALUE)
            return getDefaultColor(context);

        return mBackgroundColor;
    }

    @Override
    public boolean isDefaultColor(){
        return mBackgroundColor == IRover.ROVER_DEFAULT_COLOR_VALUE;
    }

    @Override
    public abstract int getDefaultColor(Context context);
    //endregion

    //region Label

    @Override
    public void setLabel(String mLabel) {
        this.mLabel = mLabel;
    }

    @Override
    public String getLabel(Context context){
        if(mLabel == null)
            return getDefaultLabel(context);

        return mLabel;
    }

    abstract String getDefaultLabel(Context context);
    //endregion

    //region Icon
    @Override
    public boolean isIconCachable() {
        return true;
    }

    @Override
    public boolean isIconChangeable() {
        return true;
    }

    @Override
    public boolean isRoverIcon(Context context){

        if(mRoverIcon == IRover.ROVER_DEFAULT_ICON_VALUE){
            try{
                getDefaultIcon(context);
                return false;
            }
            catch (Exception e){LogUtils.LOGE(TAG, e.getMessage());}
        }

        return true;
    }

    @Override
    public void setIcon(RoversUtils.RoverIcon icon) {
        this.mRoverIcon= icon;
    }

    @Override
    public Drawable getIcon(Context context){
        int color = getColor(context);

        try {
            if (mRoverIcon == IRover.ROVER_DEFAULT_ICON_VALUE)
                return getDefaultIcon(context);

            //if RoverIcon

            return BitmapUtils.recolorDrawableByBackground(context, mRoverIcon.getResourceID(), color);
        }
        catch (DefaultIconNotAvailableException iconException){
            //error getting the icon - show error icon
            return BitmapUtils.recolorDrawableByBackground(context, RoversManager.getErrorRoverIcon(context), color);
        }
    }

    abstract Drawable getDefaultIcon(Context context) throws DefaultIconNotAvailableException;

    //endregion

    @Override
    public String getDistinctID() {
        return TAG + mRoverID;
    }


    protected class DefaultIconNotAvailableException extends Exception
    {
        //Parameterless Constructor
        public DefaultIconNotAvailableException() {}

        //Constructor that accepts a message
        public DefaultIconNotAvailableException(String message)
        {
            super(message);
        }
    }

}
