package com.schiztech.rovers.app.roveritems;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.schiztech.rovers.app.utils.RoversUtils;
import com.schiztech.rovers.app.utils.Utils;

/**
 * Created by schiz_000 on 5/11/2014.
 */
public interface IRover {

    public static int ROVER_DEFAULT_COLOR_VALUE = Integer.MIN_VALUE;
    public static RoversUtils.RoverIcon ROVER_DEFAULT_ICON_VALUE = RoversUtils.RoverIcon.NONE;
    public enum RoverType {Application, Shortcut, Folder, BasicAction, InteractiveAction}

    Drawable getIcon(Context context);
    String getLabel(Context context);
    int getColor(Context context);
    int getDefaultColor(Context context);
    boolean isDefaultColor();

    void setColor(int color);
    void setLabel(String label);
    void setIcon(RoversUtils.RoverIcon icon);

    boolean isRoverIcon(Context context);

    boolean isIconChangeable();
    boolean isColorChangeable();

    void onDelete();

    String getDistinctID();
    boolean isIconCachable();
}
