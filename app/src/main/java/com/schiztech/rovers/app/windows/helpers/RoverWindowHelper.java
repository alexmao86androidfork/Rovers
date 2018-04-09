package com.schiztech.rovers.app.windows.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.VelocityTrackerCompat;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsListener;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.roverhosts.HostManagerBase;
import com.schiztech.rovers.app.roverhosts.HostManagerHorizontal;
import com.schiztech.rovers.app.roverhosts.HostManagerVertical;
import com.schiztech.rovers.app.roveritems.IRover;
import com.schiztech.rovers.app.roveritems.RoversManager;
import com.schiztech.rovers.app.ui.CircleButton;
import com.schiztech.rovers.app.ui.RoverView;
import com.schiztech.rovers.app.utils.BitmapUtils;
import com.schiztech.rovers.app.utils.CacheUtils;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.PrefUtils;
import com.schiztech.rovers.app.utils.RoversUtils;
import com.schiztech.rovers.app.utils.Utils;
import com.schiztech.rovers.app.windows.FloatingWindowsManager;


import io.fabric.sdk.android.services.common.Crash;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

/**
 * Created by schiz_000 on 5/23/2014.
 */
public class RoverWindowHelper extends WindowHelperBase {
    private static final String TAG = LogUtils.makeLogTag("RoverWindowHelper");

    private static final int EXPAND_RETURN_CODE = 11;
    private static final int COLLAPSE_RETURN_CODE = 12;

    private boolean mIsFullyShown = false;

    private int mRoverSize;
    private int mTriggerSize;
    //private int mDefaultColor = 0xffdddddd;
    private int mDefaultIconResource = R.drawable.ic_logo;
    private int mCurrentIconResource;

    protected RoverWindowHelper(Context context) {

        super(context);

        loadCurrentLocation();
        loadRestAlpha();
        loadRestOffset();

        mRoverSize = RoversUtils.getRoverSize(context);
        mTriggerSize = RoversUtils.getTriggerSize(context);
        //pre-init rovers manager for smoother first time load (initialization)
        RoversManager.getInstance(context).init();
    }

    @Override
    public int getID() {
        return WINDOW_ID_ROVER;
    }

    //region View & Decor Methods
    RoverView mRoverView;

    @Override
    public View getView() {

        mRoverView = new RoverView.Builder(getContext())
                .setBackground(getBackgroundColor())
                .setTrigger(true)
                .build();

        mCurrentIconResource = mDefaultIconResource;
        mRoverView.setButtonClickable(false);//otherwise the window is not move-able...
        applyRoverRestAlpha();
        applyDefaultTriggerIcon();
        return mRoverView;
    }

    @Override
    public StandOutWindow.StandOutLayoutParams getParams(FloatingWindowsManager windowsManager) {
        Point screenDims = Utils.getDisplayDimensions(getContext());
        mLocationX =(int)(mCurrentLocation.x * screenDims.x);
        mLocationY = (int)(mCurrentLocation.y * screenDims.y);
        StandOutWindow.StandOutLayoutParams params = windowsManager.getParamsInstance(getID(),
                StandOutWindow.StandOutLayoutParams.WRAP_CONTENT,
                StandOutWindow.StandOutLayoutParams.WRAP_CONTENT,
                mLocationX,
                mLocationY);


        return params;
    }

    @Override
    public int getFlags() {
        return  StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE |
                StandOutFlags.FLAG_WINDOW_FOCUS_INDICATOR_DISABLE |
                StandOutFlags.FLAG_WINDOW_HIDE_ENABLE;
    }

    //endregion View & Decor Methods

    //region Touch Methods

