package com.schiztech.rovers.app.utils;

/**
 * Created by schiz_000 on 5/13/2014.
 */


public class PrefKeys {
    private static final String TAG = LogUtils.makeLogTag("PrefsKeys");

    //region Rover Window Preferences

    public static final String ROVER_WINDOW_X = "rover_window_x";
    public static final String ROVER_WINDOW_Y = "rover_window_y";

    public static final String ROVER_HIDDEN_ALERT_IS_SHOWN = "rover_hidden_alert_is_shown";

    //endregion Rover Window Preferences

    //region Roverlytics Preferences

    public static final String ROVERLYTICS_LAUNCHES = "Roverlytics_Launches";
    public static final String ROVERLYTICS_TOTAL_TIME = "Roverlytics_Total_Time";
    public static final String ROVERLYTICS_DISTANCE = "Roverlytics_Distance";
    public static final String ROVERLYTICS_ITEMS_COUNT = "Roverlytics_Items_Count";

    //endregion Roverlytics

    //region Application Preferences

    public static final String ROVERS_IS_ACTIVATED = "Rovers_Is_Activated";

    public static final String TRIGGER_REST_ALPHA = "Rovers_Trigger_Rest_Alpha";
    public static final String TRIGGER_REST_OFFSET = "Rovers_Trigger_Rest_Offset";
    public static final String TRIGGER_BACKGROUND_COLOR = "Rovers_Trigger_Background_Color";
    public static final String TRIGGER_ICON_COLOR = "Rovers_Trigger_Icon_Color";
    public static final String TRIGGER_INDEPENDENT_SIZE = "Rovers_Trigger_Independent_Size";
    public static final String TRIGGER_ITEM_SIZE = "Rovers_Trigger_Item_Size";

    public static final String HOST_STICKY_CORNER = "Rovers_Host_Sticky_Corner";
    public static final String HOST_ORIENTATION = "Rovers_Host_Orientation";
    public static final String HOST_ADD_ON_EDIT_ONLY = "Rovers_Host_Add_On_Edit_Only";

    public static final String ITEMS_DEFAULT_APPLICATION_COLOR = "Rovers_Items_Default_Application_Color";
    public static final String ITEMS_DEFAULT_SHORTCUT_COLOR = "Rovers_Items_Default_Shortcut_Color";
    public static final String ITEMS_DEFAULT_FOLDER_COLOR = "Rovers_Items_Default_Folder_Color";
    public static final String ITEMS_ITEM_SIZE = "Rovers_Items_Item_Size";

    public static final String MISC_MUTE_CLICK_SOUND = "Rovers_Misc_Mute_Click_Sound";
    public static final String MISC_SEND_ANONYMOUS_DATA = "Rovers_Misc_Anonymous_Data";

    //endregion Preferences

    //region Extensions Preferences

    public static final String EXTENSIONS_MORE_SETTINGS = "Extensions_More_Settings";
    public static final String EXTENSIONS_MORE_COLORS = "Extensions_More_Colors";
    public static final String EXTENSIONS_MORE_ROVERS = "Extensions_More_Rovers";
    public static final String EXTENSIONS_COMPLETE_PACKAGE = "Extensions_Complete_Package";

    //endregion Extensions Preferences

    //region Walkthrough Preferences

    public static final String WALKTHROUGH_IS_FINISHED = "Walkthrough_Is_Finished";

    //endregion Walkthrough Preferences
}
