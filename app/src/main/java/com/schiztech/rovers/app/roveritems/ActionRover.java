package com.schiztech.rovers.app.roveritems;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.google.gson.annotations.Expose;
import com.schiztech.rovers.api.RoversActionBuilder;
import com.schiztech.rovers.app.utils.BitmapUtils;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.Utils;

/**
 * Created by schiz_000 on 10/5/2014.
 */
public class ActionRover extends ExecutableRover {
    private static final String TAG = LogUtils.makeLogTag("ActionRover");

    private Intent.ShortcutIconResource mIconResource = null;
    private long mBitmapID = -1;
    private String mDefaultLabel = null;
    private int mDefaultColor = 0xff64DD17;//md light green A700
    private boolean mIsColorChangeable = true;
    private boolean mIsIconChangeable = true;


    private int mApiVersion = 1;//for future back-support

    public ActionRover(Intent unflattenedIntent, Context context){
        super(flattenActionIntent(unflattenedIntent),context);

        Bitmap icon = flattenActionIcon(unflattenedIntent);
        if(icon != null)//default use icon
            mBitmapID = BitmapUtils.saveBitmapToStorage(icon, context);
        else{// if no bitmap icon set - use icon resources
            mBitmapID = -1;
            mIconResource = flattenActionIconResource(unflattenedIntent);
        }

        mDefaultLabel = flattenActionLabel(unflattenedIntent);
        mDefaultColor = flattenActionColor(unflattenedIntent);
        mIsColorChangeable = flattenActionIsColorChangeable(unflattenedIntent);
        mIsIconChangeable = flattenActionIsIconChangeable(unflattenedIntent);

        mApiVersion = flattenActionApiVersion(unflattenedIntent);
        LogUtils.LOGD(TAG, "created action for " + getIntentPackageName(mIntent));
    }

    //region Label methods
    @Override
    String getDefaultLabel(Context context) {
        return mDefaultLabel;
    }
    //endregion Label methods

    //region icon  methods
    @Override
    public boolean isIconChangeable() {
        return mIsIconChangeable;
    }

    @Override
    public boolean isRoverIcon(Context context) {
        return mBitmapID == -1;//if bitmap not exist - rover icon has to exist.
    }


    @Override
    Drawable getDefaultIcon(Context context) throws DefaultIconNotAvailableException{
        if(mBitmapID != -1){
            Bitmap icon = BitmapUtils.readBitmapFromStorage(context, mBitmapID);

            if(icon!= null)
                return new BitmapDrawable(context.getResources(), icon);

            throw new DefaultIconNotAvailableException("Couldn't get icon for action " + getLabel(context));
        }

        else{
            try {
                Resources resources = context.getPackageManager().getResourcesForApplication(mIconResource.packageName);
                if (resources != null) {
                    int id = resources.getIdentifier(mIconResource.resourceName, null, null);

                    Drawable drawableIcon = resources.getDrawable(id);
                    int color = getColor(context);
                    return BitmapUtils.recolorDrawableByBackground(context, drawableIcon, color);
                }

            } catch (PackageManager.NameNotFoundException e) {
                LogUtils.LOGW(TAG, "Couldn't get icon for package \"" + getIntentPackageName(getLaunchIntent()) + "\"");
            }
        }

        throw new DefaultIconNotAvailableException("Couldn't get icon for action " + getLabel(context));
    }
    //endregion icon methods

    //region color methods
    @Override
    public boolean isColorChangeable() {
        return mIsColorChangeable;
    }

    @Override
    public int getDefaultColor(Context context) {
        return mDefaultColor;
    }
    //endregion color methods

    //region flatten intent methods

    public static Intent flattenActionIntent(Intent unflattenedIntent){
        try {
            return Intent.parseUri(flattenActionUri(unflattenedIntent), 0);
        } catch (Exception e) {
            LogUtils.LOGE(TAG, "Couldn't get intent  for package \"" + getIntentPackageName(unflattenedIntent) + "\"" + e.getMessage());
        }

        return null;
    }
    private static String flattenActionUri(Intent unflattenedIntent) {
        try {
            Intent tmpIntent = (Intent) unflattenedIntent.getExtras().get(RoversActionBuilder.KEY_INTENT);

            return tmpIntent.toUri(0);
        } catch (Exception e) {
            LogUtils.LOGE(TAG, "Couldn't get uri for package \"" + unflattenedIntent.getPackage() + "\"" + e.getMessage());
        }

        return null;
    }

    public static Bitmap flattenActionIcon(Intent unflattenedIntent){
        try{
            return  (Bitmap) unflattenedIntent.getExtras().get(RoversActionBuilder.KEY_ICON);
        }
        catch (Exception e) {
            LogUtils.LOGE(TAG, "Couldn't get icon for package \"" + unflattenedIntent.getPackage() + "\"" + e.getMessage());
        }

        return null;
    }
    public static Intent.ShortcutIconResource flattenActionIconResource(Intent unflattenedIntent){
        try{
            return  (Intent.ShortcutIconResource) unflattenedIntent.getExtras().get(RoversActionBuilder.KEY_ICON_RESOURCE);
        }
        catch (Exception e) {
            LogUtils.LOGE(TAG, "Couldn't get dark icon for package \"" + unflattenedIntent.getPackage() + "\"" + e.getMessage());
        }

        return null;
    }
    public static int flattenActionColor(Intent unflattenedIntent){
        try{
            return unflattenedIntent.getExtras().getInt(RoversActionBuilder.KEY_COLOR);
        }
        catch (Exception e) {
            LogUtils.LOGE(TAG, "Couldn't get color for package \"" + unflattenedIntent.getPackage() + "\"" + e.getMessage());
        }

        return Color.WHITE;
    }
    public static boolean flattenActionIsColorChangeable(Intent unflattenedIntent){
        try{
            return unflattenedIntent.getExtras().getBoolean(RoversActionBuilder.KEY_IS_COLOR_CHANGEABLE);
        }
        catch (Exception e) {
            LogUtils.LOGE(TAG, "Couldn't get is color changeable for package \"" + unflattenedIntent.getPackage() + "\"" + e.getMessage());
        }

        return true;
    }
    public static boolean flattenActionIsIconChangeable(Intent unflattenedIntent){
        try{
            return unflattenedIntent.getExtras().getBoolean(RoversActionBuilder.KEY_IS_ICON_CHANGEABLE);
        }
        catch (Exception e) {
            LogUtils.LOGE(TAG, "Couldn't get is icon changeable for package \"" + unflattenedIntent.getPackage() + "\"" + e.getMessage());
        }

        return true;
    }
    public static int flattenActionApiVersion(Intent unflattenedIntent){
        try{
            return unflattenedIntent.getExtras().getInt(RoversActionBuilder.KEY_API_VERSION);
        }
        catch (Exception e) {
            LogUtils.LOGE(TAG, "Couldn't get api version for package \"" + unflattenedIntent.getPackage() + "\"" + e.getMessage());
        }

        return 1;
    }
    public static String flattenActionLabel(Intent unflattenedIntent){
        try{
            return unflattenedIntent.getExtras().getString(RoversActionBuilder.KEY_LABEL);
        }
        catch (Exception e) {
            LogUtils.LOGE(TAG, "Couldn't get label for package \"" + unflattenedIntent.getPackage() + "\"" + e.getMessage());
        }

        return null;
    }
    //endregion flatten intent methods

    @Override
    public void onDelete() {

    }
}
