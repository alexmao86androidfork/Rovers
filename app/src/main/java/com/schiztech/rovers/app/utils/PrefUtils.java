package com.schiztech.rovers.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.fragments.selectors.IntentSelectorFactory;

/**
 * Created by schiz_000 on 2/26/2015.
 */
public class PrefUtils {


    public static SharedPreferences getMainSharedPreferences(Context context) {
        return context.getSharedPreferences(Utils.getString(context, R.string.prefs_main), Context.MODE_MULTI_PROCESS);
    }

    public static SharedPreferences getRoverlyticsSharedPreferences(Context context) {
        return context.getSharedPreferences(Utils.getString(context, R.string.prefs_roverlytics), Context.MODE_PRIVATE);
    }

    public static SharedPreferences getWindowsSharedPreferences(Context context) {
        return context.getSharedPreferences(Utils.getString(context, R.string.prefs_windows), Context.MODE_MULTI_PROCESS);
    }

    //region Intent Actions

    public static final String ACTION_TRIGGER_BACKGROUND_COLOR_CHANGED = "com.schiztech.rovers.app.ACTION_TRIGGER_BACKGROUND_COLOR_CHANGED";
    public static final String ACTION_TRIGGER_ICON_COLOR_CHANGED = "com.schiztech.rovers.app.ACTION_TRIGGER_ICON_COLOR_CHANGED";
    public static final String ACTION_TRIGGER_REST_ALPHA_CHANGED = "com.schiztech.rovers.app.ACTION_TRIGGER_REST_ALPHA_CHANGED";
    public static final String ACTION_TRIGGER_REST_OFFSET_CHANGED = "com.schiztech.rovers.app.ACTION_TRIGGER_REST_OFFSET_CHANGED";
    public static final String ACTION_ITEMS_DEFAULT_COLOR = "com.schiztech.rovers.app.ACTION_ITEMS_DEFAULT_COLOR";

    public static String[] getIntentActions(){
        return new String[]{
                ACTION_TRIGGER_BACKGROUND_COLOR_CHANGED,
                ACTION_TRIGGER_ICON_COLOR_CHANGED,
                ACTION_TRIGGER_REST_ALPHA_CHANGED,
                ACTION_TRIGGER_REST_OFFSET_CHANGED,
                ACTION_ITEMS_DEFAULT_COLOR
        };
    }

    //endregion Intent Actions

    //region App Preferences

    //region Rovers Is Activated

    private static final boolean ROVERS_IS_ACTIVATED_DEFAULT = true;

    public static boolean getDefaultRoversIsActivated() {
        return ROVERS_IS_ACTIVATED_DEFAULT;
    }

    public static boolean getRoversIsActivatedValue(Context context) {
        return getMainSharedPreferences(context).getBoolean(PrefKeys.ROVERS_IS_ACTIVATED, getDefaultRoversIsActivated());
    }

    public static void setRoversIsActivatedValue(Context context, boolean value) {
        getMainSharedPreferences(context).edit().putBoolean(PrefKeys.ROVERS_IS_ACTIVATED, value).apply();
    }


    //endregion Rovers Is Activated

    //region Trigger Rest Alpha
    private static final int TRIGGER_REST_ALPHA_DEFAULT = R.string.trigger_rest_alpha_default;

    public static float getDefaultTriggerRestAlpha(Context context) {
        return Float.parseFloat(Utils.getString(context, TRIGGER_REST_ALPHA_DEFAULT));
    }

    public static float getTriggerRestAlphaValue(Context context) {
        return Float.parseFloat(getMainSharedPreferences(context).getString(PrefKeys.TRIGGER_REST_ALPHA, getDefaultTriggerRestAlpha(context) + ""));
    }
    //endregion Rest Alpha

    //region Trigger Rest Offset

    private static final int TRIGGER_REST_OFFSET_DEFAULT = R.string.trigger_rest_offset_default;

    public static float getDefaultTriggerRestOffset(Context context) {
        return Float.parseFloat(Utils.getString(context, TRIGGER_REST_OFFSET_DEFAULT));
    }

    public static float getTriggerRestOffsetValue(Context context) {
        return Float.parseFloat(getMainSharedPreferences(context).getString(PrefKeys.TRIGGER_REST_OFFSET, getDefaultTriggerRestOffset(context) + ""));
    }
    //endregion Rest Offset

    //region Trigger Background Color

    private static final int TRIGGER_BACKGROUND_COLOR_DEFAULT = R.color.trigger_background_color_default;

