package com.schiztech.rovers.app.windows.helpers;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;

import com.schiztech.rovers.app.roverhosts.HostManagerBase;
import com.schiztech.rovers.app.roverhosts.HostManagerHorizontal;
import com.schiztech.rovers.app.roverhosts.HostManagerVertical;
import com.schiztech.rovers.app.roveritems.FolderRover;
import com.schiztech.rovers.app.roveritems.IRover;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.PrefUtils;
import com.schiztech.rovers.app.utils.RoversUtils;
import com.schiztech.rovers.app.utils.Utils;
import com.schiztech.rovers.app.windows.FloatingWindowsManager;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

/**
 * Created by schiz_000 on 6/5/2014.
 * <p/>
 * helper for the rovers host window.
 * shows a list with the rovers
 */
public class HostWindowHelper extends WindowHelperBase {
    private static final String TAG = LogUtils.makeLogTag("HostWindowHelper");
    HostManagerBase mHostManager;
    Utils.Corner mCurrentCorner;

    int mTriggerSize;

    protected HostWindowHelper(Context context) {
        super(context);


        if(PrefUtils.isHostOrientationLandscape(getContext())) {
            mHostManager = HostManagerHorizontal.getInstance(getContext());

        }
        else {
            mHostManager = HostManagerVertical.getInstance(getContext());
        }

        mHostManager.init();

        //when touching an empty space - close host.
        mHostManager.setEmptyTouchListener(mEmptyTouchListener);

        //when currently displayed folder changes
        mHostManager.setFolderChangedListener(mFolderChangedListener);

        //when rover is launched
        mHostManager.setRoverLaunchListener(mRoverLaunchListener);

        mCurrentCorner = mHostManager.getCurrentCorner();

        mTriggerSize = RoversUtils.getTriggerSize(getContext());

    }

    @Override
    public int getID() {
        return WINDOW_ID_HOST;
    }

    //region View & Decor Methods
    @Override
    public View getView() {
        return mHostManager.getParentView();
    }

    @Override
    public StandOutWindow.StandOutLayoutParams getParams(FloatingWindowsManager windowsManager) {

        StandOutWindow.StandOutLayoutParams params = windowsManager.getParamsInstance(getID(),
                mHostManager.getRequiredWidthSize(mTriggerSize),
                mHostManager.getRequiredHeightSize(mTriggerSize),
                mHostManager.getRequiredXPosition(mTriggerSize),
                mHostManager.getRequiredYPosition(mTriggerSize));

        return params;
    }

    @Override
    public int getFlags() {
        return StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE |
                StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE;
    }


    //endregion View & Decor Methods

    //region Touch Methods

