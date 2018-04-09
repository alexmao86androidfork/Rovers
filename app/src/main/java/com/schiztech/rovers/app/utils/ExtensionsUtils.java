package com.schiztech.rovers.app.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.MainActivity;
import com.schiztech.rovers.app.managers.AnalyticsManager;

/**
 * Created by schiz_000 on 3/20/2015.
 */
public class ExtensionsUtils {
    private static final String TAG = LogUtils.makeLogTag("ExtensionsUtils");

    public enum ExtensionType {
        MoreSettings,
        MoreColors,
        MoreRovers,
        CompletePackage,
        Coffee
    }

    public static String getExtensionName(Context context, ExtensionType type) {
        int nameRes = 0;
        switch (type) {
            case MoreColors:
                nameRes = R.string.extensions_more_colors_title;
                break;
            case MoreSettings:
                nameRes = R.string.extensions_more_settings_title;
                break;
            case MoreRovers:
                nameRes = R.string.extensions_more_rovers_title;
                break;
            case CompletePackage:
                nameRes = R.string.extensions_complete_package_title;
                break;

        }

        if (nameRes != 0) {
            return Utils.getString(context, nameRes);
        }
        return null;
    }

    public static String getExtensionContentWarning(Context context, ExtensionType type) {
        int nameRes = 0;

        switch (type) {
            case MoreColors:
                nameRes = R.string.extensions_locked_more_colors;
                break;
            case MoreSettings:
                nameRes = R.string.extensions_locked_more_settings;
                break;
            case MoreRovers:
                nameRes = R.string.extensions_locked_more_rovers;
                break;
        }

        if (nameRes != 0) {
            return Utils.getString(context, nameRes);
        }

        return null;
    }

    public static void navigateToExtensionsScreen(Context context) {
        Utils.navigateTo(context, R.string.fragment_extensions);
    }

    //region Is Got Methods

    public static boolean isGotExtension(Context context, ExtensionType type) {
        switch (type) {
            case MoreColors:
                return isGotMoreColors(context);
            case MoreSettings:
                return isGotMoreSettings(context);
            case MoreRovers:
                return isGotMoreRovers(context);
            case CompletePackage:
                return isGotCompletePackage(context) || isGotAllSeparated(context);
            case Coffee:
                return false;
        }

        LogUtils.LOGE(TAG, "Tried to is got extension with unknown type: " + type.toString());

        return false;
    }

    public static boolean isGotMoreSettings(Context context) {
        return PrefUtils.getExtensionsMoreSettingsValue(context) || isGotCompletePackage(context);
    }

    public static boolean isGotMoreColors(Context context) {
        return PrefUtils.getExtensionsMoreColorsValue(context) || isGotCompletePackage(context);
    }

    public static boolean isGotMoreRovers(Context context) {
        return PrefUtils.getExtensionsMoreRoversValue(context) || isGotCompletePackage(context);
    }


    public static boolean isGotCompletePackage(Context context) {
        return PrefUtils.getExtensionsCompletePackageValue(context);
    }


    public static boolean isGotAllSeparated(Context context) {
        return PrefUtils.getExtensionsMoreRoversValue(context) &&
                PrefUtils.getExtensionsMoreColorsValue(context) &&
                PrefUtils.getExtensionsMoreSettingsValue(context);
    }

    //endregion Is Got Methods

    //region Set Got Methods

    public static void setGotExtension(Context context, ExtensionType type, boolean value) {
        LogUtils.LOGD(TAG, "Setting extension: " + type.toString() + " to: " + value);

        switch (type) {
            case MoreColors:
                setGotMoreColors(context, value);
                break;
            case MoreSettings:
                setGotMoreSettings(context, value);
                break;
            case MoreRovers:
                setGotMoreRovers(context, value);
                break;
            case CompletePackage:
                setGotCompletePackage(context, value);
                break;
            default:
                LogUtils.LOGE(TAG, "Tried to set got extension with unknown type: " + type.toString());
                return;//don't continue
        }

        AnalyticsManager.getInstance(context).reportEvent(AnalyticsManager.Category.Extensions,
                AnalyticsManager.Action.Got_Changed,
                type.toString(), value ? 1L : 0L);


        if (value == true) {
            setGotExtensionAlert(context, type);
        }

    }

    public static void setGotMoreSettings(Context context, boolean value) {
        PrefUtils.setExtensionsMoreSettingsValue(context, value);
    }

    public static void setGotMoreColors(Context context, boolean value) {
        PrefUtils.setExtensionsMoreColorsValue(context, value);
    }

    public static void setGotMoreRovers(Context context, boolean value) {
        PrefUtils.setExtensionsMoreRoversValue(context, value);
    }

    public static void setGotCompletePackage(Context context, boolean value) {
        PrefUtils.setExtensionsCompletePackageValue(context, value);
    }

    //endregion Set Got Methods


    private static void setGotExtensionAlert(Context context, ExtensionType type) {
        Toast.makeText(context, Utils.getString(context, R.string.extensions_congratulations) +
                "\n" +
                Utils.getString(context, R.string.extensions_successfully_got) + " " + getExtensionName(context, type), Toast.LENGTH_SHORT).show();
    }

}