    public static int getDefaultTriggerBackgroundColor(Context context) {
        return Utils.getColor(context, TRIGGER_BACKGROUND_COLOR_DEFAULT);
    }

    public static int getTriggerBackgroundValue(Context context) {
        return getMainSharedPreferences(context).getInt(PrefKeys.TRIGGER_BACKGROUND_COLOR, getDefaultTriggerBackgroundColor(context));
    }


    //endregion Trigger Background Color

    //region Trigger Icon Color

    private static final int TRIGGER_ICON_COLOR_DEFAULT = R.color.trigger_icon_color_default;

    public static int getDefaultTriggerIconColor(Context context) {
        return Utils.getColor(context, TRIGGER_ICON_COLOR_DEFAULT);
    }

    public static int getTriggerIconValue(Context context) {
        return getMainSharedPreferences(context).getInt(PrefKeys.TRIGGER_ICON_COLOR, getDefaultTriggerIconColor(context));
    }


    //endregion Trigger Icon Color

    //region Trigger Independent Size

    private static final int TRIGGER_INDEPENDENT_SIZE_DEFAULT = R.bool.trigger_independent_size_default;

    public static boolean getDefaultTriggerIndependentSize(Context context) {
        return Utils.getBoolean(context, TRIGGER_INDEPENDENT_SIZE_DEFAULT);
    }

    public static boolean getTriggerIndependentSizeValue(Context context) {
        return getMainSharedPreferences(context).getBoolean(PrefKeys.TRIGGER_INDEPENDENT_SIZE, getDefaultTriggerIndependentSize(context));
    }


    //endregion Trigger Independent Size

    //region Trigger Item Size

    private static final int TRIGGER_ITEM_SIZE_DEFAULT = R.integer.trigger_item_size_default;

    public static int getDefaultTriggerItemSize(Context context) {
        return Utils.getInteger(context, TRIGGER_ITEM_SIZE_DEFAULT);
    }

    public static int getTriggerItemSizeValue(Context context) {
        String value = getMainSharedPreferences(context).getString(PrefKeys.TRIGGER_ITEM_SIZE, "" + getDefaultTriggerItemSize(context));
        return Integer.parseInt(value);
    }


    //endregion Trigger Item Size

    //region Host Sticky Corner
    public static final int HOST_STICKY_CORNER_CLOSESTCORNER = 0;
    public static final int HOST_STICKY_CORNER_TOPLEFT = 1;
    public static final int HOST_STICKY_CORNER_TOPRIGHT = 2;
    public static final int HOST_STICKY_CORNER_BOTTOMLEFT = 3;
    public static final int HOST_STICKY_CORNER_BOTTOMRIGHT = 4;


    private static final int HOST_STICKY_CORNER_DEFAULT = R.integer.host_sticky_corner_default;

    public static int getDefaultHostStickyCorner(Context context) {
        return Utils.getInteger(context, HOST_STICKY_CORNER_DEFAULT);
    }

    public static int getHostStickyCornerValue(Context context) {
        return Integer.parseInt(getMainSharedPreferences(context).getString(PrefKeys.HOST_STICKY_CORNER, getDefaultHostStickyCorner(context) + ""));
    }
    //endregion Sticky Corner

    //region Host Orientation
    public static final int HOST_ORIENTATION_AUTO = 0;
    public static final int HOST_ORIENTATION_VERTICAL = 1;
    public static final int HOST_ORIENTATION_HORIZONTAL = 2;

    private static final int HOST_ORIENTATION_DEFAULT = R.integer.host_orientation_default;

    public static int getDefaultHostOrientation(Context context) {
        return Utils.getInteger(context, HOST_ORIENTATION_DEFAULT);
    }

    public static int getHostOrientationValue(Context context) {
        return Integer.parseInt(getMainSharedPreferences(context).getString(PrefKeys.HOST_ORIENTATION, getDefaultHostOrientation(context) + ""));
    }

    public static boolean isHostOrientationLandscape(Context context) {
        int orientationPrefs = getHostOrientationValue(context);

        if (orientationPrefs == PrefUtils.HOST_ORIENTATION_HORIZONTAL)
            return true;

        if (orientationPrefs == PrefUtils.HOST_ORIENTATION_AUTO) {
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                return true;
        }

        return false;

    }
    //endregion Host Orientation

    //region Host Add On Edit Only
    private static final int HOST_ADD_ON_EDIT_ONLY_DEFAULT = R.bool.host_add_on_edit_only_default;

