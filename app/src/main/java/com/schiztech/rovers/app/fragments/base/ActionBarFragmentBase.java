package com.schiztech.rovers.app.fragments.base;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.schiztech.rovers.app.utils.LogUtils;

/**
 * Created by schiz_000 on 12/23/2014.
 */
public abstract class ActionBarFragmentBase extends SupportFragmentBase {
    public static final String TAG ="ActionBarFragmentBase";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(activity instanceof OnActionBarChangedListener){
            setOnActionBarChangedListener((OnActionBarChangedListener)activity);
        }
        else{
            LogUtils.LOGE(TAG, "Activity isn't implementing OnActionBarChangedListener!");
            throw new IllegalArgumentException("Activity isn't implementing OnActionBarChangedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActionBarChanged = null;
    }

    protected void setActionBar(Toolbar toolbar){
        if(mActionBarChanged != null)
            mActionBarChanged.onActionBarChanged(toolbar);
    }

    protected void setSystemColor(@ColorRes int colorRes){
        if(mActionBarChanged != null)
            mActionBarChanged.onSystemColorChanged(colorRes);

    }

    public abstract void updateActionBar();

    protected Context getContext(){
        return getActivity().getApplicationContext();
    }

    //region OnActionBarChangedListener

    public interface OnActionBarChangedListener{
        void onActionBarChanged(Toolbar toolbar);
        void onSystemColorChanged(@ColorRes int colorRes);
    }

    protected OnActionBarChangedListener mActionBarChanged;

    protected void setOnActionBarChangedListener(OnActionBarChangedListener listener){
        mActionBarChanged = listener;
    }


    //endregion OnActionBarChangedListener

}
