package com.schiztech.rovers.app.windows.helpers;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.Utils;
import com.schiztech.rovers.app.windows.FloatingWindowsManager;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.ui.Window;

/**
 * Created by schiz_000 on 5/23/2014.
 */
public abstract class WindowHelperBase {
    private static final String TAG = LogUtils.makeLogTag("WindowHelperBase");
    private Context context;

    protected WindowHelperBase(Context context) {
        this.context = context;
    }

    public abstract int getID();

    public Context getContext() {
        return context;
    }

    //region StandOutWindow Methods

    //region View & Decor Methods
    public abstract View getView();

    public abstract StandOutWindow.StandOutLayoutParams getParams(FloatingWindowsManager windowsManager);

    public abstract int getFlags();
    //endregion View & Decor Methods

    //region Touch & Movement Methods


    public boolean onTouchBody(View view, MotionEvent event) {
        return false;
    }

    public void onMove(Window window, View view, MotionEvent event) {

    }

    protected void animateWindow(Point destination) {
        animateWindow(destination, new PointF(0, 0), Utils.MoveType.Regular, FloatingWindowsManager.DEFAULT_WINDOW_MOVE_RETURN_CODE);
    }

    protected void animateWindow(Point destination, int returnCode) {
        animateWindow(destination, new PointF(0, 0), Utils.MoveType.Regular, returnCode);
    }

    protected void animateWindow(Point destination, Utils.MoveType moveType) {
        animateWindow(destination, new PointF(0, 0), moveType, FloatingWindowsManager.DEFAULT_WINDOW_MOVE_RETURN_CODE);
    }

    protected void animateWindow(Point destination, PointF velocity, Utils.MoveType moveType) {
        animateWindow(destination, velocity, moveType, FloatingWindowsManager.DEFAULT_WINDOW_MOVE_RETURN_CODE);
    }

    protected void animateWindow(Point destination, PointF velocity, int returnCode) {
        animateWindow(destination, velocity, Utils.MoveType.Regular, returnCode);
    }

    protected void animateWindow(Point destination, Utils.MoveType moveType, int returnCode) {
        animateWindow(destination, new PointF(0, 0), moveType, returnCode);
    }

    protected void animateWindow(Point destination, PointF velocity, Utils.MoveType moveType, int returnCode) {
        Bundle data = new Bundle();

        data.putInt(FloatingWindowsManager.DATA_KEY_WINDOW_MOVE_X, destination.x);
        data.putInt(FloatingWindowsManager.DATA_KEY_WINDOW_MOVE_Y, destination.y);
        data.putFloat(FloatingWindowsManager.DATA_KEY_WINDOW_MOVE_VELOCITY_X, velocity.x);
        data.putFloat(FloatingWindowsManager.DATA_KEY_WINDOW_MOVE_VELOCITY_Y, velocity.y);
        data.putInt(FloatingWindowsManager.DATA_KEY_WINDOW_MOVE_TYPE, moveType.ordinal());

        if (returnCode != FloatingWindowsManager.DEFAULT_WINDOW_MOVE_RETURN_CODE)
            data.putInt(FloatingWindowsManager.DATA_KEY_WINDOW_MOVE_RETURN_CODE, returnCode);


        sendData(FloatingWindowsManager.DISREGARD_ID,
                FloatingWindowsManager.DATA_REQ_WINDOW_MOVE_REQUEST,
                data);
    }


    //endregion Touch & Movement Methods

    //region Window State Methods

    public void onWindowShown() {
    }

    public void onWindowClosed() {
    }

    public void onWindowHidden() {
    }

    public boolean onShow() {
        return false;
    }

    public boolean onHide() {
        return false;
    }

    public boolean onClose() {
        return false;
    }


    //endregion Window State Methods

    //endregion StandOutWindow Methods

    //region Data Communication

    public final static String DATA_KEY_IS_POINT_IN_WINDOW_X = "is_point_in_window_x";
    public final static String DATA_KEY_IS_POINT_IN_WINDOW_Y = "is_point_in_window_y";
    public final static String DATA_KEY_IS_POINT_IN_WINDOW_RESULT = "is_point_in_window_result";
    public final static int DATA_REQ_IS_POINT_IN_WINDOW_REQUEST = 2001;
    public final static int DATA_REQ_IS_POINT_IN_WINDOW_RESPOND = 2002;

    public final static int DATA_REQ_ANNOUNCE_SHOWN = 2003;
    public final static int DATA_REQ_ANNOUNCE_CLOSED = 2004;
    public final static int DATA_REQ_REQUEST_CLOSE = 2005;


    protected void sendData(int toID, int requestID, Bundle data) {
        StandOutWindow.sendData(getContext(),
                FloatingWindowsManager.class,
                toID,
                requestID,
                data,
                null,
                getID());

    }