    public static boolean getDefaultAddOnEditOnly(Context context) {
        return Utils.getBoolean(context, HOST_ADD_ON_EDIT_ONLY_DEFAULT);
    }

    public static boolean getAddOnEditOnlyValue(Context context) {
        return getMainSharedPreferences(context).getBoolean(PrefKeys.HOST_ADD_ON_EDIT_ONLY, getDefaultAddOnEditOnly(context));
    }
    //endregion Host Add On Edit Only

    //region Items Default Application Color

    private static final int ITEMS_DEFAULT_APPLICATION_COLOR_DEFAULT = R.color.items_default_application_color_default;

    public static int getDefaultApplicationColorDefault(Context context) {
        return Utils.getColor(context, ITEMS_DEFAULT_APPLICATION_COLOR_DEFAULT);
    }

    public static int getDefaultApplicationColorValue(Context context) {
        return getMainSharedPreferences(context).getInt(PrefKeys.ITEMS_DEFAULT_APPLICATION_COLOR, getDefaultApplicationColorDefault(context));
    }


    //endregion Items Default Application Color

    //region Items Default Shortcut Color

    private static final int ITEMS_DEFAULT_SHORTCUT_COLOR_DEFAULT = R.color.items_default_shortcut_color_default;

    public static int getDefaultShortcutColorDefault(Context context) {
        return Utils.getColor(context, ITEMS_DEFAULT_SHORTCUT_COLOR_DEFAULT);
    }

    public static int getDefaultShortcutColorValue(Context context) {
        return getMainSharedPreferences(context).getInt(PrefKeys.ITEMS_DEFAULT_SHORTCUT_COLOR, getDefaultShortcutColorDefault(context));
    }


    //endregion Items Default Shortcut Color

    //region Items Default Folder Color
    private static final int ITEMS_DEFAULT_FOLDER_COLOR_DEFAULT = R.color.items_default_folder_color_default;

    public static int getDefaultFolderColorDefault(Context context) {
        return Utils.getColor(context, ITEMS_DEFAULT_FOLDER_COLOR_DEFAULT);
    }

    public static int getDefaultFolderColorValue(Context context) {
        return getMainSharedPreferences(context).getInt(PrefKeys.ITEMS_DEFAULT_FOLDER_COLOR, getDefaultFolderColorDefault(context));
    }


    //endregion Items Default Folder Color

    //region Items Item Size

    private static final int ITEMS_ITEM_SIZE_DEFAULT = R.integer.items_item_size_default;

    public static int getDefaultItemsItemSize(Context context) {
        return Utils.getInteger(context, ITEMS_ITEM_SIZE_DEFAULT);
    }

    public static int getItemsItemSizeValue(Context context) {
        String value = getMainSharedPreferences(context).getString(PrefKeys.ITEMS_ITEM_SIZE, ""+getDefaultItemsItemSize(context));
        return Integer.parseInt(value);
    }


    //endregion Items Item Size

    //region Misc Mute Click Sound
    private static final int MISC_MUTE_CLICK_SOUND_DEFAULT = R.bool.misc_mute_click_sound_default;

    public static boolean getDefaultMiscMuteClickSound(Context context) {
        return Utils.getBoolean(context, MISC_MUTE_CLICK_SOUND_DEFAULT);
    }

    public static boolean getMiscMuteClickSoundValue(Context context) {
        return getMainSharedPreferences(context).getBoolean(PrefKeys.MISC_MUTE_CLICK_SOUND, getDefaultMiscMuteClickSound(context));
    }
    //endregion Misc Mute Click Sound

    //region Misc Send Anonymous Data
    private static final int MISC_SEND_ANONYMOUS_DATA_DEFAULT = R.bool.misc_send_anonymous_data_default;

    public static boolean getDefaultMiscSendAnonymousData(Context context) {
        return Utils.getBoolean(context, MISC_SEND_ANONYMOUS_DATA_DEFAULT);
    }

    public static boolean getMiscSendAnonymousDataValue(Context context) {
        return getMainSharedPreferences(context).getBoolean(PrefKeys.MISC_SEND_ANONYMOUS_DATA, getDefaultMiscSendAnonymousData(context));
    }
    //endregion Misc Send Anonymous Data

    //region Items Count
    private static final int ROVERLYTICS_ITEMS_COUNT_DEFAULT = 0;

    public static int getDefaultRoverlyticsItemsCount() {
        return ROVERLYTICS_ITEMS_COUNT_DEFAULT;
    }

