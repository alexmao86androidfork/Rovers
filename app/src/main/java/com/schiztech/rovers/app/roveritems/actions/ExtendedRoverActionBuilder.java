package com.schiztech.rovers.app.roveritems.actions;

import android.content.Intent;

import com.schiztech.rovers.api.RoversActionBuilder;

public class ExtendedRoverActionBuilder extends RoversActionBuilder{
    public ExtendedRoverActionBuilder() {
        super();
    }

    public static final int INTERACTIVE_ACTION_ID_INDEX = 0;
    public static final int INTERACTIVE_ACTION_ICON_INDEX = 1;
    public static final int INTERACTIVE_ACTION_BACKGROUND_INDEX = 2;

    public static final String INTERACTIVE_ACTION_ID = "_id";
    public static final String INTERACTIVE_ACTION_ICON= "icon";
    public static final String INTERACTIVE_ACTION_BACKGROUND = "background";

    public static String[] INTERACTIVE_ACTION_TYPE = new String[] {INTERACTIVE_ACTION_ID, INTERACTIVE_ACTION_ICON, INTERACTIVE_ACTION_BACKGROUND};

    public static String CONTENT_TYPE = "vnd.android.cursor.dir/com.schiztech.rovers.interactive_action";

    public static final String KEY_IS_INTERACTIVE = "rovers_action_key_is_interactive";
    private boolean mIsInteractive = false;

    public static final String KEY_IS_COLOR_INTERACTIVE = "rovers_action_key_is_color_interactive";
    private boolean mIsColorInteractive = true;

    public static final String KEY_IS_ICON_INTERACTIVE = "rovers_action_key_is_icon_interactive";
    private boolean mIsIconInteractive = true;

    public static final String KEY_CONTENT_URI = "rovers_action_key_content_uri";
    private String mContentUri = null;

    public ExtendedRoverActionBuilder setIsInteractive(boolean isInteractive){
        mIsInteractive = isInteractive;
        return this;
    }

    public ExtendedRoverActionBuilder setIsColorInteractive(boolean isColorInteractive){
        mIsColorInteractive = isColorInteractive;
        return this;
    }

    public ExtendedRoverActionBuilder setIsIconInteractive(boolean isIconInteractive){
        mIsIconInteractive = isIconInteractive;
        return this;
    }

    public ExtendedRoverActionBuilder setContentUri(String contentUri){
        mContentUri = contentUri;
        return this;
    }

    @Override
    public Intent create(){
        Intent intent = super.create();

        if(mIsInteractive == true && mContentUri == null)
            throw new IllegalArgumentException("Interactive rover action must have an authority");


        intent.putExtra(KEY_IS_INTERACTIVE, mIsInteractive);
        intent.putExtra(KEY_IS_COLOR_INTERACTIVE, mIsColorInteractive);
        intent.putExtra(KEY_IS_ICON_INTERACTIVE, mIsIconInteractive);

        intent.putExtra(KEY_CONTENT_URI, mContentUri);



        return intent;
    }

}