package com.schiztech.rovers.app.ui.listeners;
/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ListActivity;
import android.app.ListFragment;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.schiztech.rovers.app.utils.Utils;

/**
 * A {@link View.OnTouchListener} that makes any {@link View} dismissable when the
 * user swipes (drags her finger) horizontally across the view.
 * <p/>
 * <p><em>For {@link ListView} list items that don't manage their own touch events
 * (i.e. you're using
 * {@link ListView#setOnItemClickListener(AdapterView.OnItemClickListener)}
 * or an equivalent listener on {@link ListActivity} or
 * {@link ListFragment}, use SwipeDismissListViewTouchListener} instead.</em></p>
 * <p/>
 * <p>Example usage:</p>
 * <p/>
 * <pre>
 * view.setOnTouchListener(new SwipeDismissTouchListener(
 *         view,
 *         null, // Optional token/cookie object
 *         new SwipeDismissTouchListener.OnDismissCallback() {
 *             public void onDismiss(View view, Object token) {
 *                 parent.removeView(view);
 *             }
 *         }));
 * </pre>
 * <p/>
 * <p>This class Requires API level 12 or later due to use of {@link
 * android.view.ViewPropertyAnimator}.</p>
 *
 * @see //SwipeDismissListViewTouchListener
 */
public abstract class SwipeDismissTouchListener implements View.OnTouchListener {
    // Cached ViewConfiguration and system-wide constant values
    protected int mSlop;
    protected int mMinFlingVelocity;
    protected int mMaxFlingVelocity;
    protected long mAnimationTime;

    // Fixed properties
    protected View mView;
    protected DismissCallbacks mCallbacks;
    protected int mViewLength = 1; // 1 and not 0 to prevent dividing by zero

    // Transient properties
    protected float mDownX;
    protected float mDownY;

    protected int mSwipingSlop;
    protected Object mToken;
    protected VelocityTracker mVelocityTracker;
    protected float mDirectionalTranslation;

    //region Swiping Flag

    private boolean pSwiping;

    private void setSwiping(boolean isSwiping){
        boolean oldValue = pSwiping;
        pSwiping = isSwiping;

        if(pSwiping != oldValue) {//only announce on new value of the swiping flag
            if (pSwiping) {
                Utils.Vibrate(mView.getContext(), Utils.VIBRATE_MINIMAL);
                mCallbacks.onSwipingStarted(mView);
            }
            else
                mCallbacks.onSwipingFinished(mView);
        }
    }

    public boolean isSwiping(){
        return pSwiping;
    }

    //endregion Swiping Flag

    /**
     * The callback interface used by {@link SwipeDismissTouchListener} to inform its client
     * about a successful dismissal of the view for which it was created.
     */
    public interface DismissCallbacks {
        /**
         * Called to determine whether the view can be dismissed.
         */
        boolean canDismiss(Object token);


        /**
         * Called to indicate that the view started swiping
         * @param view The originating {@link View} .
         */
        void onSwipingStarted(View view);


        /**
         * Called to indicate that the view finished swiping
         * @param view The originating {@link View} .
         */
        void onSwipingFinished(View view);

        /**
         * Called when the user has indicated they she would like to dismiss the view.
         *
         * @param view  The originating {@link View} to be dismissed.
         * @param token The optional token passed to this object's constructor.
         */
        void onDismiss(View view, Object token);
    }

    /**
     * Constructs a new swipe-to-dismiss touch listener for the given view.
     *
     * @param view      The view to make dismissable.
     * @param token     An optional token/cookie object to be passed through to the callback.

     */
    public SwipeDismissTouchListener(View view, Object token) {
        ViewConfiguration vc = ViewConfiguration.get(view.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = view.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mView = view;
        mToken = token;

    }

    public void setCallbacksListener(DismissCallbacks callbacks){
        mCallbacks = callbacks;
    }

    //region Directional Related Methods

    protected abstract void offsetLocation(MotionEvent motionEvent);

    protected abstract int getViewLength(View view);

    protected abstract int getViewPlumbLength(View view);

    protected abstract float getDirectionalDelta(MotionEvent motionEvent);

    protected abstract float getPlumbDelta(MotionEvent motionEvent);

    protected abstract float getDirectionalVelocity();

    protected abstract float getPlumbVelocity();

    protected abstract void animateViewDismiss(boolean isWithDirection);

    protected abstract void animateViewCancel();
    protected abstract void setViewDirectionalTranslation(float translation);

    protected abstract void setLayoutParamsPlumbValue(ViewGroup.LayoutParams lp, int plumbValue);

    //endregion Directional Related Methods

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // offset because the view is translated during swipe
        offsetLocation(motionEvent);

        if (mViewLength < 2) {
            mViewLength = getViewLength(mView);
        }

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                // TODO: ensure this is a finger, and set a flag
                mDownX = motionEvent.getRawX();
                mDownY = motionEvent.getRawY();
                if (mCallbacks.canDismiss(mToken)) {
                    mVelocityTracker = VelocityTracker.obtain();
                    mVelocityTracker.addMovement(motionEvent);
                }
                return false;
            }