    @Override
    public boolean onTouchBody(View view, MotionEvent event) {
        if(mIsFullyShown != true)
            return false;

        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mRoverView != null && mIsExpanded != true) {
                mRoverView.setButtonPressed(false);
            }
        }
        else {
            if (mRoverView != null) {
                mRoverView.setButtonPressed(true);
            }
        }

        if (mGestureDetector == null) {
            mGestureDetector = new GestureDetector(getContext(), new GestureListener());
        }

        return mGestureDetector.onTouchEvent(event);
    }

    private GestureDetector mGestureDetector;

    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;


        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if(!PrefUtils.getMiscMuteClickSoundValue(getContext())) {
                mRoverView.playSoundEffect(SoundEffectConstants.CLICK);
            }
            LogUtils.LOGV(TAG, "Tap up!");
            if (mIsEditMode) {
                requestEditModeCancel();

            } else if (mIsExpanded) {
                requestHostBackAction();
            } else {
                expandRoverHost();
            }

            return true;
        }

        public void onLongPress(MotionEvent e) {
            //cancel event if on move
            if (mIsMoving == true) return;

            //vibrate to make user notice of gesture
            Utils.Vibrate(getContext(), Utils.VIBRATE_MINIMAL);

            //collapse host if expanded
            if (mIsExpanded)
                collapseRoverHost();

        }