    @Override
    public boolean onTouchBody(View view, MotionEvent event) {
        if(mIsClosing)
            return false;//ignore all touches when closing, especially the request is point in window!
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            if(mIsEditMode){
                cancelEditMode();
            }
            else {
                Bundle data = new Bundle();
                data.putInt(DATA_KEY_IS_POINT_IN_WINDOW_X, (int) event.getRawX());
                data.putInt(DATA_KEY_IS_POINT_IN_WINDOW_Y, (int) event.getRawY());
                sendData(WINDOW_ID_ROVER, DATA_REQ_IS_POINT_IN_WINDOW_REQUEST, data);
            }
        }
        return false;
    }

    //endregion Touch Methods

    //region Window State Methods
    @Override
    public boolean onShow() {
        LogUtils.LOGD(TAG, "host is showing");
        mHostManager.setEditModeListener(mEditModeListener);
        return super.onShow();
    }
    @Override
    public void onWindowShown() {
        mHostManager.attach();
        //announce rover window that host is expanded
        sendData(WINDOW_ID_ROVER, DATA_REQ_ANNOUNCE_SHOWN, null);
    }
    @Override
    public void onWindowClosed() {
        LogUtils.LOGD(TAG, "host is closing");
        //announce rover window that host is collapsed
        sendData(WINDOW_ID_ROVER, DATA_REQ_ANNOUNCE_CLOSED, null);
        mHostManager = null;
    }

    @Override
    public void onWindowHidden() {
        LogUtils.LOGD(TAG, "host is hiding");
        //announce rover window that host is collapsed
        sendData(WINDOW_ID_ROVER, DATA_REQ_ANNOUNCE_CLOSED, null);

    }

    private boolean mIsClosing = false;
    private void onCloseRequest() {
        mIsClosing = true;
        mHostManager.detach(new Utils.AnimationFinishedListener() {
            @Override
            public void onAnimationFinished() {
                sendData(FloatingWindowsManager.DISREGARD_ID, FloatingWindowsManager.DATA_REQ_CLOSE, null);
            }
        });
        sendData(WINDOW_ID_ROVER, RoverWindowHelper.DATA_REQ_UPDATE_VISUALS, null);
    }

    //endregion Window State Methods

    //region Edit Mode

    private boolean mIsEditMode = false;

    protected void startEditMode() {
        mHostManager.startEditMode();
    }

    protected void cancelEditMode() {
        mHostManager.cancelEditMode();
    }


    protected HostManagerBase.EditModeListener mEditModeListener = new HostManagerBase.EditModeListener() {
        @Override
        public void onEditModeStarted() {
            LogUtils.LOGD(TAG, "Edit mode started");

            mIsEditMode = true;
            sendData(WindowHelperBase.WINDOW_ID_ROVER, DATA_REQ_EDIT_MODE_STARTED, null);

        }


        @Override
        public void onEditModeFinished() {
            LogUtils.LOGD(TAG, "Edit mode finished");
            mIsEditMode = false;

            sendData(WindowHelperBase.WINDOW_ID_ROVER, DATA_REQ_EDIT_MODE_CANCELED, null);
        }
    };

    public static final int DATA_REQ_EDIT_MODE_STARTED = 3001;
    public static final int DATA_REQ_EDIT_MODE_CANCELED = 3002;
    public static final int DATA_REQ_EDIT_MODE_START_REQUEST = 3011;
    public static final int DATA_REQ_EDIT_MODE_CANCEL_REQUEST = 3012;

    //endregion Edit Mode

    //region Data Communication


    public void onReceiveData(Window window, int requestCode, Bundle data, int fromId) {
        switch (requestCode) {
            case DATA_REQ_IS_POINT_IN_WINDOW_RESPOND:
                handleIsPointInWindowRespond(data, fromId);
                break;

            case DATA_REQ_EDIT_MODE_CANCEL_REQUEST:
                cancelEditMode();
                break;
            case DATA_REQ_EDIT_MODE_START_REQUEST:
                startEditMode();
                break;

            case DATA_REQ_REQUEST_CLOSE:
                onCloseRequest();
                break;

            case DATA_REQ_BACK_EVENT_TRIGGERED:
                onBackEventTriggered();
                break;
        }
    }

    private void handleIsPointInWindowRespond(Bundle data, int fromId) {
        if (fromId != WINDOW_ID_ROVER)
            return; // cares only if message is from the ROVER window

        boolean result = data.getBoolean(DATA_KEY_IS_POINT_IN_WINDOW_RESULT);

        if (result) {
            //do nothing
        } else {
            requestCollapse();
        }
    }

    private void requestCollapse(){
        //if edit mode - stop the edit mode before closing
        if (mIsEditMode) {
            cancelEditMode();
        }

        sendData(WINDOW_ID_ROVER, RoverWindowHelper.DATA_REQ_REQUEST_COLLAPSE, null);
    }

    //endregion Data Communication

    //region Animations

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

    //endregion Animations

    //region Events

    public static final int DATA_REQ_BACK_EVENT_TRIGGERED = 3021;

    private void onBackEventTriggered(){
        boolean result = mHostManager.navigateBack();

        if(result == false) {//if navigation has no back folder
            requestCollapse();
        }

    }

    private HostManagerBase.FolderChangedListener mFolderChangedListener = new HostManagerBase.FolderChangedListener() {
        @Override
        public void onFolderChanged(FolderRover folder, boolean isRoot) {
            Bundle data = new Bundle();
            if(!isRoot) {
                data.putInt(RoverWindowHelper.DATA_KEY_VISUAL_ICON_RESOURCE, folder.getIconResource());
                data.putInt(RoverWindowHelper.DATA_KEY_VISUAL_BACKGROUND_COLOR, folder.getColor(getContext()));
            }

            sendData(WINDOW_ID_ROVER, RoverWindowHelper.DATA_REQ_UPDATE_VISUALS, data);
        }
    };

    private HostManagerBase.EmptyTouchListener mEmptyTouchListener = new HostManagerBase.EmptyTouchListener() {
        @Override
        public void onEmptyTouch() {
            if(mIsEditMode){
                cancelEditMode();
            }
            else {
                requestCollapse();
            }
        }
    };

    private HostManagerBase.RoverLaunchListener mRoverLaunchListener = new HostManagerBase.RoverLaunchListener() {
        @Override
        public void onRoverLaunch(IRover rover) {
            sendData(WINDOW_ID_ROVER, RoverWindowHelper.DATA_REQ_REQUEST_COLLAPSE, null);
        }
    };

    //endregion Events
}
