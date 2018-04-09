package com.schiztech.rovers.app.roveritems;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.schiztech.rovers.app.roveritems.actions.ExtendedRoverActionBuilder;
import com.schiztech.rovers.app.utils.BitmapUtils;
import com.schiztech.rovers.app.utils.LogUtils;

/**
 * Created by schiz_000 on 11/9/2014.
 */
public class InteractiveActionRover extends ActionRover{
    private static final String TAG = LogUtils.makeLogTag("InteractiveActionRover");

    private String mContentUri;
    private boolean mIsColorInteractive = true;
    private boolean mIsIconInteractive = true;

    private int mInteractiveColor = IRover.ROVER_DEFAULT_COLOR_VALUE;
    private byte[] mInteractiveIconStream = null;

    public InteractiveActionRover(Intent unflattenedIntent, Context context) {
        super(unflattenedIntent, context);

        mContentUri = flattenActionContentUri(unflattenedIntent);
        mIsColorInteractive = flattenActionIsColorInteractive(unflattenedIntent);
        mIsIconInteractive = flattenActionIsIconInteractive(unflattenedIntent);
        LogUtils.LOGD(TAG, "created interactive action for " + getIntentPackageName(mIntent));
    }


    //region Icon

    @Override
    public boolean isIconCachable() {//cachable only if user changed the icon to rover icon
        if(mIsIconInteractive)//if the icon is interactive we don't want it to nbe cached.
            return false;

        return super.isIconCachable();
    }

    @Override
    public boolean isRoverIcon(Context context){
        if(mIsIconInteractive)
            return true;//we want the interactive icon to be masked as rover icon

        return super.isRoverIcon(context);
    }

    @Override
    Drawable getDefaultIcon(Context context) throws DefaultIconNotAvailableException{
        updateInteractiveValues(context);//update interactive icon and background color values
        if(mIsIconInteractive && mInteractiveIconStream != null){
            try {
                Bitmap icon = BitmapFactory.decodeByteArray(mInteractiveIconStream, 0, mInteractiveIconStream.length);
                mInteractiveIconStream = null;//empty byte array after use.
                Drawable drawableIcon = new BitmapDrawable(context.getResources(), icon);
                int color = getColor(context);
                return BitmapUtils.recolorDrawableByBackground(context, drawableIcon, color);
            }
            catch (Exception e){
                LogUtils.LOGE(TAG, "Couldn't get interactive icon from byte stream for \"" + getLabel(context) + "\"" + e.getMessage());
            }
        }

        return super.getDefaultIcon(context);
    }

    //endregion Icon

    //region Color

    @Override
    public int getDefaultColor(Context context) {
        if (mIsColorInteractive && mInteractiveColor != IRover.ROVER_DEFAULT_COLOR_VALUE) {
            int result = mInteractiveColor;

            return result;
        }
        return super.getDefaultColor(context);
    }



    //endregion Color

    //region flatten intent methods
    private static String flattenActionContentUri(Intent unflattenedIntent) {
        try {
            return (String) unflattenedIntent.getExtras().get(ExtendedRoverActionBuilder.KEY_CONTENT_URI);

        } catch (Exception e) {
            LogUtils.LOGE(TAG, "Couldn't get content uri for package \"" + unflattenedIntent.getPackage() + "\"" + e.getMessage());
        }

        return null;
    }

    private static boolean flattenActionIsColorInteractive(Intent unflattenedIntent) {
        try {
            return (boolean) unflattenedIntent.getExtras().getBoolean(ExtendedRoverActionBuilder.KEY_IS_COLOR_INTERACTIVE, true);

        } catch (Exception e) {
            LogUtils.LOGE(TAG, "Couldn't get is color interactive for package \"" + unflattenedIntent.getPackage() + "\"" + e.getMessage());
        }

        return true;
    }

    private static boolean flattenActionIsIconInteractive(Intent unflattenedIntent) {
        try {
            return (boolean) unflattenedIntent.getExtras().getBoolean(ExtendedRoverActionBuilder.KEY_IS_ICON_INTERACTIVE, true);

        } catch (Exception e) {
            LogUtils.LOGE(TAG, "Couldn't get is icon interactive for package \"" + unflattenedIntent.getPackage() + "\"" + e.getMessage());
        }

        return true;
    }

    //endregion flatten intent methods

    private void updateInteractiveValues(Context context){

        if(mIsIconInteractive == false && mIsColorInteractive == false){
            return;//non interactive field is set as interactive. there's no reason to get the interactive values....
        }

        //reset values before reload
        mInteractiveColor = IRover.ROVER_DEFAULT_COLOR_VALUE;
        mInteractiveIconStream = null;

        //validate provider return type
        String returnType =context.getContentResolver().getType(Uri.parse(mContentUri));
        if(returnType == null ||  !returnType.equals(ExtendedRoverActionBuilder.CONTENT_TYPE)) {
            LogUtils.LOGE(TAG, "Couldn't get interactive values for \"" + getLabel(context) + "\"" +
                    " content provider doesn't return the proper type " +
                    "(returns " + returnType + " instead of " + ExtendedRoverActionBuilder.CONTENT_TYPE + ").");
            return;
        }

        //get provider's results - first result in cursor
        Cursor cursor = context.getContentResolver().query(Uri.parse(mContentUri), null, null, null, null);

        //if no result or an error occurred - don't continue.
        if (cursor == null || cursor.getCount() <= 0) {
            LogUtils.LOGE(TAG, "Couldn't get interactive values for \"" + getLabel(context) + "\"" + " content provider doesn't return any value.");
            return;
        }

        cursor.moveToFirst();
        if(mIsIconInteractive) {
            try {
                mInteractiveIconStream = cursor.getBlob(ExtendedRoverActionBuilder.INTERACTIVE_ACTION_ICON_INDEX);
            } catch (Exception e) {
                //the provider didn't supply proper interactive icon - use default.
                mInteractiveIconStream = null;
            }
        }

        if(mIsColorInteractive) {
            try {
                mInteractiveColor = cursor.getInt(ExtendedRoverActionBuilder.INTERACTIVE_ACTION_BACKGROUND_INDEX);
            } catch (Exception e) {
                //the provider didn't supply proper interactive color - use default.
                mInteractiveColor = IRover.ROVER_DEFAULT_COLOR_VALUE;
            }
        }

        cursor.close();
    }

}
