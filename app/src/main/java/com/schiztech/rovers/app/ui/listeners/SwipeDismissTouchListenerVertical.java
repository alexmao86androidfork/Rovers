package com.schiztech.rovers.app.ui.listeners;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by schiz_000 on 6/6/2014.
 */
public class SwipeDismissTouchListenerVertical extends SwipeDismissTouchListener{
    /**
     * Constructs a new swipe-to-dismiss touch listener for the given view.
     *
     * @param view      The view to make dismissable.
     * @param token     An optional token/cookie object to be passed through to the callback.
     * @param callbacks The callback to trigger when the user has indicated that she would like to
     */
    public SwipeDismissTouchListenerVertical(View view, Object token) {
        super(view, token);
    }

    //region Directional Related Methods

    @Override
    protected void offsetLocation(MotionEvent motionEvent){
        motionEvent.offsetLocation(0, mDirectionalTranslation);
    }

    @Override
    protected int getViewLength(View view){
        return view.getHeight();
    }

    @Override
    protected int getViewPlumbLength(View view){return view.getWidth();}

    @Override
    protected float getDirectionalDelta(MotionEvent motionEvent){
        return motionEvent.getRawY() - mDownY;
    }

    @Override
    protected float getPlumbDelta(MotionEvent motionEvent){
        return motionEvent.getRawX() - mDownX;
    }

    @Override
    protected float getDirectionalVelocity(){
        return mVelocityTracker.getYVelocity();
    }

    @Override
    protected float getPlumbVelocity(){
        return mVelocityTracker.getXVelocity();
    }

    @Override
    protected void animateViewDismiss(boolean isWithDirection){
        mView.animate()
                .translationY(isWithDirection ? mViewLength : -mViewLength)
                .alpha(0)
                .setDuration(mAnimationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        performDismiss();
                    }
                });
    }

    @Override
    protected void animateViewCancel(){
        mView.animate()
                .translationY(0)
                .alpha(1)
                .setDuration(mAnimationTime)
                .setListener(null);
    }

    @Override
    protected void setViewDirectionalTranslation(float translation){
        mView.setTranslationY(translation);
    }

    @Override
    protected void setLayoutParamsPlumbValue(ViewGroup.LayoutParams lp, int plumbValue){
        lp.width = plumbValue;
    }

    //endregion Directional Related Methods
}