            case MotionEvent.ACTION_UP: {
                if (mVelocityTracker == null) {
                    break;
                }

                float directionalDelta = getDirectionalDelta(motionEvent);
                mVelocityTracker.addMovement(motionEvent);
                mVelocityTracker.computeCurrentVelocity(1000);
                float directionalVelocity = getDirectionalVelocity();
                float absDirectionalVelocity = Math.abs(directionalVelocity);
                float absPlumbVelocity = Math.abs(getPlumbVelocity());
                boolean dismiss = false;
                boolean dismissRight = false;
                if (Math.abs(directionalDelta) > mViewLength / 2 && isSwiping()) {
                    dismiss = true;
                    dismissRight = directionalDelta > 0;
                } else if (mMinFlingVelocity <= absDirectionalVelocity && absDirectionalVelocity <= mMaxFlingVelocity
                        && absPlumbVelocity < absDirectionalVelocity
                        && absPlumbVelocity < absDirectionalVelocity && isSwiping()) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = (directionalVelocity < 0) == (directionalDelta < 0);
                    dismissRight = getDirectionalVelocity() > 0;
                }
                if (dismiss) {
                    // dismiss
                    animateViewDismiss(dismissRight);
                } else if (isSwiping()) {
                    // cancel
                    animateViewCancel();
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDirectionalTranslation = 0;
                mDownX = 0;
                mDownY = 0;
                setSwiping(false);
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                if (mVelocityTracker == null) {
                    break;
                }

                animateViewCancel();
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDirectionalTranslation = 0;
                mDownX = 0;
                mDownY = 0;
                setSwiping(false);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mVelocityTracker == null) {
                    break;
                }

                mVelocityTracker.addMovement(motionEvent);
                float directionalDelta = getDirectionalDelta(motionEvent);
                float plumbDelta = getPlumbDelta(motionEvent);
                if (Math.abs(directionalDelta) > mSlop && Math.abs(plumbDelta) < Math.abs(directionalDelta) / 2) {
                    setSwiping(true);
                    mSwipingSlop = (directionalDelta > 0 ? mSlop : -mSlop);
                    mView.getParent().requestDisallowInterceptTouchEvent(true);

                    // Cancel listview's touch
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (motionEvent.getActionIndex() <<
                                    MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mView.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }

                if (isSwiping()) {
                    mDirectionalTranslation = directionalDelta;
                    setViewDirectionalTranslation(mDirectionalTranslation - mSwipingSlop);
                    // TODO: use an ease-out interpolator or such
                    mView.setAlpha(Math.max(0f, Math.min(1f,
                            1f - 2f * Math.abs(directionalDelta) / mViewLength)));
                    return true;
                }
                break;
            }
        }
        return false;
    }

    protected void performDismiss() {
        // Animate the dismissed view to zero-height and then fire the dismiss callback.
        // This triggers layout on each animation frame; in the future we may want to do something
        // smarter and more performant.

        final ViewGroup.LayoutParams lp = mView.getLayoutParams();
        final int originalPlumbLength = getViewPlumbLength(mView);

        ValueAnimator animator = ValueAnimator.ofInt(originalPlumbLength, 1).setDuration(mAnimationTime);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCallbacks.onDismiss(mView, mToken);
                // Reset view presentation
                //mView.setAlpha(1f);
                //setViewDirectionalTranslation(0);
                //setLayoutParamsPlumbValue(lp, originalPlumbLength);
                //mView.setLayoutParams(lp);
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setLayoutParamsPlumbValue(lp,  (Integer) valueAnimator.getAnimatedValue());
                mView.setLayoutParams(lp);
            }
        });

        animator.start();
    }
}