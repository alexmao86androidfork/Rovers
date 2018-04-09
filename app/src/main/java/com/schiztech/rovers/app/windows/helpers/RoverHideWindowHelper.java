package com.schiztech.rovers.app.windows.helpers;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.Utils;
import com.schiztech.rovers.app.windows.FloatingWindowsManager;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

/**
 * Created by schiz_000 on 5/25/2014.
 */
public class RoverHideWindowHelper extends WindowHelperBase {
    private static final String TAG = LogUtils.makeLogTag("RoverHideWindowHelper");
    View mView;


    protected RoverHideWindowHelper(Context context) {
        super(context);
    }

    @Override
    public int getID() {
        return WINDOW_ID_ROVER_HIDE;
    }

    @Override
    public View getView() {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.window_hide, null);

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
        return   StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE;
    }


    @Override
    protected boolean isPointInWindow(Window window, Point point, int fromId) {
        if(window == null || mView == null){
            return false;
        }

        ImageView hideIcon = (ImageView) mView.findViewById(R.id.hide_icon);
        TextView hideLabel = (TextView) mView.findViewById(R.id.hide_label);
        int[] location = new int[2];
        hideIcon.getLocationInWindow(location);

        int xAreaRight = location[0] + hideIcon.getWidth() + hideLabel.getWidth();
        int xAreaLeft = location[0];
        int yAreaTop = location[1];
        int yAreaBottom = location[1] + hideIcon.getHeight();

        if (point.x <= xAreaRight && point.x >= xAreaLeft
                    && point.y <= yAreaBottom && point.y >= yAreaTop) {
            if(!hideIcon.isPressed()) {
                hideIcon.setPressed(true);
                hideIcon.setImageResource(R.drawable.ic_action_cancel_pressed);
                hideLabel.setTextColor(0xffff4444);
            }
            return true;
        }

        if(hideIcon.isPressed()) {
            hideIcon.setPressed(false);
            hideIcon.setImageResource(R.drawable.ic_action_cancel_regular);
            hideLabel.setTextColor(0xffffffff);
        }

        return false;
    }

    @Override
    public void onWindowShown(){
       mView.setTranslationY(-200);
       mView.setAlpha(0);
       mView.animate()
               .alpha(1)
               .translationY(0)
               .setStartDelay(300)
               .setDuration(400)
               .setInterpolator(new DecelerateInterpolator())
               .setListener(new Animator.AnimatorListener() {
                   @Override
                   public void onAnimationStart(Animator animator) {
                       mIsShowAnimationStarted = true;
                   }

                   @Override
                   public void onAnimationEnd(Animator animator) {
                       animator.removeAllListeners();
                   }

                   @Override
                   public void onAnimationCancel(Animator animator) {
                       animator.removeAllListeners();
                   }

                   @Override
                   public void onAnimationRepeat(Animator animator) {

                   }
               })
               .start();


    }

    boolean mIsShowAnimationStarted = false;
    boolean mIsCloseAnimationFinished = false;
    @Override
    public boolean onClose(){
        if(!mIsCloseAnimationFinished && mIsShowAnimationStarted){
            Point screenDims = Utils.getDisplayDimensions(getContext());
            mView.animate()
                    .alpha(0)
                    .translationY(-200)
                    .setDuration(200)
                    .setInterpolator(new AccelerateInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            mIsCloseAnimationFinished = true;
                            sendData(FloatingWindowsManager.DISREGARD_ID, FloatingWindowsManager.DATA_REQ_CLOSE, null);

                            animator.removeAllListeners();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {
                            mIsCloseAnimationFinished = true;
                            sendData(FloatingWindowsManager.DISREGARD_ID, FloatingWindowsManager.DATA_REQ_CLOSE, null);

                            animator.removeAllListeners();
                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    })
                    .start();
            return true;
        }
        else{
            return false;
        }
    }



    @Override
    public Animation getShowAnimation() {
        return null;
    }

    @Override
    public Animation getHideAnimation() {
        return null;
    }

    @Override
    public Animation getCloseAnimation() {
        return null;
    }

}
