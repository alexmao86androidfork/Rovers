package com.schiztech.rovers.app.roveritems;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.schiztech.rovers.app.utils.BitmapUtils;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.PrefUtils;
import com.schiztech.rovers.app.utils.RoversUtils;
import com.schiztech.rovers.app.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by schiz_000 on 5/12/2014.
 */
public class FolderRover implements IRover {
    private static final String TAG = LogUtils.makeLogTag("FolderRover");
    private static int FOLDER_CURRENT_VERSION = 1;

    private int mCurrentVersion;

    /**
     * folder's unique ID
     */
     private long mFolderID;

    //holds the folder mLabel
    private String mLabel = null;
    //holds the folder icon resource
    protected RoversUtils.RoverIcon mRoverIcon = IRover.ROVER_DEFAULT_ICON_VALUE;

    int mBackgroundColor = IRover.ROVER_DEFAULT_COLOR_VALUE;

    private List<IRover> mChildren;

    /**
     * GSON NEEDS ONLY!!
     */
    public FolderRover(){

    }

    public FolderRover(Context context) {
        this.mFolderID = RoversManager.generateNewFolderID(context);
    }

    public FolderRover(long folderID) {
        this.mFolderID = folderID;
    }

    //region Getters & Setters

    public int getIconResource(){
        if(mRoverIcon == IRover.ROVER_DEFAULT_ICON_VALUE)
            return getDefaultIconResource();

        return mRoverIcon.getResourceID();
    }

    /**
     * Sets the mLabel of the folder
     * @param mLabel
     */
    @Override
    public void setLabel(String mLabel) {
        this.mLabel = mLabel;
    }

    private String getDefaultLabel(){
        return "folder #" + mFolderID;
    }

    @Override
    public void setIcon(RoversUtils.RoverIcon icon) {
        this.mRoverIcon = icon;
    }

    @Override
    public boolean isRoverIcon(Context context){
        return true;
    }

    @Override
    public boolean isIconChangeable() {
        return true;
    }

    @Override
    public boolean isColorChangeable() {
        return true;
    }

    /**
     * Gets the folder identification
     * @return
     */
    public long getFolderID() {
        return mFolderID;
    }



    //endregion

    //region IRover Methods

    @Override
    public Drawable getIcon(Context context) {
        int icon = getDefaultIconResource();
        if(mRoverIcon != IRover.ROVER_DEFAULT_ICON_VALUE)
            icon = mRoverIcon.getResourceID();


        int color = getColor(context);

        return BitmapUtils.recolorDrawableByBackground(context, icon, color);
    }

    private int getDefaultIconResource(){
        return RoversUtils.RoverIcon.Folders_Round.getResourceID();
    }

    @Override
    public String getLabel(Context context) {
        if(mLabel == null)
            return getDefaultLabel();

        return mLabel;
    }

    @Override
    public int getColor(Context context) {
        if(mBackgroundColor == IRover.ROVER_DEFAULT_COLOR_VALUE)
            return getDefaultColor(context);
        return mBackgroundColor;
    }

    @Override
    public void setColor(int color) {
        mBackgroundColor = color;
    }

    @Override
    public boolean isDefaultColor(){
        return mBackgroundColor == IRover.ROVER_DEFAULT_COLOR_VALUE;
    }

    @Override
    public int getDefaultColor(Context context){
        return PrefUtils.getDefaultFolderColorValue(context);
    }


    @Override
    public void onDelete() {
        if(mChildren == null) return;

        for(IRover child : mChildren){
            child.onDelete();
        }
    }

    @Override
    public String getDistinctID() {
        return TAG + mFolderID;
    }

    @Override
    public boolean isIconCachable() {
        return true;
    }

    //endregion

    //region Children Methods

    /**
     * getter for the mChildren list.
     * makes sure that mChildren is initialized
     * @return READONLY child list
     */
    public List<IRover> getChildren() {
        //make sure mChildren array is initailized
        if(mChildren == null)
            mChildren = new ArrayList<IRover>();

        return Collections.unmodifiableList(mChildren);
    }

    public boolean addChild(IRover newChild){
        if(newChild == null)//not supporting adding null!
            return false;

        if(mChildren == null)
            mChildren = new ArrayList<IRover>();

        return mChildren.add(newChild);
    }

    public boolean addChild(int position, IRover newChild){
        if(newChild == null)//not supporting adding null!
            return false;

        if(mChildren == null)
            mChildren = new ArrayList<IRover>();

        try {
            mChildren.add(position, newChild);
            return true;
        }

        catch(Exception e){
            LogUtils.LOGE(TAG, "Couldn't add rover child: " +e.getStackTrace());
            return false;
        }


    }

    public boolean removeChild(IRover child){
        int childPosition = mChildren.indexOf(child);
        if(childPosition != -1){
            mChildren.get(childPosition).onDelete();
        }
        return mChildren.remove(child);
    }

    public boolean removeChildAt(int position){
        if(position!= -1){
            mChildren.get(position).onDelete();
        }

        return mChildren.remove(position) != null;
    }


    //endregion

}