    public void onReceiveData(Window window, int requestCode, Bundle data, int fromId) {
        switch (requestCode) {
            case DATA_REQ_IS_POINT_IN_WINDOW_REQUEST:
                handleIsPointInWindowRequest(window, data, fromId);
                break;

            case FloatingWindowsManager.DATA_REQ_ROVER_MOVE_FINISHED:
                handleWindowMoveFinished(data);
        }
    }

    private void handleIsPointInWindowRequest(Window window, Bundle data, int fromId) {
        int x = data.getInt(DATA_KEY_IS_POINT_IN_WINDOW_X);
        int y = data.getInt(DATA_KEY_IS_POINT_IN_WINDOW_Y);

        Bundle answer = new Bundle();//answer container

        boolean result = isPointInWindow(window, new Point(x, y), fromId);
        answer.putBoolean(DATA_KEY_IS_POINT_IN_WINDOW_RESULT, result);//default answer

        sendData(fromId, DATA_REQ_IS_POINT_IN_WINDOW_RESPOND, answer);
    }

    protected boolean isPointInWindow(Window window, Point point, int fromId) {
        if (window != null) {
            StandOutWindow.StandOutLayoutParams hideParams = window.getLayoutParams();
            int xAreaRight = hideParams.x + window.getWidth();
            int xAreaLeft = hideParams.x;
            int yAreaTop = hideParams.y;
            int yAreaBottom = hideParams.y + window.getHeight();


            if (point.x <= xAreaRight && point.x >= xAreaLeft
                    && point.y <= yAreaBottom && point.y >= yAreaTop) {
                return true;
            }
        }

        return false;
    }

    private void handleWindowMoveFinished(Bundle data) {
        if (data.containsKey(FloatingWindowsManager.DATA_KEY_WINDOW_MOVE_RETURN_CODE)) {
            int returnCode = data.getInt(FloatingWindowsManager.DATA_KEY_WINDOW_MOVE_RETURN_CODE);
            onAnimateWindowFinished(returnCode);
        }
    }

    protected void onAnimateWindowFinished(int returnCode) {

    }

    //endregion Data Communication

    //region Identity Methods
    public static final int WINDOW_ID_ROVER = 101;
    public static final int WINDOW_ID_HOST = 102;
    public static final int WINDOW_ID_HIDDEN_ALERT = 103;


    public static final int WINDOW_ID_ROVER_HIDE = 205;


    public static WindowHelperBase getInstance(int id, Context context) {
        switch (id) {
            case WINDOW_ID_ROVER:
                return new RoverWindowHelper(context);
            case WINDOW_ID_HOST:
                return new HostWindowHelper(context);
            case WINDOW_ID_HIDDEN_ALERT:
                return new HiddenAlertWindowHelper(context);

            case WINDOW_ID_ROVER_HIDE:
                return new RoverHideWindowHelper(context);
            default:
                return null;
        }
    }

    //endregion Identity Methods

    //region Window Flags Methods

    protected void addWindowFlags(int flags) {
        Bundle data = new Bundle();

        data.putInt(FloatingWindowsManager.DATA_KEY_FLAGS, flags);

        sendData(FloatingWindowsManager.DISREGARD_ID,
                FloatingWindowsManager.DATA_REQ_WINDOW_ADD_FLAGS,
                data);
    }

    protected void removeWindowFlags(int flags) {
        Bundle data = new Bundle();

        data.putInt(FloatingWindowsManager.DATA_KEY_FLAGS, flags);

        sendData(FloatingWindowsManager.DISREGARD_ID,
                FloatingWindowsManager.DATA_REQ_WINDOW_REMOVE_FLAGS,
                data);
    }


    //endregion Window Flags Methods

    //region Animations

    public Animation getShowAnimation() {
        Animation anim = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        return anim;
    }

    public Animation getHideAnimation() {
        Animation anim = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        return anim;
    }

    public Animation getCloseAnimation() {
        return AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
    }

    //endregion Animations

    //region KeyEvents

    public boolean onKeyEvent(KeyEvent event) {
        return false;
    }

    //endregion KeyEvents

    //region Focus Methods

    protected void requestFocus() {
        sendData(FloatingWindowsManager.DISREGARD_ID, FloatingWindowsManager.DATA_REQ_WINDOW_FOCUS, null);
    }

    protected void requestUnfocus() {
        sendData(FloatingWindowsManager.DISREGARD_ID, FloatingWindowsManager.DATA_REQ_WINDOW_UNFOCUS, null);
    }

    //endregion Focus Methods

    //region Dim Methods

    protected void requestDim() {
        sendData(FloatingWindowsManager.DISREGARD_ID, FloatingWindowsManager.DATA_REQ_WINDOW_DIM, null);
    }

    protected void requestUndim() {
        sendData(FloatingWindowsManager.DISREGARD_ID, FloatingWindowsManager.DATA_REQ_WINDOW_UNDIM, null);
    }

    //endregion Dim Methods

    //region Orientation Methods

    public void onConfigurationChanged(Configuration newConfig) {

    }

    //endregion Orientation Methods

    //region Preferences Changed

    public void onPreferencesChanged(String action) {
    }

    //endregion Preferences Changed

}
