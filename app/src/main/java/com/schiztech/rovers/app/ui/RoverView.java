package com.schiztech.rovers.app.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.roveritems.IRover;
import com.schiztech.rovers.app.ui.handlers.DragAndDropHandler;
import com.schiztech.rovers.app.ui.listeners.SwipeDismissTouchListener;
import com.schiztech.rovers.app.utils.BitmapUtils;
import com.schiztech.rovers.app.utils.CacheUtils;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.PrefUtils;
import com.schiztech.rovers.app.utils.RoversUtils;
import com.schiztech.rovers.app.utils.Utils;

import java.lang.ref.WeakReference;

/**
 * Created by schiz_000 on 5/22/2014.
 */
public class RoverView extends FrameLayout {
    private static final String TAG = LogUtils.makeLogTag("RoverView");
    private static final int DEFAULT_COLOR = Color.BLACK;

    private boolean mIsTrigger = false;

    private boolean mIsSizeAdjustableToTrigger = false;



    //region C'tors

    private RoverView(Context context, int layout) {
        super(context);
        init(layout);
    }
    private RoverView(Context context, int layout, boolean isTrigger) {
        super(context);
        mIsTrigger = isTrigger;
        init(layout);

    }

    //endregion



    private void init(int layout) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layout, null);
        addView(view);

        mTouchHandler = new TouchHandler();

        refreshRoverSize();



    }

    private void refreshRoverSize() {
        int iconSize = mIsTrigger ? RoversUtils.getTriggerSize(getContext()) : RoversUtils.getRoverSize(getContext());
        refreshRoverSize(iconSize);
    }

    private void refreshRoverSize(int size) {
        CircleButton icon = (CircleButton) findViewById(R.id.rover_icon);
        ImageView overlay = (ImageView) findViewById(R.id.rover_overlay);

        icon.setCircleSize(size);


        int paddingSize = Math.max(RoversUtils.getTriggerSize(getContext()), RoversUtils.getRoverSize(getContext()));

        FrameLayout padding = (FrameLayout) findViewById(R.id.roverItem_padding_vertical);
        if (padding != null) {
            padding.setMinimumWidth(mIsSizeAdjustableToTrigger ? paddingSize : 0);
        } else {
            padding = (FrameLayout) findViewById(R.id.roverItem_padding_horizontal);
            padding.setMinimumHeight(mIsSizeAdjustableToTrigger ? paddingSize : 0);
        }

        overlay.getLayoutParams().width = icon.getInnerWidth();
        overlay.getLayoutParams().height = icon.getInnerWidth();
        overlay.setAlpha(0f);
    }


    //region Getters

    public Drawable getDrawable(){
        CircleButton icon = (CircleButton) findViewById(R.id.rover_icon);
        return icon.getDrawable();
    }

    public int getCircleColor(){
        CircleButton icon = (CircleButton) findViewById(R.id.rover_icon);
        return icon.getCircleColor();
    }

    public int getCirclePressedColor(){
        CircleButton icon = (CircleButton) findViewById(R.id.rover_icon);
        if(icon != null) {
            return icon.getCircleColorPressed();
        }
        else return 0;
    }

    public View getCircleView(){
        return findViewById(R.id.rover_icon);
    }

    public boolean getIsTrigger(boolean isTrigger){
        return mIsTrigger;
    }


    //endregion

    //region Setters

    public void setIsSizeAdjustableToTrigger(boolean isAdjustable){
        mIsSizeAdjustableToTrigger = isAdjustable;
        refreshRoverSize();
    }


    public void setIsTrigger(boolean isTrigger){
        mIsTrigger = isTrigger;
        refreshRoverSize();
    }

    public void setTouchHandlerEnabled(boolean isEnabled) {
        CircleButton icon = (CircleButton) findViewById(R.id.rover_icon);
        icon.setOnTouchListener(isEnabled ? mTouchHandler : null);
    }

    public void setRoverValues(IRover rover) {

        Integer cachedColor = CacheUtils.getFromColorCache(rover.getDistinctID());

        if(cachedColor != null){
            setCircleColor(cachedColor);
        }

        if (rover.isRoverIcon(getContext())) {
            Drawable roverIcon = CacheUtils.getFromDrawableCache(rover.getDistinctID());
//            if(roverIcon == null){
//                roverIcon = rover.getIcon(getContext());
//                if(rover.isIconCachable()) {
//                    CacheUtils.addToDrawableCache(rover.getDistinctID(), roverIcon);
//                }
//
//            }
//
//            setCircleColor(rover.getColor(getContext()));
//            setIconDrawable(roverIcon);
            if(roverIcon != null){
                setIconDrawable(roverIcon);
                setCircleColor(rover.getColor(getContext()));
            }
            else {
                new RoverIconLoaderTask(this,rover, getContext()).execute();
            }
        }

        else {
            Bitmap cachedIcon = CacheUtils.getFromBitmapCache(rover.getDistinctID());
            if(cachedIcon != null) {
                setIconBitmap(cachedIcon);
                setCircleColor(rover.getColor(getContext()));
            }
            else {
                new BitmapIconLoaderTask(this, rover, getContext(), ((CircleButton) getCircleView()).getInnerWidth()).execute();
            }
        }

    }



    public void setCircleColor(int color) {
        setCircleColor(color, true);
    }

    public void setCircleColor(int color, boolean isStrokeAffected) {
        CircleButton icon = (CircleButton) findViewById(R.id.rover_icon);
        if(icon != null)
            icon.setCircleColor(color);
    }

    public void setIconBitmap(Bitmap bitmap) {
        CircleButton icon = (CircleButton) findViewById(R.id.rover_icon);
        if(icon != null)
            icon.setImageBitmap(bitmap);

    }

    public void setIconResource(int iconId) {
        CircleButton icon = (CircleButton) findViewById(R.id.rover_icon);
        if(icon != null)
            icon.setImageResource(iconId);
    }

    public void setIconDrawable(Drawable iconDrawable) {
        CircleButton icon = (CircleButton) findViewById(R.id.rover_icon);
        if(icon != null)
            icon.setImageDrawable(iconDrawable);


    }

    public void setButtonClickable(boolean clickable) {
        CircleButton icon = (CircleButton) findViewById(R.id.rover_icon);
        icon.setClickable(clickable);
    }

    public void setButtonPressed(boolean pressed) {
        CircleButton icon = (CircleButton) findViewById(R.id.rover_icon);
        if(icon != null)
            icon.setPressed(pressed);

    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        CircleButton icon = (CircleButton) findViewById(R.id.rover_icon);
        icon.setSoundEffectsEnabled(false);
        icon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RoverView.this.performClick();
            }
        });

    }

    @Override
    public void setClickable(boolean clickable) {
        super.setClickable(clickable);
        CircleButton icon = (CircleButton) findViewById(R.id.rover_icon);
        if(icon != null)
            icon.setClickable(clickable);

    }


    public void setSwipeToDismissListener(SwipeDismissTouchListener l) {
        mTouchHandler.setSwipeListener(l);
    }


    public void setDragAndDropHandler(DragAndDropHandler h) {
        mTouchHandler.setDragAndDropHandler(h);
    }

    public void setTouchActionsListener(OnTouchActionsListener l) {
        mTouchActionsListener = l;
    }

    public void setPlaceHolder(int size, Utils.Direction direction, OnTouchListener touchListener) {
        int width = (direction == Utils.Direction.Left || direction == Utils.Direction.Right) ?
                size : ViewGroup.LayoutParams.MATCH_PARENT;
        int height = (direction == Utils.Direction.Top || direction == Utils.Direction.Bottom) ?
                size : ViewGroup.LayoutParams.MATCH_PARENT;

        View view = new View(getContext());
        view.setLayoutParams( new ViewGroup.LayoutParams(width, height));
        view.setBackgroundColor(0x00000000);
        view.setOnTouchListener(touchListener);
        if(direction == Utils.Direction.Right || direction == Utils.Direction.Bottom) {
            ((ViewGroup) findViewById(R.id.roverItem_container)).addView(view);
        }

        else {
            ((ViewGroup)findViewById(R.id.roverItem_container)).addView(view,0);
        }
    }

    public void setLayoutGravity(int gravity){
        ((FrameLayout)this).setForegroundGravity(gravity);
        ((LinearLayout.LayoutParams)this.getLayoutParams()).gravity = gravity;
        ((LinearLayout)findViewById(R.id.roverItem_container)).setGravity(gravity);
    }

    public void markPressed(boolean pressed){
        if(getCircleView() != null){
            ((CircleButton)getCircleView()).markPressed(pressed);
        }
    }

    public void setScaleCircle(float scaleLevel){
        CircleButton icon = (CircleButton) findViewById(R.id.rover_icon);
        icon.setScaleLevel(scaleLevel);
    }

    public enum OverlayType{
        Delete,
        Edit,
        Drag
    }
    public static final float OVERLAY_ALPHA = 0.75f;
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public void showOverlay(OverlayType type){
        int color;
        int resouce;

        switch (type){
            case Delete:
                resouce = R.drawable.ic_action_trash;
                color = 0xffcc0000;
                break;
            case Edit:
                color = getCirclePressedColor();
                resouce = BitmapUtils.isColorDark(color) ? R.drawable.ic_action_edit : R.drawable.ic_action_edit_dark;
                break;
            case Drag:
                color = getCirclePressedColor();
                resouce = BitmapUtils.isColorDark(color) ? R.drawable.ic_action_drag : R.drawable.ic_action_drag_dark;
                break;
            default:
                resouce = R.drawable.ic_action_edit;
                color = 0x00000000;
                break;

        }


        CircleButton icon = (CircleButton) findViewById(R.id.rover_icon);
        ImageView overlay = (ImageView) findViewById(R.id.rover_overlay);

        if (Utils.isAndroidVersionEqualOrAbove(Build.VERSION_CODES.JELLY_BEAN)) {
            overlay.setBackground(BitmapUtils.getCircularDrawable(color, getContext().getResources(), false));
        }
        else{
            overlay.setBackgroundDrawable(BitmapUtils.getCircularDrawable(color, getContext().getResources(), false));
        }
        overlay.setImageResource(resouce);
        overlay.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

//        overlay.getLayoutParams().width = icon.getInnerWidth();
//        overlay.getLayoutParams().height = icon.getInnerWidth();

        if(type == OverlayType.Drag) {//set immediately
            overlay.setAlpha(OVERLAY_ALPHA);
        }
        else{
            overlay.animate().alpha(OVERLAY_ALPHA).setDuration(400).setListener(null).start();
        }
    }

    public void hideOverlay(){
        final ImageView overlay = (ImageView) findViewById(R.id.rover_overlay);

        overlay.animate().alpha(0f).setDuration(400).start();


    }

    //endregion Setters

    //region Animation
    protected Spring mShakeSpring;
    public void animateShake(){
        final CircleButton circleButton = (CircleButton) findViewById(R.id.rover_icon);

        mShakeSpring = Utils.getSpringSystem().createSpring();
        mShakeSpring.setSpringConfig(new SpringConfig(100,0));
        mShakeSpring.setCurrentValue(0);
        mShakeSpring.addListener(new SpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                circleButton.setRotation((float)(-3f + 6*spring.getCurrentValue()));
            }

            @Override
            public void onSpringAtRest(Spring spring) {

            }

            @Override
            public void onSpringActivate(Spring spring) {
            }

            @Override
            public void onSpringEndStateChange(Spring spring) {

            }
        });

        //start!
        mShakeSpring.setEndValue(1);
    }

    public void cancelShake(){
        if(mShakeSpring != null){
            mShakeSpring.setAtRest();
            mShakeSpring.removeAllListeners();
            mShakeSpring = null;
        }

        CircleButton circleButton = (CircleButton) findViewById(R.id.rover_icon);
        circleButton.setRotation(0);
        circleButton.clearAnimation();
    }

    Spring mPopOutSpring;
    public void animateOut(final int delay, final float currentValue, final Utils.AnimationFinishedListener listener){
//        final CircleButton circleButton = (CircleButton) findViewById(R.id.rover_icon);
//
//        circleButton.animate().alpha(0).scaleY(0).scaleX(0).setDuration(250).setInterpolator(new DecelerateInterpolator()).setStartDelay(delay)
//                .setListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animator) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animator) {
//                        if(listener != null)
//                            listener.onAnimationFinished();
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animator) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animator) {
//
//                    }
//                })
//                .start();
        if(mPopInSpring != null)
            mPopInSpring.setAtRest();

        final CircleButton circleButton = (CircleButton) findViewById(R.id.rover_icon);

        mPopOutSpring = Utils.getSpringSystem().createSpring();
        mPopOutSpring.setSpringConfig(new SpringConfig(500,10));
        mPopOutSpring.setOvershootClampingEnabled(true);
        mPopOutSpring.setCurrentValue(0f);
//        mPopOutSpring.setVelocity(3);
        mPopOutSpring.addListener(new SpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float scaleValue = (float)Math.max(0, 1- spring.getCurrentValue()*1);
                scaleValue = Math.min(circleButton.getScaleX(), scaleValue);

                float alphaValue = (float)Math.min(circleButton.getAlpha(), 1- spring.getCurrentValue()*1.25);//faster fade
                alphaValue = Math.max(0, alphaValue);

                circleButton.setScaleX(scaleValue);
                circleButton.setScaleY(scaleValue);
                circleButton.setAlpha(alphaValue);

                if(alphaValue == 0 && scaleValue == 0)
                    spring.setAtRest();
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                if(listener != null)
                    listener.onAnimationFinished();

                //recycle bitmap which is no longer in use.
                Drawable d = circleButton.getDrawable();
                circleButton.setImageDrawable(null);

                mPopOutSpring = null;
                spring.removeAllListeners();
//                if(d instanceof BitmapDrawable) {
//                    ((BitmapDrawable) d).getBitmap().recycle();
//                }
            }

            @Override
            public void onSpringActivate(Spring spring) {
            }

            @Override
            public void onSpringEndStateChange(Spring spring) {

            }
        });

        if(delay != 0) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    if(mPopOutSpring != null)
                        mPopOutSpring.setEndValue(1);
                }
            }, delay);
        }
        else{
            mPopOutSpring.setEndValue(1);
        }
    }

    Spring mPopInSpring;
    public void animateIn(int delay, final float finalValue, final Utils.AnimationFinishedListener listener){
        if(mPopOutSpring != null)
            mPopOutSpring.setAtRest();

        final CircleButton circleButton = (CircleButton) findViewById(R.id.rover_icon);
        if(finalValue == 1) {
            circleButton.setScaleX(0f);
            circleButton.setScaleY(0f);
        }
        else{
            circleButton.setScaleLevel(0f);
        }

        circleButton.setAlpha(0f);

        mPopInSpring = Utils.getSpringSystem().createSpring();
        mPopInSpring.setSpringConfig(new SpringConfig(150,8));
//        mPopInSpring.setOvershootClampingEnabled(true);
        mPopInSpring.setCurrentValue(0f);
//        mPopInSpring.setVelocity(2);
        mPopInSpring.addListener(new SpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float compareValue = circleButton.getScaleX();
                if(finalValue != 1)
                    compareValue = circleButton.getScaleLevel();

                float scaleValue = (float)Math.min(finalValue, spring.getCurrentValue()*1);
                scaleValue = Math.max(compareValue, scaleValue);

                float alphaValue = (float)Math.max(circleButton.getAlpha(), spring.getCurrentValue()*3);
                alphaValue = Math.min(1, alphaValue);

                if(finalValue == 1) {
                    circleButton.setScaleX(scaleValue);
                    circleButton.setScaleY(scaleValue);
                }
                else{
                    circleButton.setScaleLevel(scaleValue);
                }
                circleButton.setAlpha(alphaValue);

                if(alphaValue == 1 && scaleValue == finalValue)
                    spring.setAtRest();
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                mPopInSpring = null;
                spring.removeAllListeners();
            }

            @Override
            public void onSpringActivate(Spring spring) {
            }

            @Override
            public void onSpringEndStateChange(Spring spring) {
                if(listener != null)
                    listener.onAnimationFinished();
            }
        });

        if(delay != 0) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    if(mPopInSpring != null)
                        mPopInSpring.setEndValue(finalValue);
                }
            }, delay);
        }
        else{
            mPopInSpring.setEndValue(finalValue);
        }






    }



    //endregion Animation

    //region Touch Handler
    private TouchHandler mTouchHandler;

    private class TouchHandler extends GestureDetector.SimpleOnGestureListener implements OnTouchListener,
            SwipeDismissTouchListener.DismissCallbacks, DragAndDropHandler.DragAndDropCallbacks {
        private SwipeDismissTouchListener mSwipeListener;
        private DragAndDropHandler mDragHandler;
        private GestureDetector mGestureDetector;
        boolean mLongClicked = false;



        public TouchHandler(){
            mGestureDetector = new GestureDetector(getContext(),this);

        }
        public void setSwipeListener(SwipeDismissTouchListener l){

            mSwipeListener = l;
            mSwipeListener.setCallbacksListener(this);

        }

        public void setDragAndDropHandler(DragAndDropHandler l){
            mDragHandler = l;
            mDragHandler.setCallbacksListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            boolean result = false;

            mGestureDetector.onTouchEvent(e);

            if (mLongClicked && !mSwipeListener.isSwiping() && e.getAction() == MotionEvent.ACTION_UP) {
                //long press up detected
                if(mTouchActionsListener != null)
                    mTouchActionsListener.onLongPressUp(v,e);

            }

            if(mDragHandler == null || !mDragHandler.isDragging() && mSwipeListener!= null)
                mSwipeListener.onTouch(v,e);


            return false;


        }

        public boolean onDown(MotionEvent e) {
            mLongClicked = false;
            return true;
        }

        public void onLongPress(MotionEvent e) {
            if(canDragAndDrop()){
                showOverlay(RoverView.OverlayType.Drag);

                mDragHandler.startDragAndDrop();
            }
            else{
                mLongClicked = true;
                //long press down detected
                if(mTouchActionsListener != null)
                    mTouchActionsListener.onLongPressDown(RoverView.this,e);
            }

        }

        public boolean onSingleTapUp (MotionEvent e){
            if(!PrefUtils.getMiscMuteClickSoundValue(getContext())) {
                playSoundEffect(SoundEffectConstants.CLICK);
            }

            //single tap up detected
            if(mTouchActionsListener != null)
                mTouchActionsListener.onSingleTap(RoverView.this, e);
            return true;
        }

        //region Swipe To Dismiss Callbacks
        @Override
        public boolean canDismiss(Object token) {

            if(mTouchActionsListener != null)
                return mTouchActionsListener.canDismiss(token);

            return false;
        }

        @Override
        public void onSwipingStarted(View view) {
            //swiping started detected
            if(mTouchActionsListener != null)
                mTouchActionsListener.onSwipeStarted(view);
        }

        @Override
        public void onSwipingFinished(View view) {

            //swiping started detected
            if(mTouchActionsListener != null)
                mTouchActionsListener.onSwipeFinished(view);
        }

        @Override
        public void onDismiss(View view, Object token) {
            //swiping started detected
            if(mTouchActionsListener != null)
                mTouchActionsListener.onDismiss(view, token);
        }

        //endregion Swipe To Dismiss Callbacks

        //region Drag & Drop Callbacks

        @Override
        public boolean canDragAndDrop() {
            if(mTouchActionsListener != null)
                return mTouchActionsListener.canDragAndDrop();

            return false;
        }

        @Override
        public void onDragAndDropStarted(ViewGroup container, View view, int originalPosition) {
            if(mTouchActionsListener != null)
                mTouchActionsListener.onDragAndDropStarted(container,view,originalPosition);

        }

        @Override
        public void onDragAndDropFinished(ViewGroup container, View view, int originalPosition, int droppedPosition) {
            if(mTouchActionsListener != null)
                mTouchActionsListener.onDragAndDropFinished(container, view, originalPosition, droppedPosition);

        }

        //endregion Drag & Drop Callbacks
    }


    private OnTouchActionsListener mTouchActionsListener;
    public interface OnTouchActionsListener {
        void onSingleTap(View view, MotionEvent e);
        void onLongPressDown(View view, MotionEvent e);
        void onLongPressUp(View v, MotionEvent e);
        boolean canDismiss(Object token);
        void onSwipeStarted(View v);
        void onSwipeFinished(View v);
        void onDismiss(View v,Object token);
        void onDragAndDropStarted(ViewGroup container, View view, int originalPosition);
        void onDragAndDropFinished(ViewGroup container, View view, int originalPosition, int droppedPosition);
        boolean canDragAndDrop();
    }

    //endregion Touch Handler

    //region RoverView Builder

    public static class Builder {

        private Context mContext;
        private int mLayout = R.layout.rover_item_vertical;
        private RoverView mRoverView;

        public Builder(Context context) {
            mContext = context;
            create();
        }

        public Builder(Context context, int layout) {
            mContext = context;
            mLayout = layout;
            create();
        }

        private void create() {
            mRoverView = new RoverView(mContext, mLayout);
        }

        public RoverView build() {
            return mRoverView;
        }



        //endregion Text

        public Builder setTrigger(boolean isTrigger) {
            mRoverView.setIsTrigger(isTrigger);
            return this;
        }

        public Builder setSize(int size, boolean isAdjustable){
            mRoverView.setIsSizeAdjustableToTrigger(isAdjustable);
            mRoverView.refreshRoverSize(size);
            return this;
        }

        public Builder setSizeAdjustableToTrigger(boolean isAdjustable){
            mRoverView.setIsSizeAdjustableToTrigger(isAdjustable);
            return this;
        }


        //region Icon



        public Builder setIcon(Bitmap icon) {
            mRoverView.setIconBitmap(icon);

            return this;
        }

        public Builder setIcon(int icon) {
            mRoverView.setIconResource(icon);

            return this;
        }

        public Builder setIcon(Drawable icon) {
            mRoverView.setIconDrawable(icon);

            return this;
        }

        //endregion Icon

        //region Background

        public Builder setBackground(int color) {
            mRoverView.setCircleColor(color);

            return this;
        }

        //endregion Background

        //region Rover

        public Builder setRover(IRover rover) {
            mRoverView.setRoverValues(rover);
            return this;
        }

        //endregion Rover

        //region Listeners

        public Builder setOnTouchActionsListener(OnTouchActionsListener l){
            mRoverView.setTouchActionsListener(l);

            return this;
        }

        public Builder enableTouchHandler(boolean isEnabled){
            mRoverView.setTouchHandlerEnabled(isEnabled);

            return this;
        }

        //endregion Listeners

        //region PlaceHolder

        public Builder setPlaceholder(int size, Utils.Direction direction, OnTouchListener touchListener) {
            mRoverView.setPlaceHolder(size, direction, touchListener);

            return this;
        }

        //endregion

    }


    //endregion


    /**
     * Should be called after no longer visible
     * cleans all listeners & callbacks from the rover so it can be destroyed.
     */
    public void clearRoverView(){
        mTouchHandler = null;
        View icon = getCircleView();
        if(icon != null) {
            icon.setOnTouchListener(null);
            icon.setOnClickListener(null);
        }

        Utils.unbindDrawables(this);
    }

    //region Bitmap Loader
    class BitmapIconLoaderTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<RoverView> mRoverViewWeakReference;
        private IRover mRover;
        private Context mContext;
        private Bitmap mToRecycle = null;
        int mInnerWidth;

        public BitmapIconLoaderTask(RoverView roverView, IRover rover, Context context, int innerWidth) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            mRoverViewWeakReference = new WeakReference<RoverView>(roverView);
            mRover = rover;
            mContext = context;
            mInnerWidth = innerWidth;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {

            if(mRover!= null){
                Drawable drawable = mRover.getIcon(mContext);
                if(drawable instanceof BitmapDrawable){
                    Bitmap icon = ((BitmapDrawable) drawable).getBitmap();
                    return BitmapUtils.getCroppedBitmap(icon, mInnerWidth);
                }
                else {
                    mToRecycle = BitmapUtils.drawableToBitmap(drawable);
                    return BitmapUtils.getCroppedBitmap(mToRecycle, mInnerWidth);
                }
            }
            return null;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (mRoverViewWeakReference != null && bitmap != null) {
                //cache icon for next uses
                if(mRover.isIconCachable()) {
                    CacheUtils.addToBitmapCache(mRover.getDistinctID(), bitmap);
                }

                final RoverView roverView = mRoverViewWeakReference.get();
                if (roverView != null) {
                    roverView.setIconBitmap(bitmap);
                    int color = mRover.getColor(getContext());
                    CacheUtils.addToColorCache(mRover.getDistinctID(), color);
                    roverView.setCircleColor(color);
                    if(mToRecycle != null) {
                        mToRecycle.recycle();
                    }
                }
            }

            //cleanup
            mToRecycle = null;
            mRover = null;
            mContext = null;
        }
    }

    class RoverIconLoaderTask extends AsyncTask<Integer, Void, Drawable>{
        private final WeakReference<RoverView> mRoverViewWeakReference;
        private IRover mRover;
        private Context mContext;


        public RoverIconLoaderTask(RoverView roverView, IRover rover, Context context) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            mRoverViewWeakReference = new WeakReference<RoverView>(roverView);
            mRover = rover;
            mContext = context;
        }
        // Decode image in background.
        @Override
        protected Drawable doInBackground(Integer... params) {

            if(mRover != null)
                return mRover.getIcon(getContext());

            return null;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Drawable drawable) {
            if (mRoverViewWeakReference != null && drawable != null) {
                //cache icon for next uses
                if(mRover.isIconCachable()) {
                    CacheUtils.addToDrawableCache(mRover.getDistinctID(), drawable);
                }

                final RoverView roverView = mRoverViewWeakReference.get();
                if (roverView != null) {
                    int color = mRover.getColor(mContext);
                    CacheUtils.addToColorCache(mRover.getDistinctID(), color);

                    setCircleColor(color);
                    setIconDrawable(drawable);
                }
            }

            //cleanup
            mRover = null;
            mContext = null;
        }

    }



    //endregion
}