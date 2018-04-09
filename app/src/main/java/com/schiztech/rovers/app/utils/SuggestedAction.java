package com.schiztech.rovers.app.utils;

import android.content.ComponentName;
import android.content.Context;

import com.schiztech.rovers.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schiz_000 on 5/22/2015.
 */
public class SuggestedAction {
    public String title;
    public String description;
    public int iconRes;
    public String packageName;

    //region Suggested Actions

    private static List<SuggestedAction> sSuggestedActions;

    public static List<SuggestedAction> getSuggestedActions(Context context) {
        if (sSuggestedActions == null) {
            populateSuggestedActions(context);
        }

        return sSuggestedActions;
    }

    public static void clearSuggestedActions() {
        if (sSuggestedActions != null)
            sSuggestedActions.clear();

        sSuggestedActions = null;
    }

    private static void populateSuggestedActions(Context context) {
        clearSuggestedActions();
        sSuggestedActions = new ArrayList<>();

        sSuggestedActions.add(getBatterySuggestedRoverAction(context));
        sSuggestedActions.add(getContactSuggestedRoverAction(context));
        sSuggestedActions.add(getFlashlightSuggestedRoverAction(context));
        sSuggestedActions.add(getSettingsSuggestedRoverAction(context));
    }

    private static SuggestedAction getBatterySuggestedRoverAction(Context context) {
        SuggestedAction batteryPromo = new SuggestedAction();
        batteryPromo.title = Utils.getString(context, R.string.roveraction_suggested_batterylevel_label);
        batteryPromo.description = Utils.getString(context, R.string.roveraction_suggested_batterylevel_desc);
        batteryPromo.iconRes = R.drawable.ic_suggested_batteryaction;
        batteryPromo.packageName = Utils.getString(context, R.string.roveraction_suggested_batterylevel_pkg);

        return batteryPromo;
    }

    private static SuggestedAction getContactSuggestedRoverAction(Context context) {
        SuggestedAction batteryPromo = new SuggestedAction();
        batteryPromo.title =  Utils.getString(context, R.string.roveraction_suggested_directcontact_label);
        batteryPromo.description = Utils.getString(context, R.string.roveraction_suggested_directcontact_desc);
        batteryPromo.iconRes = R.drawable.ic_suggested_contactaction;
        batteryPromo.packageName = Utils.getString(context, R.string.roveraction_suggested_directcontact_pkg);

        return batteryPromo;
    }

    private static SuggestedAction getFlashlightSuggestedRoverAction(Context context) {
        SuggestedAction batteryPromo = new SuggestedAction();
        batteryPromo.title = Utils.getString(context, R.string.roveraction_suggested_flashlight_label);
        batteryPromo.description = Utils.getString(context, R.string.roveraction_suggested_flashlight_desc);
        batteryPromo.iconRes = R.drawable.ic_suggested_flashlightaction;
        batteryPromo.packageName = Utils.getString(context, R.string.roveraction_suggested_flashlight_pkg);

        return batteryPromo;
    }

    private static SuggestedAction getSettingsSuggestedRoverAction(Context context) {
        SuggestedAction batteryPromo = new SuggestedAction();
        batteryPromo.title = Utils.getString(context, R.string.roveraction_suggested_settings_label);
        batteryPromo.description = Utils.getString(context, R.string.roveraction_suggested_settings_desc);
        batteryPromo.iconRes = R.drawable.ic_suggested_settingsaction;
        batteryPromo.packageName = Utils.getString(context, R.string.roveraction_suggested_settings_pkg);

        return batteryPromo;
    }

    //endregion Promotional Actions

    public ActivityInfo toActivityInfo(Context context){
        ActivityInfo ai = new ActivityInfo();
        ai.icon = Utils.getDrawable(context, iconRes);
        ai.label = title;
        ai.componentName = new ComponentName(packageName, "");//only set package - for linking to market.
        ai.isForDownload = true;
        return ai;
    }
}