//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            boolean result = false;
//            try {
//                float diffY = e2.getY() - e1.getY();
//                float diffX = e2.getX() - e1.getX();
//                if (Math.abs(diffX) > Math.abs(diffY)) {
//                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
//                        if (diffX > 0) {
//                            onSwipeRight();
//                        } else {
//                            onSwipeLeft();
//                        }
//                    }
//                } else {
//                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
//                        if (diffY > 0) {
//                            onSwipeBottom();
//                        } else {
//                            onSwipeTop();
//                        }
//                    }
//                }
//            } catch (Exception exception) {
//                exception.printStackTrace();
//            }
//            return result;
//        }
//
//
//        public void onSwipeRight() {
//        }
//
//        public void onSwipeLeft() {
//        }
//
//        public void onSwipeTop() {
//        }
//
//        public void onSwipeBottom() {
//        }


}

    //endregion Touch Methods

    //region Movement Methods
    boolean mIsMoving = false;
    boolean mIsInHideArea = false;

    int mLastX;
    int mLastY;
    int mDeltaTotalX =0;
    int mDeltaTotalY =0;
    int mLocationX;
    int mLocationY;
    private float mLastXVelocity = 0;
    private float mLastYVelocity =0;
    private VelocityTracker mVelocityTracker = null;

    @Override
    public void onMove(Window window, View view, MotionEvent event) {
        if(mIsFullyShown != true)
            return;

        //cancel move if during animation of collapse or expand
        if( !mIsMoving && ( mIsDuringCollapse || mIsDuringExpand))
            return;

        StandOutWindow.StandOutLayoutParams params = window.getLayoutParams();
        event.offsetLocation(mDeltaTotalX, mDeltaTotalY);

        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int)event.getRawX();
                mLastY = (int)event.getRawY();
                mLocationX = params.x + (int)window.getChildAt(0).getX();
                mLocationY = params.y + (int)window.getChildAt(0).getY();
                mDeltaTotalX = 0;
                mDeltaTotalY = 0;
                if(mVelocityTracker == null) {
                    // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                    mVelocityTracker = VelocityTracker.obtain();
                }
                else {
                    // Reset the velocity tracker back to its initial state.
                    mVelocityTracker.clear();
                }
                // Add a user's movement to the tracker.
                mVelocityTracker.addMovement(event);

                break;
            case MotionEvent.ACTION_MOVE:


                mVelocityTracker.addMovement(event);
                // When you want to determine the velocity, call
                // computeCurrentVelocity(). Then call getXVelocity()
                // and getYVelocity() to retrieve the velocity for each pointer ID.

                mVelocityTracker.computeCurrentVelocity(1000);
                // Log velocity of pixels per second
                // Best practice to use VelocityTrackerCompat where possible.
                mLastXVelocity = VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId);
                mLastYVelocity =  VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId);

                int deltaX = (int)event.getRawX() - mLastX;
                int deltaY = (int)event.getRawY() - mLastY;
                mDeltaTotalX += deltaX;
                mDeltaTotalY += deltaY;

                mLastX = (int)event.getRawX();
                mLastY = (int)event.getRawY();
                mLocationX += deltaX;
                mLocationY += deltaY;

                //when expanded - more significant moves required
                if(!mIsExpanded || Math.abs(mDeltaTotalX) > 25 && Math.abs(mDeltaTotalY) > 25) {
                    animateWindow(new Point(mLocationX, mLocationY), Utils.MoveType.Instant);

                    if (mIsMoving == false) {
                        onMoveStarted();
                        mIsMoving = true;
                    }

                    Bundle data = new Bundle();
                    data.putInt(DATA_KEY_IS_POINT_IN_WINDOW_X, window.getLayoutParams().x + window.getWidth() / 2);
                    data.putInt(DATA_KEY_IS_POINT_IN_WINDOW_Y, window.getLayoutParams().y + window.getHeight() / 2);
                    sendData(WINDOW_ID_ROVER_HIDE, DATA_REQ_IS_POINT_IN_WINDOW_REQUEST, data);

                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mRoverView.setButtonPressed(false);
                // Return a VelocityTracker object back to be re-used by others.
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDeltaTotalX = 0;
                mDeltaTotalY = 0;
                //moving finished - action_up while moving flag is ON
                if (mIsMoving) {
                    onMoveFinished();
                    mIsMoving = false;
                }
                break;
        }


    }

    private void onMoveStarted() {
        mIsMoving = true;

        if (mIsExpanded) {
            collapseRoverHost();
        }
        sendData(FloatingWindowsManager.DISREGARD_ID,
                FloatingWindowsManager.DATA_REQ_ROVER_MOVE_STARTED,
                null);

    }

    private void onMoveFinished() {
        //announce finished moving
        if (mIsMoving) {
            sendData(FloatingWindowsManager.DISREGARD_ID,
                    FloatingWindowsManager.DATA_REQ_ROVER_MOVE_FINISHED,
                    null);
        }

        //check if need to hide the rover
        if (mIsInHideArea == true) {
            mIsInHideArea = false;
            //show hidden alert if not shown before
            if(PrefUtils.getHiddenAlertIsShownValue(getContext()) == false) {
                Bundle data = new Bundle();
                data.putInt(FloatingWindowsManager.DATA_KEY_WINDOW_ID, WindowHelperBase.WINDOW_ID_HIDDEN_ALERT);
                sendData(FloatingWindowsManager.DISREGARD_ID, FloatingWindowsManager.DATA_REQ_SHOW_ANOTHER, data);
            }
            sendData(FloatingWindowsManager.DISREGARD_ID,
                    FloatingWindowsManager.DATA_REQ_HIDE,
                    null);

        }

        else {//moved without closing the window
                moveToEndPoint();
        }
    }


    private float mRestOffset;
    private void loadRestOffset() {
        mRestOffset = PrefUtils.getTriggerRestOffsetValue(getContext());
    }

    private void updateRestOffset(){
        loadRestOffset();
        moveToEndPoint();
    }

    private Point getAdjustedEndLocation(Point originalLocation){
       int roverWidth = mRoverView.getWidth();
       int roverHeight = mRoverView.getHeight();
        Point screenDims = Utils.getDisplayDimensions(getContext());

        Point result = new Point(0,0);
        result.x = Math.min(originalLocation.x, screenDims.x - roverWidth);
        result.x += (int)((originalLocation.x == 0) ? -mRestOffset * roverWidth : +mRestOffset * roverWidth);
        result.y = Math.min(originalLocation.y, screenDims.y - roverHeight);
        return result;
    }

    private Point getAdjustedCornerLocation(Point originalLocation) {
        Point adjustedLocation = originalLocation;

        if (mRoverView != null) {
            if (originalLocation.y > mRoverView.getHeight()) {
                adjustedLocation.y -= mRoverView.getHeight();
            }

            if (originalLocation.x > mRoverView.getWidth()) {
                adjustedLocation.x -= mRoverView.getWidth();
            }

        }


        return adjustedLocation;
    }


    //region Hide Area
    private void enterRoverInHideArea() {
        if (mIsInHideArea == true)
            return;//already in the hide area.

        mIsInHideArea = true;
        Utils.Vibrate(getContext(), Utils.VIBRATE_MINIMAL);
        if (mRoverView != null)
            mRoverView.setCircleColor(Color.RED);


    }

    private void exitRoverFromHideArea() {
        if (mIsInHideArea == false)
            return;//already out of hide area.

        mIsInHideArea = false;
        Utils.Vibrate(getContext(), Utils.VIBRATE_MINIMAL);
        if (mRoverView != null)
            mRoverView.setCircleColor(getBackgroundColor());
    }

    //endregion Hide Area

    //endregion Movement Methods

    //region Window State Methods

    @Override
    public void onWindowShown() {
        loadCurrentLocation();
        moveToEndPoint(true);
        SharedPreferences prefs =  PrefUtils.getMainSharedPreferences(getContext());
        if(prefs != null){
            updateRestAlpha();
            updateRestOffset();
            updateBackgroundColor();
            updateIconColor();
        }

        mIsFullyShown = true;
    }

    @Override
    public void onWindowHidden(){

        mRoverView.setCircleColor(getBackgroundColor());
        mIsFullyShown = false;
    }

    @Override
    public boolean onClose() {
        return super.onClose();
    }

    @Override
    public boolean onHide() {
        collapseRoverHost();

        return super.onHide();
    }

    //endregion Window State Methods

    //region Data Communication
    public final static int DATA_REQ_REQUEST_COLLAPSE = 4002;

    @Override
    public void onReceiveData(Window window, int requestCode, Bundle data, int fromId) {
        super.onReceiveData(window, requestCode, data, fromId);
        switch (requestCode) {
            case DATA_REQ_IS_POINT_IN_WINDOW_RESPOND:
                handleIsPointInWindowRespond(data, fromId);
                break;

            case HostWindowHelper.DATA_REQ_EDIT_MODE_CANCELED:
                onEditModeFinished();
                break;
            case HostWindowHelper.DATA_REQ_EDIT_MODE_STARTED:
                onEditModeStarted();
                break;
            case HostWindowHelper.DATA_REQ_ANNOUNCE_SHOWN:
                if (fromId == WINDOW_ID_HOST) {
                    onRoverHostExpanded();
                }
                break;
            case HostWindowHelper.DATA_REQ_ANNOUNCE_CLOSED:
                if (fromId == WINDOW_ID_HOST) {
                    onRoverHostCollapsed();
                }
                break;

            case DATA_REQ_REQUEST_COLLAPSE:
                collapseRoverHost();
                break;

            case DATA_REQ_UPDATE_VISUALS:
                if(fromId == WINDOW_ID_HOST){
                    onUpdateVisuals(data);
                }
                break;

            case DATA_REQ_ADD_ROVER:
                onAddRover(data);
            break;
        }
    }

    private void handleIsPointInWindowRespond(Bundle data, int fromId) {
        if (fromId != WINDOW_ID_ROVER_HIDE || mIsMoving == false)
            return; // cares only if message is from the HIDE window & rover still moving

        boolean result = data.getBoolean(DATA_KEY_IS_POINT_IN_WINDOW_RESULT);
        if (result) {
            enterRoverInHideArea();
        } else {
            exitRoverFromHideArea();
        }
    }


    @Override
    protected boolean isPointInWindow(Window window, Point point, int fromId) {
        if(fromId != WINDOW_ID_HOST || window == null){
            return super.isPointInWindow(window,point,fromId);
        }

        //pad a little so more touched will be counted as IN
        final float PADDING_RATIO = 0.25f;
        StandOutWindow.StandOutLayoutParams hideParams = window.getLayoutParams();
        int xAreaRight = hideParams.x + window.getWidth() + (int)(PADDING_RATIO*window.getWidth());
        int xAreaLeft = hideParams.x - (int)(PADDING_RATIO*window.getWidth());
        int yAreaTop = hideParams.y - (int)(PADDING_RATIO*window.getHeight());
        int yAreaBottom = hideParams.y + window.getHeight() + (int)(PADDING_RATIO*window.getHeight());


        if (point.x <= xAreaRight && point.x >= xAreaLeft
                && point.y <= yAreaBottom && point.y >= yAreaTop) {
            return true;
        }

        return false;
    }

    //endregion Data Communication

    //region Location Logic

    PointF mCurrentLocation;

    /**
     * Regular call to move to end point, just animates the flow to the end point
     */
    private void moveToEndPoint(){
        moveToEndPoint(false);
    }

    /**
     * animate rover trigger to the end point with a given speed.
     * @param isForcedMovement either the movement if forced even if location is same
     */
    private void moveToEndPoint(boolean isForcedMovement) {
        float maxFlingVelocity    = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
        Point destination = Utils.computeEndPoint(getContext(), mRoverView.getHeight(), mLocationX + mRoverView.getWidth()/2, mLocationY+ mRoverView.getHeight()/2, mLastXVelocity / maxFlingVelocity, mLastYVelocity / maxFlingVelocity);
        destination = getAdjustedEndLocation(destination);
        //animate only if instant move or destination different from current location
        if(!mIsDuringExpand && (isForcedMovement == true || mLocationX != destination.x || mLocationY != destination.y)) {
            PointF velocity = new PointF(mLastXVelocity, mLastYVelocity);
            saveCurrentLocation(destination);
            animateWindow(destination, velocity,Utils.MoveType.Regular, FloatingWindowsManager.DEFAULT_WINDOW_MOVE_RETURN_CODE);
        }
    }

    private Point getCurrentLocation(){
        Point screenDims = Utils.getDisplayDimensions(getContext());

        Point destination = new Point((int)(screenDims.x * mCurrentLocation.x) , (int)(screenDims.y * mCurrentLocation.y ));
        return destination;
    }

    @Override
    protected void onAnimateWindowFinished(int returnCode){

        if(returnCode == EXPAND_RETURN_CODE){
            new HostPreloaderTask(getContext()).execute();
        }
    }

    private class HostPreloaderTask extends AsyncTask<Void, Void, Bundle> {
        Context mContext;
        public HostPreloaderTask(Context context) {
            mContext = context;
        }


        @Override
        protected Bundle doInBackground(Void... params) {
            try {
                int layout;

                if (PrefUtils.isHostOrientationLandscape(getContext())) {
                    layout = HostManagerHorizontal.getStaticHostLayoutResource();
                } else {
                    layout = HostManagerVertical.getStaticHostLayoutResource();
                }

                HostManagerBase.preloadLayoutView(mContext, layout);
            }

            catch (Exception e){
                LogUtils.LOGE(TAG, "Error HostPreloaderTask (doinbackground): " + e.getMessage());
            }

            Bundle data = new Bundle();
            data.putInt(FloatingWindowsManager.DATA_KEY_WINDOW_ID, WindowHelperBase.WINDOW_ID_HOST);

            return data;
        }


        @Override
        protected void onPostExecute(Bundle data) {
            sendData(FloatingWindowsManager.DISREGARD_ID, FloatingWindowsManager.DATA_REQ_SHOW_ANOTHER, data);
            mContext = null;// cleanup
        }
    }

    private void saveCurrentLocation(Point currentLocation) {
        Point screenDims = Utils.getDisplayDimensions(getContext());
        float x = (float)currentLocation.x / (float)screenDims.x;
        float y = (float)currentLocation.y / (float)screenDims.y;

        mCurrentLocation = new PointF(x,y);

        PrefUtils.setRoverWindowXValue(getContext(),x);
        PrefUtils.setRoverWindowYValue(getContext(), y);



    }

    private void loadCurrentLocation() {
        mCurrentLocation = getSavedCurrentLocation();
    }

    //endregion Location Logic

    //region Shared Preferences


    private PointF getSavedCurrentLocation() {
        float x = PrefUtils.getRoverWindowXValue(getContext());
        float y = PrefUtils.getRoverWindowYValue(getContext());
        return new PointF(x,y);
    }


    //region Preferences Changed

    @Override
    public void onPreferencesChanged(String action) {
        super.onPreferencesChanged(action);
        if (action == null) return;

        switch (action){
            case PrefUtils.ACTION_TRIGGER_BACKGROUND_COLOR_CHANGED:
                updateBackgroundColor();
                break;
            case PrefUtils.ACTION_TRIGGER_ICON_COLOR_CHANGED:
                updateIconColor();
                break;
            case PrefUtils.ACTION_TRIGGER_REST_ALPHA_CHANGED:
                updateRestAlpha();
                break;
            case PrefUtils.ACTION_TRIGGER_REST_OFFSET_CHANGED:
                updateRestOffset();
                break;
            case PrefUtils.ACTION_ITEMS_DEFAULT_COLOR:
                CacheUtils.clearDrawableCache();
                break;
        }

    }

    //endregion Preferences Changed

    //region Expand Host Methods

    private boolean mIsExpanded = false;

    private void expandRoverHost() {
        LogUtils.LOGD(TAG, "Rovers Trigger started expand");

        if( mIsDuringExpand == false && mIsExpanded == false) {
            mIsDuringExpand = true;
            getContext().sendBroadcast(new Intent("com.schiztech.rovers.roverhost.expanded"));//let receivers know that host will show soon
            cancelRoverRestAlpha();
            requestDim();
//        Point screenDims = Utils.getDisplayDimensions(getContext());
            Point currentLocation = getCurrentLocation();
            Utils.Corner corner = Utils.getExpandCorner(getContext(), new Point(currentLocation.x + (int) (mRoverView.getWidth() / 2), currentLocation.y + (int) (mRoverView.getHeight() / 2)));
            Point cornerLocation = Utils.getCornerLocation(getContext(), corner);
            cornerLocation = getAdjustedCornerLocation(cornerLocation);
            cornerLocation = getExpandAdjustedCornerPadding(corner, cornerLocation);
            animateWindow(cornerLocation, Utils.MoveType.Sudden, EXPAND_RETURN_CODE);

        }
    }

    private Point getExpandAdjustedCornerPadding(Utils.Corner corner, Point cornerLocation){
        //only required if trigger size is smaller than item size.
        if(mTriggerSize >= mRoverSize)
            return cornerLocation;

        int paddingSize = (mRoverSize - mTriggerSize) / 2;
        boolean isHostLandscape = PrefUtils.isHostOrientationLandscape(getContext());
        if(isHostLandscape){
            if(corner == Utils.Corner.LeftTop || corner == Utils.Corner.RightTop){
                cornerLocation.y += paddingSize;
            }
            else{//if corner is bottom side
                cornerLocation.y -= paddingSize;
            }
        }
        else{//if host is vertical
            if(corner == Utils.Corner.LeftBottom || corner == Utils.Corner.LeftTop){
                cornerLocation.x += paddingSize;
            }
            else{//if corner is right side
                cornerLocation.x -= paddingSize;
            }

        }

        return cornerLocation;
    }
    private boolean mIsDuringCollapse = false;
    private boolean mIsDuringExpand = false;
    private void collapseRoverHost() {
        if(mIsDuringCollapse == false && mIsExpanded == true) {
            mIsDuringCollapse = true;
            requestUndim();
            //cancel edit mode if ON
            if (mIsEditMode) {
                sendData(WindowHelperBase.WINDOW_ID_HOST, HostWindowHelper.DATA_REQ_EDIT_MODE_CANCEL_REQUEST, null);
            }

            //close host
            Bundle data = new Bundle();
            data.putInt(FloatingWindowsManager.DATA_KEY_WINDOW_ID, WindowHelperBase.WINDOW_ID_HOST);
            sendData(WINDOW_ID_HOST, DATA_REQ_REQUEST_CLOSE, null);
            applyRoverRestAlpha();

            if (mIsMoving == false) {
                Point dest = getCurrentLocation();
                animateWindow(dest, Utils.MoveType.Sudden, COLLAPSE_RETURN_CODE);
            }
        }
    }


    private void onRoverHostExpanded() {
        LogUtils.LOGV(TAG, "Rovers Trigger Expanded");
        mIsExpanded = true;
        mIsDuringExpand = false;
        removeWindowFlags(StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE);
        requestFocus();
    }

    private void onRoverHostCollapsed() {
        mIsExpanded = false;
        mIsDuringCollapse = false;
        requestUnfocus();

        addWindowFlags(StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE);


    }

    private void requestHostBackAction(){
        sendData(WindowHelperBase.WINDOW_ID_HOST, HostWindowHelper.DATA_REQ_BACK_EVENT_TRIGGERED, null);
    }
    //endregion

    //region Host Edit Mode

    protected boolean mIsEditMode = false;

    protected void onEditModeStarted() {
        mIsEditMode = true;


        mRoverView.showOverlay(RoverView.OverlayType.Edit);

    }

    protected void onEditModeFinished() {
        mIsEditMode = false;


        mRoverView.hideOverlay();
    }

    private void requestEditModeCancel(){
        sendData(WindowHelperBase.WINDOW_ID_HOST, HostWindowHelper.DATA_REQ_EDIT_MODE_CANCEL_REQUEST, null);
    }

    //endregion Host Edit Mode

    //region Update Rover Visuals

    public static final int DATA_REQ_UPDATE_VISUALS = 4001;
    public static final String DATA_KEY_VISUAL_ICON_RESOURCE = "icon_resource";
    public static final String DATA_KEY_VISUAL_BACKGROUND_COLOR = "background_color";

    private void onUpdateVisuals(Bundle data){
        int icon = mDefaultIconResource;
        int color = getBackgroundColor();
        if(data != null){
            icon = data.getInt(DATA_KEY_VISUAL_ICON_RESOURCE, icon);
            color = data.getInt(DATA_KEY_VISUAL_BACKGROUND_COLOR, color);
        }

        Drawable old = mRoverView.getDrawable();
        if(icon != mDefaultIconResource){
            Drawable d = BitmapUtils.recolorDrawableByBackground(getContext(), icon, color);
            mRoverView.setIconDrawable(d);
        }
        else{
            applyDefaultTriggerIcon();
        }

        if(mCurrentIconResource != mDefaultIconResource && old != null && old instanceof BitmapDrawable){
            ((BitmapDrawable)old).getBitmap().recycle();
        }

        mCurrentIconResource = icon;


        mRoverView.setCircleColor(color);

    }

    //endregion Update Rover Visuals

    //region KeyEvents

    public boolean onKeyEvent(KeyEvent event) {

        if (mIsExpanded && event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    if (mIsEditMode) {
                        requestEditModeCancel();

                    } else if (mIsExpanded) {
                        requestHostBackAction();
                    }
                    return true;
            }
        }

        return false;
    }

    //endregion KeyEvents

    //region Rest Alpha

    float mRestAlpha;
    private void loadRestAlpha() {
        mRestAlpha = PrefUtils.getTriggerRestAlphaValue(getContext());
    }

    private void updateRestAlpha(){
        loadRestAlpha();
        applyRoverRestAlpha();
    }

    private void applyRoverRestAlpha(){
        if(mRoverView != null){
            mRoverView.animate().alpha(mRestAlpha).setDuration(getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime));
        }
    }

    private void cancelRoverRestAlpha(){
        if(mRoverView != null){
            mRoverView.animate().alpha(1).setDuration(getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime));
        }
    }

    //endregion Rest Alpha

    //region Orientation Methods

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //collapse host if expanded
        if (mIsExpanded)
            collapseRoverHost();


        loadCurrentLocation();
        moveToEndPoint(true);

    }

    //endregion Orientation Methods

    //region Add Rover

    public static final int DATA_REQ_ADD_ROVER = 4003;
    public static final String DATA_KEY_ADD_ROVER_TYPE = "rover_type";
    public static final String DATA_KEY_ADD_ROVER_DATA = "rover_data";

    private void onAddRover(Bundle data){
        try {
            if (data != null && data.containsKey(DATA_KEY_ADD_ROVER_TYPE) && data.containsKey(DATA_KEY_ADD_ROVER_DATA)) {
                Class roverType = (Class) data.getSerializable(DATA_KEY_ADD_ROVER_TYPE);

                String roverData = data.getString(DATA_KEY_ADD_ROVER_DATA);
                IRover newRover = RoversManager.gsonToRover(roverData, roverType);
                if (newRover != null) {
                    RoversManager.getInstance(getContext()).addChildToCurrentFolder(newRover);
                }
            }
        }
        catch (Exception e){
            LogUtils.LOGE(TAG, "Error while adding new Rover: " + e.getMessage());
        }
    }


    //endregion Add Rover

    private int getBackgroundColor(){
        return PrefUtils.getTriggerBackgroundValue(getContext());
    }

    private void updateBackgroundColor(){
        if(mRoverView != null){
            mRoverView.setCircleColor(getBackgroundColor());
        }
    }

    private int getIconColor(){
        return PrefUtils.getTriggerIconValue(getContext());
    }

    private void updateIconColor(){
        applyDefaultTriggerIcon();
    }

    private void applyDefaultTriggerIcon(){
        if(mRoverView != null) {
            Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), mDefaultIconResource);
            Bitmap croppedBitmap = BitmapUtils.getCroppedBitmap(bitmap, ((CircleButton) mRoverView.getCircleView()).getInnerWidth());
            Bitmap recolored = BitmapUtils.recolorBitmap(new BitmapDrawable(getContext().getResources(), croppedBitmap), getIconColor());

            mRoverView.setIconBitmap(recolored);

            croppedBitmap.recycle();
        }
    }




}