    public static int getRoverlyticsItemsCountValue(Context context) {
        return getRoverlyticsSharedPreferences(context).getInt(PrefKeys.ROVERLYTICS_ITEMS_COUNT, getDefaultRoverlyticsItemsCount());
    }

    public static void setRoverlyticsItemsCountValue(Context context, int value) {
        getRoverlyticsSharedPreferences(context).edit().putInt(PrefKeys.ROVERLYTICS_ITEMS_COUNT, value).apply();
    }


    //endregion Items Count


    //endregion App Preferences

    //region Roverlytics Preferences

    //region Roverlytics Launches

    private static final int ROVERLYTICS_LAUNCHES_DEFAULT = 0;

    public static int getDefaultRoverlyticsLaunches() {
        return ROVERLYTICS_LAUNCHES_DEFAULT;
    }

    public static int getRoverlyticsLaunchesValue(Context context) {
        return getRoverlyticsSharedPreferences(context).getInt(PrefKeys.ROVERLYTICS_LAUNCHES, getDefaultRoverlyticsLaunches());
    }

    public static void setRoverlyticsLaunchesValue(Context context, int value) {
        getRoverlyticsSharedPreferences(context).edit().putInt(PrefKeys.ROVERLYTICS_LAUNCHES, value).apply();
    }


    //endregion Roverlytics Launches

    //region Roverlytics Total Time

    private static final float ROVERLYTICS_TOTAL_TIME_DEFAULT = 0f;

    public static float getDefaultRoverlyticsTotalTime() {
        return ROVERLYTICS_TOTAL_TIME_DEFAULT;
    }

    public static float getRoverlyticsTotalTimeValue(Context context) {
        return getRoverlyticsSharedPreferences(context).getFloat(PrefKeys.ROVERLYTICS_TOTAL_TIME, getDefaultRoverlyticsTotalTime());
    }

    public static void setRoverlyticsTotalTimeValue(Context context, float value) {
        getRoverlyticsSharedPreferences(context).edit().putFloat(PrefKeys.ROVERLYTICS_TOTAL_TIME, value).apply();
    }

    //endregion Roverlytics Total Time

    //region Roverlytics Distance

    private static final float ROVERLYTICS_DISTANCE_DEFAULT = 0f;

    public static float getDefaultRoverlyticsDistance() {
        return ROVERLYTICS_DISTANCE_DEFAULT;
    }

    public static float getRoverlyticsDistanceValue(Context context) {
        return getRoverlyticsSharedPreferences(context).getFloat(PrefKeys.ROVERLYTICS_DISTANCE, getDefaultRoverlyticsDistance());
    }

    public static void setRoverlyticsDistanceValue(Context context, float value) {
        getRoverlyticsSharedPreferences(context).edit().putFloat(PrefKeys.ROVERLYTICS_DISTANCE, value).apply();
    }

    //endregion Roverlytics Distance


    //endregion Roverlytics Preferences

    //region Windows Preferences

    //region RoverWindow X Location

    private static final float ROVER_WINDOW_X_DEFAULT = 0.25f;

    public static float getDefaultRoverWindowX() {
        return ROVER_WINDOW_X_DEFAULT;
    }

    public static float getRoverWindowXValue(Context context) {
        return getWindowsSharedPreferences(context).getFloat(PrefKeys.ROVER_WINDOW_X, getDefaultRoverWindowX());
    }

    public static void setRoverWindowXValue(Context context, float value) {
        getWindowsSharedPreferences(context).edit().putFloat(PrefKeys.ROVER_WINDOW_X, value).apply();
    }

    //endregion RoverWindow X Location

    //region Hidden Alert Is Shown

    private static final boolean ROVER_HIDDEN_ALERT_IS_SHOWN_DEFAULT = false;

    public static boolean getDefaultHiddenAlertIsShown() {
        return ROVER_HIDDEN_ALERT_IS_SHOWN_DEFAULT;
    }

    public static boolean getHiddenAlertIsShownValue(Context context) {
        return getWindowsSharedPreferences(context).getBoolean(PrefKeys.ROVER_HIDDEN_ALERT_IS_SHOWN, getDefaultHiddenAlertIsShown());
    }

    public static void setHiddenAlertIsShownValue(Context context, boolean value) {
        getWindowsSharedPreferences(context).edit().putBoolean(PrefKeys.ROVER_HIDDEN_ALERT_IS_SHOWN, value).apply();
    }

    //endregion Hidden Alert Is Shown

    //region RoverWindow Y Location

    private static final float ROVER_WINDOW_Y_DEFAULT = 0.51f;

