package com.schiztech.rovers.app.windows.helpers;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Point;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.PrefUtils;
import com.schiztech.rovers.app.utils.Utils;
import com.schiztech.rovers.app.windows.FloatingWindowsManager;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;

/**
 * Created by schiz_000 on 7/22/2014.
 */
public class HiddenAlertWindowHelper extends WindowHelperBase {
    private static final String TAG = LogUtils.makeLogTag("HiddenAlertWindowHelper");
    View mView;
    View mLayout;
    View mButton;

    public HiddenAlertWindowHelper(Context context)
    {
        super(context);
    }

    @Override
    public int getID() {
        return WINDOW_ID_HIDDEN_ALERT;
    }

    //region View & Decor Methods
    @Override
    public View getView() {

        mView = LayoutInflater.from(getContext()).inflate(R.layout.window_hidden_alert, null);
        mLayout = mView.findViewById(R.id.hiddenAlert_Layout);
        mLayout.setOnClickListener(mDismissClick);
        mLayout.setSoundEffectsEnabled(false);

        mButton = mView.findViewById(R.id.hiddenAlert_Button);
        mButton.setOnClickListener(mDismissClick);

        return mView;
    }

    @Override
    public StandOutWindow.StandOutLayoutParams getParams(FloatingWindowsManager windowsManager) {
        return windowsManager.getParamsInstance(getID(),
                StandOutWindow.StandOutLayoutParams.MATCH_PARENT,
                StandOutWindow.StandOutLayoutParams.WRAP_CONTENT,
                StandOutWindow.StandOutLayoutParams.LEFT,
                StandOutWindow.StandOutLayoutParams.TOP);
    }

    @Override
    public int getFlags() {
        return StandOutFlags.FLAG_WINDOW_FOCUS_INDICATOR_DISABLE;
    }
    //endregion View & Decor Methods

    private View.OnClickListener mDismissClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            sendData(FloatingWindowsManager.DISREGARD_ID, FloatingWindowsManager.DATA_REQ_CLOSE, null);
            //mark as shown
            PrefUtils.setHiddenAlertIsShownValue(getContext(), true);
        }
    };

    @Override
    public boolean onTouchBody(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            sendData(FloatingWindowsManager.DISREGARD_ID, FloatingWindowsManager.DATA_REQ_CLOSE, null);
            return true;

        }
        return false;
    }


    @Override
    public void onWindowShown(){
        requestDim();
    }

    @Override
    public boolean onClose(){
        requestUndim();
        if(mButton != null){
            mButton.setOnClickListener(null);
            mButton = null;
        }
        if(mLayout != null) {
            mLayout.setOnClickListener(null);
            mLayout = null;
        }
        return false;
    }


    @Override
    public Animation getShowAnimation() {
        return AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
    }

    @Override
    public Animation getHideAnimation() {
        return null;
    }

    @Override
    public Animation getCloseAnimation() {
        return AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
    }


    public boolean onKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    sendData(FloatingWindowsManager.DISREGARD_ID, FloatingWindowsManager.DATA_REQ_CLOSE, null);
                    return true;
            }
        }

        return false;
    }

}