    public static float getDefaultRoverWindowY() {
        return ROVER_WINDOW_Y_DEFAULT;
    }

    public static float getRoverWindowYValue(Context context) {
        return getWindowsSharedPreferences(context).getFloat(PrefKeys.ROVER_WINDOW_Y, getDefaultRoverWindowY());
    }

    public static void setRoverWindowYValue(Context context, float value) {
        getWindowsSharedPreferences(context).edit().putFloat(PrefKeys.ROVER_WINDOW_Y, value).apply();
    }

    //endregion RoverWindow Y Location

    //endregion Windows Preferences

    //region Extensions Preferences

    //region More Settings

    private static final boolean EXTENSIONS_MORE_SETTINGS_DEFAULT = false;

    public static boolean getDefaultExtensionsMoreSettings() {
        return EXTENSIONS_MORE_SETTINGS_DEFAULT;
    }

    public static boolean getExtensionsMoreSettingsValue(Context context) {
        return getMainSharedPreferences(context).getBoolean(PrefKeys.EXTENSIONS_MORE_SETTINGS, getDefaultExtensionsMoreSettings());
    }

    public static void setExtensionsMoreSettingsValue(Context context, boolean value) {
        getMainSharedPreferences(context).edit().putBoolean(PrefKeys.EXTENSIONS_MORE_SETTINGS, value).apply();
    }

    //endregion More Settings

    //region More Colors

    private static final boolean EXTENSIONS_MORE_COLORS_DEFAULT = false;

    public static boolean getDefaultExtensionsMoreColors() {
        return EXTENSIONS_MORE_COLORS_DEFAULT;
    }

    public static boolean getExtensionsMoreColorsValue(Context context) {
        return getMainSharedPreferences(context).getBoolean(PrefKeys.EXTENSIONS_MORE_COLORS, getDefaultExtensionsMoreColors());
    }

    public static void setExtensionsMoreColorsValue(Context context, boolean value) {
        getMainSharedPreferences(context).edit().putBoolean(PrefKeys.EXTENSIONS_MORE_COLORS, value).apply();
    }

    //endregion More Colors

    //region More Rovers

    private static final boolean EXTENSIONS_MORE_ROVERS_DEFAULT = false;

    public static boolean getDefaultExtensionsMoreRovers() {
        return EXTENSIONS_MORE_ROVERS_DEFAULT;
    }

    public static boolean getExtensionsMoreRoversValue(Context context) {
        return getMainSharedPreferences(context).getBoolean(PrefKeys.EXTENSIONS_MORE_ROVERS, getDefaultExtensionsMoreRovers());
    }

    public static void setExtensionsMoreRoversValue(Context context, boolean value) {
        getMainSharedPreferences(context).edit().putBoolean(PrefKeys.EXTENSIONS_MORE_ROVERS, value).apply();
    }

    //endregion More Rovers

    //region Complete Package

    private static final boolean EXTENSIONS_COMPLETE_PACKAGE_DEFAULT = false;

    public static boolean getDefaultExtensionsCompletePackage() {
        return EXTENSIONS_COMPLETE_PACKAGE_DEFAULT;
    }

    public static boolean getExtensionsCompletePackageValue(Context context) {
        return getMainSharedPreferences(context).getBoolean(PrefKeys.EXTENSIONS_COMPLETE_PACKAGE, getDefaultExtensionsCompletePackage());
    }

    public static void setExtensionsCompletePackageValue(Context context, boolean value) {
        getMainSharedPreferences(context).edit().putBoolean(PrefKeys.EXTENSIONS_COMPLETE_PACKAGE, value).apply();
    }

    //endregion Complete Package

    //endregion Extensions Preferences

    //region Walkthrough Preferences

    //region Is Finished

    public static final boolean WALKTHROUGH_IS_FINISHED_DEFAULT = false;

    public static boolean getDefaultWalkthroughIsFinished() {
        return WALKTHROUGH_IS_FINISHED_DEFAULT;
    }

    public static boolean getWalkthroughIsFinishedValue(Context context) {
        return getMainSharedPreferences(context).getBoolean(PrefKeys.WALKTHROUGH_IS_FINISHED, getDefaultWalkthroughIsFinished());
    }

    public static void setWalkthroughIsFinishedValue(Context context, boolean value) {
        getMainSharedPreferences(context).edit().putBoolean(PrefKeys.WALKTHROUGH_IS_FINISHED, value).apply();
    }


    //endregion Is Finished

    //endregion Walkthrough Preferences

}
