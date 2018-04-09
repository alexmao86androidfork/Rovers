package com.schiztech.rovers.app.windows;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.MainActivity;
import com.schiztech.rovers.app.utils.CacheUtils;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.PrefUtils;
import com.schiztech.rovers.app.utils.RoverlyticsUtils;
import com.schiztech.rovers.app.utils.Utils;
import com.schiztech.rovers.app.windows.helpers.WindowHelperBase;

import java.util.ArrayList;
import java.util.List;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.ui.Window;

/**
 * Created by schiz_000 on 5/23/2014.
 */
public class FloatingWindowsManager extends StandOutWindow {
    private static final String TAG = LogUtils.makeLogTag("FloatingWindowsManager");

    public FloatingWindowsManager() {
        windows = new SparseArray<WindowHelperBase>();
    }

    //region Service States

    @Override
    public void onCreate() {
        super.onCreate();

        registerPreferencesChangedReceiver();

        CacheUtils.clearIconsCache();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (intent != null) {
                super.onStartCommand(intent, flags, startId);
            } else {
                LogUtils.LOGI(TAG, "Service restarted");
                if (PrefUtils.getRoversIsActivatedValue(getApplicationContext())) {
                    Utils.syncRoverWindow(getApplicationContext(), true);
                }
            }

        }
        catch (Exception e){
            LogUtils.LOGE(TAG, "OnStartCommand: " + e.getMessage());
        }

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        WindowAnimator.onDestroy();
        unRegisterPreferencesChangedReceiver();
        super.onDestroy();
    }

    //endregion

    //region Labeling Methods
    @Override
    public String getAppName() {
        return "MostBasicWindow";
    }

    @Override
    public int getAppIcon() {
        return android.R.drawable.btn_star;
    }

    //endregion Labeling Methods

    //region View & Decor Methods

    @Override
    public void createAndAttachView(int id, FrameLayout frame) {
        View view = getWindowHelper(id).getView();
        frame.addView(view);
    }

    @Override
    public StandOutLayoutParams getParams(int id, Window window) {

        return getWindowHelper(id, true).getParams(this);
    }

    @Override
    public int getFlags(int id) {
        return super.getFlags(id) | getWindowHelper(id).getFlags();
    }

    //endregion View & Decor Methods

    //region Touch & Movement Methods

    public static final int DATA_REQ_ROVER_MOVE_STARTED = 1001;
    public static final int DATA_REQ_ROVER_MOVE_FINISHED = 1002;
    public static final int DATA_REQ_WINDOW_MOVE_REQUEST = 1003;
    public static final String DATA_KEY_WINDOW_MOVE_X = "window_move_x";
    public static final String DATA_KEY_WINDOW_MOVE_Y = "window_move_y";
    public static final String DATA_KEY_WINDOW_MOVE_VELOCITY_X = "window_move_velocity_x";
    public static final String DATA_KEY_WINDOW_MOVE_VELOCITY_Y = "window_move_velocity_y";
    public static final String DATA_KEY_WINDOW_MOVE_TYPE = "window_move_type";
    public static final String DATA_KEY_WINDOW_MOVE_RETURN_CODE = "window_move_return_code";
    public static final int DEFAULT_WINDOW_MOVE_RETURN_CODE = -1;


    private void onRoverMoveStarted() {
        //show the "hide" X location
        tryShowWindow(WindowHelperBase.WINDOW_ID_ROVER_HIDE);
    }

    private void onRoverMoveFinished() {
        //close the "hide" X location
        tryCloseWindow(WindowHelperBase.WINDOW_ID_ROVER_HIDE);

    }

    private void onWindowMoveRequest(final int id, Bundle data) {
        if (!data.containsKey(DATA_KEY_WINDOW_MOVE_X) || !data.containsKey(DATA_KEY_WINDOW_MOVE_Y)) {
            LogUtils.LOGE(TAG, "Tried to move window #" + id + " but one or more of the coordinates didn't exist!");
            return;
        }


        if (getWindow(id) == null) {
            LogUtils.LOGE(TAG, "Tried to move window #" + id + " but window is null!");
            return;
        }

        int x = data.getInt(DATA_KEY_WINDOW_MOVE_X);
        int y = data.getInt(DATA_KEY_WINDOW_MOVE_Y);
        float xVelocity = 0;
        float yVelocity = 0;
        Utils.MoveType moveType = Utils.MoveType.Regular;

        if (data.containsKey(DATA_KEY_WINDOW_MOVE_TYPE)) {
            moveType = Utils.MoveType.fromInteger(data.getInt(DATA_KEY_WINDOW_MOVE_TYPE));
        }

        if (data.containsKey(DATA_KEY_WINDOW_MOVE_VELOCITY_X)) {
            xVelocity = data.getFloat(DATA_KEY_WINDOW_MOVE_VELOCITY_X);
        }

        if (data.containsKey(DATA_KEY_WINDOW_MOVE_VELOCITY_Y)) {
            yVelocity = data.getFloat(DATA_KEY_WINDOW_MOVE_VELOCITY_Y);
        }

        int returnCode = DEFAULT_WINDOW_MOVE_RETURN_CODE;

        if (data.containsKey(DATA_KEY_WINDOW_MOVE_RETURN_CODE)) {
            returnCode = data.getInt(DATA_KEY_WINDOW_MOVE_RETURN_CODE);
        }


        WindowAnimator.animateLocation(id, getWindow(id), new Point(x, y), new PointF(xVelocity, yVelocity), moveType, returnCode);
    }

    @Override
    public boolean onTouchBody(int id, final Window window, View view,
                               MotionEvent event) {
        try {

            return getWindowHelper(id).onTouchBody(view, event);
        } catch (NullPointerException e) {
            LogUtils.LOGW(TAG, "Could not onTouchBody on non-existing window helper - id " + id);
            return false;
        }


    }

    public void onMove(int id, Window window, View view, MotionEvent event) {

        try {
            getWindowHelper(id).onMove(window, view, event);
        } catch (NullPointerException e) {
            LogUtils.LOGW(TAG, "Could not onMove on non-existing window helper - id " + id);
        }


    }

    //endregion Touch & Movement Methods

    //region Data Communication
    @Override
    public void onReceiveData(int id, int requestCode, Bundle data,
                              Class<? extends StandOutWindow> fromCls, int fromId) {
        if (id == DISREGARD_ID) {
            switch (requestCode) {
                case DATA_REQ_ROVER_MOVE_STARTED:
                    onRoverMoveStarted();
                    break;
                case DATA_REQ_ROVER_MOVE_FINISHED:
                    onRoverMoveFinished();
                    break;

                case DATA_REQ_WINDOW_MOVE_REQUEST:
                    onWindowMoveRequest(fromId, data);
                    break;

                case DATA_REQ_WINDOW_ADD_FLAGS:
                    onAddWindowFlagsRequest(fromId, data);
                    break;

                case DATA_REQ_WINDOW_REMOVE_FLAGS:
                    onRemoveWindowFlagsRequest(fromId, data);
                    break;

                case DATA_REQ_CLOSE:
                    tryCloseWindow(fromId);
                    break;

                case DATA_REQ_CLOSE_ANOTHER:
                    tryCloseWindow(data);
                    break;

                case DATA_REQ_SHOW:
                    tryShowWindow(fromId);
                    break;

                case DATA_REQ_SHOW_ANOTHER:
                    tryShowWindow(data);
                    break;
                case DATA_REQ_HIDE:
                    tryHideWindow(fromId);
                    break;
                case DATA_REQ_BRING_TO_FRONT:
                    tryBringToFront(fromId);
                    break;

                case DATA_REQ_WINDOW_LAYOUTPARAMS_UPDATE:
                    onWindowLayoutParamsUpdate(fromId, data);
                    break;

                case DATA_REQ_WINDOW_FOCUS:
                    onWindowFocusRequest(fromId);
                    break;
                case DATA_REQ_WINDOW_UNFOCUS:
                    onWindowUnfocusRequest(fromId);
                    break;

                case DATA_REQ_WINDOW_DIM:
                    onWindowDimRequest(fromId);
                    break;
                case DATA_REQ_WINDOW_UNDIM:
                    onWindowUndimRequest(fromId);
                    break;
            }
        } else {
            if (getWindow(id) != null)
                getWindowHelper(id).onReceiveData(getWindow(id), requestCode, data, fromId);
            else
                LogUtils.LOGW(TAG, "Could not deliver data to non-existing window - id " + id);
        }
    }


    //endregion Data Communication

    //region Window Flags Methods
    public static final int DATA_REQ_WINDOW_ADD_FLAGS = 1010;
    public static final int DATA_REQ_WINDOW_REMOVE_FLAGS = 1011;
    public static final String DATA_KEY_FLAGS = "flags";

    private void onAddWindowFlagsRequest(int windowId, Bundle data) {
        int flags = data.getInt(DATA_KEY_FLAGS, Integer.MIN_VALUE);
        if (flags == Integer.MIN_VALUE) {
            LogUtils.LOGE(TAG, "Can't add flags with no flags in data");
            return;

        }
        Window window = getWindow(windowId);
        if (window == null) {
            LogUtils.LOGE(TAG, "Can't add flags to a null window");
            return;
        }

        window.addFlags(flags);
    }

    private void onRemoveWindowFlagsRequest(int windowId, Bundle data) {
        int flags = data.getInt(DATA_KEY_FLAGS, Integer.MIN_VALUE);
        if (flags == Integer.MIN_VALUE) {
            LogUtils.LOGE(TAG, "Can't remove flags with no flags in data");
            return;

        }
        Window window = getWindow(windowId);
        if (window == null) {
            LogUtils.LOGE(TAG, "Can't remove flags from a null window");
            return;
        }

        window.removeFlags(flags);
    }

    //endregion Window Flags Methods

    //region WindowHelper Methods
    private SparseArray<WindowHelperBase> windows;

    public StandOutLayoutParams getParamsInstance(int id, int w, int h, int xpos, int ypos) {
        return new StandOutLayoutParams(id, w, h, xpos, ypos);
    }

    private WindowHelperBase getWindowHelper(int id) {
        return getWindowHelper(id, false);
    }

    private WindowHelperBase getWindowHelper(int id, boolean isCreateIfNotExist) {
        if (isCreateIfNotExist && windows.indexOfKey(id) < 0) {
            WindowHelperBase helper = WindowHelperBase.getInstance(id, getApplicationContext());
            windows.put(id, helper);
            LogUtils.LOGD(TAG, "got new helper instance for  #" + id);
        }

        if (windows.get(id) == null) {
            LogUtils.LOGE(TAG, "Couldn't get helper for window ID #" + id);
            windows.remove(id);//don't let the null object stay there!
            return null;
        }

        return windows.get(id);
    }

    //endregion WindowHelper Methods

    //region Window State Methods

    public static final int DATA_REQ_SHOW = -999;
    public static final int DATA_REQ_SHOW_ANOTHER = -998;
    public static final String DATA_KEY_WINDOW_ID = "window_id";


    public static final int DATA_REQ_HIDE = -996;
    public static final int DATA_REQ_CLOSE = -995;
    public static final int DATA_REQ_CLOSE_ANOTHER = -994;
    public static final int DATA_REQ_BRING_TO_FRONT = -990;

    private void tryBringToFront(int id) {
        try {
            bringToFront(id);
        } catch (Exception e) {
            LogUtils.LOGE(TAG, "Failed to bring to front window id #" + id + ". E: " + e.toString());
        }
    }

    private void tryHideWindow(int id) {
        try {
            LogUtils.LOGD(TAG, "Trying to hide window " + id);
            hide(id);
        } catch (Exception e) {
            LogUtils.LOGE(TAG, "Failed to hide window id #" + id + ". E: " + e.toString());
        }
    }

    private void tryShowWindow(Bundle data) {
        if (!data.containsKey(DATA_KEY_WINDOW_ID)) {
            LogUtils.LOGE(TAG, "Requested to show another window, without giving the id of the window to show!");
            return;
        }

        int windowIdToShow = data.getInt(DATA_KEY_WINDOW_ID);
        tryShowWindow(windowIdToShow);
    }

    private void tryShowWindow(int id) {

        try {
            if (getWindow(id) == null)
                show(id);
        } catch (Exception e) {
            LogUtils.LOGE(TAG, "Failed to show window id #" + id + ". E: " + e.toString());
        }


    }

    private void tryCloseWindow(int id) {
        try {
            close(id);
        } catch (Exception e) {
            LogUtils.LOGE(TAG, "Failed to close window id #" + id + ". E: " + e.toString());
        }


    }

    private void tryCloseWindow(Bundle data) {
        if (!data.containsKey(DATA_KEY_WINDOW_ID)) {
            LogUtils.LOGE(TAG, "Requested to close another window, without giving the id of the window to show!");
            return;
        }

        int windowIdToClose = data.getInt(DATA_KEY_WINDOW_ID);
        tryCloseWindow(windowIdToClose);
    }

    @Override
    public void onWindowShown(int id) {
        if (getWindowHelper(id) != null)
            getWindowHelper(id).onWindowShown();
    }

    @Override
    public void onWindowClosed(int id) {

        getWindowHelper(id).onWindowClosed();

        if (windows.indexOfKey(id) >= 0) {
            windows.remove(id);
            LogUtils.LOGD(TAG, "Removed window ID #" + id + " due to closed windows.");
        }
    }

    @Override
    public void onWindowHidden(int id) {

        getWindowHelper(id).onWindowHidden();
    }

    @Override
    public boolean onShow(int id, Window window) {
        WindowAnimator.initWindowSprings();
        WindowHelperBase winHelper = getWindowHelper(id);
        if (winHelper != null)
            return winHelper.onShow();

        return false;
    }

    @Override
    public boolean onHide(int id, Window window) {
        return getWindowHelper(id).onHide();
    }

    @Override
    public boolean onClose(int id, Window window) {
        WindowHelperBase winHelper = getWindowHelper(id);
        if (winHelper != null)
            return getWindowHelper(id).onClose();

        return false;
    }

    //endregion Window State Methods

    //region Window Layout Params
    public static final int DATA_REQ_WINDOW_LAYOUTPARAMS_UPDATE = 1020;
    public static final String DATA_KEY_WINDOW_LAYOUTPARAMS_HEIGHT = "layoutparams_height";
    public static final String DATA_KEY_WINDOW_LAYOUTPARAMS_WIDTH = "layoutparams_width";

    protected void onWindowLayoutParamsUpdate(int id, Bundle data) {
        Window window = getWindow(id);
        if (window == null) {
            LogUtils.LOGE(TAG, "Failed update layout params for window #" + id + ". null window");
            return;
        }
        if (data == null) {
            LogUtils.LOGE(TAG, "Failed update layout params for window #" + id + ". null data");
            return;
        }


        if (data.containsKey(DATA_KEY_WINDOW_LAYOUTPARAMS_HEIGHT)) {
            int height = data.getInt(DATA_KEY_WINDOW_LAYOUTPARAMS_HEIGHT);
            window.getLayoutParams().height = height;
        }

        if (data.containsKey(DATA_KEY_WINDOW_LAYOUTPARAMS_WIDTH)) {
            int width = data.getInt(DATA_KEY_WINDOW_LAYOUTPARAMS_WIDTH);
            window.getLayoutParams().width = width;
        }


        window.edit().commit();//force update window layout

    }
    //endregion Window Layout Params

    //region Animations

    @Override
    public Animation getShowAnimation(int id) {
        WindowHelperBase winHelper = getWindowHelper(id);
        if (winHelper != null)
            return getWindowHelper(id).getShowAnimation();

        return null;
    }

    @Override
    public Animation getHideAnimation(int id) {
        return getWindowHelper(id).getHideAnimation();
    }

    @Override
    public Animation getCloseAnimation(int id) {
        WindowHelperBase winHelper = getWindowHelper(id);
        if (winHelper != null)
            return getWindowHelper(id).getCloseAnimation();

        return null;
    }

    //endregion

    //region KeyEvents

    @Override
    public boolean onKeyEvent(int id, Window window, KeyEvent event) {
        return getWindowHelper(id).onKeyEvent(event);
    }

    //endregion KeyEvents

    //region Focus Methods
    public static final int DATA_REQ_WINDOW_FOCUS = 1030;
    public static final int DATA_REQ_WINDOW_UNFOCUS = 1031;

    private void onWindowFocusRequest(int id) {
        try {
            if (getWindow(id) == null) {
                LogUtils.LOGE(TAG, "Tried focusing a null window! #" + id);
            }
            focus(id);
            StandOutLayoutParams params = getWindow(id).getLayoutParams();
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            updateViewLayout(id, params);
        }
        catch (Exception e){
        }
    }

    private void onWindowUnfocusRequest(int id) {
        if (getWindow(id) == null) {
            LogUtils.LOGE(TAG, "Tried unfocusing a null window! #" + id);
        }
        unfocus(id);
        StandOutLayoutParams params = getWindow(id).getLayoutParams();
        params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        updateViewLayout(id, params);
    }

    //endregion Focus Methods

    //region Dim Methods
    public static final int DATA_REQ_WINDOW_DIM = 1040;
    public static final int DATA_REQ_WINDOW_UNDIM = 1041;
    public static final float DEFAULT_DIM_AMOUNT = 0.5f;

    private void onWindowDimRequest(int id) {
        try {
            if (getWindow(id) == null) {
                LogUtils.LOGE(TAG, "Tried dimming a null window! #" + id);
            }

            StandOutLayoutParams params = getWindow(id).getLayoutParams();
            params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            params.dimAmount = DEFAULT_DIM_AMOUNT;
            updateViewLayout(id, params);
        }
        catch (Exception e){
        }
    }

    private void onWindowUndimRequest(int id) {
        if (getWindow(id) == null) {
            LogUtils.LOGE(TAG, "Tried undimming a null window! #" + id);
        }

        StandOutLayoutParams params = getWindow(id).getLayoutParams();
        params.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.dimAmount = 0;
        updateViewLayout(id, params);
    }

    //endregion Dim Methods

    //region Orientation Change

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        for (int i = 0; i < windows.size(); i++) {
            WindowHelperBase helper = windows.valueAt(i);
            helper.onConfigurationChanged(newConfig);
        }

    }

    //endregion Orientation Change

    //region Preferences Changed

    private static final int PREFERENCES_CHANGE_DELAY = 3000;
    Object mPrefChangedLocker = new Object();
    Handler mPrefChangedHandler = null;
    List<String> mPrefChangedActions = new ArrayList<>();
    Runnable mPrefChangedRunnable = new Runnable() {

        public void run() {
            for (String action : mPrefChangedActions) {
                onPreferencesChanged(action);
            }

            mPrefChangedActions.clear();
        }
    };

    private BroadcastReceiver mPreferencesChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (!mPrefChangedActions.contains(action))
                mPrefChangedActions.add(action);

            Toast.makeText(getApplicationContext(), "The changes will be updated in a few seconds", Toast.LENGTH_SHORT).show();

            synchronized (mPrefChangedLocker) {
                if (mPrefChangedHandler != null) {
                    mPrefChangedHandler.removeCallbacks(mPrefChangedRunnable);
                    mPrefChangedHandler = null;
                }

                mPrefChangedHandler = new Handler();
                mPrefChangedHandler.postDelayed(mPrefChangedRunnable, PREFERENCES_CHANGE_DELAY);
            }
        }
    };

    public void onPreferencesChanged(String action) {
        for (int i = 0; i < windows.size(); i++) {
            WindowHelperBase helper = windows.valueAt(i);
            helper.onPreferencesChanged(action);
        }
    }


    private void registerPreferencesChangedReceiver() {
        IntentFilter filter = new IntentFilter();

        //add all pref intent actions
        String[] actions = PrefUtils.getIntentActions();
        for (String action : actions) {
            filter.addAction(action);
        }
        registerReceiver(mPreferencesChangedReceiver, filter);
    }

    private void unRegisterPreferencesChangedReceiver() {
        if (mPreferencesChangedReceiver != null) {
            unregisterReceiver(mPreferencesChangedReceiver);
            mPreferencesChangedReceiver = null;
        }
    }

    //endregion Preferences Changed

    //region Window Animator

    public static class WindowAnimator {

        static Spring xSpring;
        static Spring ySpring;

        static SpringConfig mSuddenSpringConfig;
        static SpringConfig mRegularSpringConfig;

        static WindowSpringListener mSpringListener;

        public static void initWindowSprings() {
            try {
                if (mSuddenSpringConfig == null)
                    mSuddenSpringConfig = new SpringConfig(800, 50);

                if (mRegularSpringConfig == null)
                    mRegularSpringConfig = new SpringConfig(350, 28);


                if (mSpringListener == null)
                    mSpringListener = WindowSpringListener.getInstance();

                if (xSpring == null) {
                    xSpring = Utils.getSpringSystem().createSpring();
                    xSpring.setSpringConfig(mRegularSpringConfig);
                    xSpring.addListener(mSpringListener);
                }

                if (ySpring == null) {
                    ySpring = Utils.getSpringSystem().createSpring();
                    ySpring.setSpringConfig(mRegularSpringConfig);
                    ySpring.addListener(mSpringListener);
                }
            } catch (Exception e) {
                LogUtils.LOGE(TAG, "Failed initWindowSprings: " + e.getMessage());
            }
        }

        public static void animateLocation(final int windowID, final Window window, Point destination, PointF velocity, Utils.MoveType moveType, final int returnCode) {

            if(xSpring == null || ySpring == null)
                return;

            if (window != null) {
                captureDistance(new PointF((float) xSpring.getCurrentValue(), (float) ySpring.getCurrentValue()), destination, window.getContext());
            }

            mSpringListener.setIsActivated(false);
            mSpringListener.setIsAnnounced(false);
            mSpringListener.setReturnCode(returnCode);
            mSpringListener.setWindow(window);
            mSpringListener.setWindowID(windowID);

            xSpring.setEndValue(xSpring.getCurrentValue()).setAtRest();
            ySpring.setEndValue(ySpring.getCurrentValue()).setAtRest();

            xSpring.setVelocity(velocity.x);
            ySpring.setVelocity(velocity.y);

            xSpring.setSpringConfig(moveType == Utils.MoveType.Regular ? mRegularSpringConfig : mSuddenSpringConfig);
            ySpring.setSpringConfig(moveType == Utils.MoveType.Regular ? mRegularSpringConfig : mSuddenSpringConfig);

            if (moveType == Utils.MoveType.Instant) {
                mSpringListener.setIsActivated(true);
                xSpring.setCurrentValue(destination.x).setAtRest();
                ySpring.setCurrentValue(destination.y).setAtRest();
            } else {

                xSpring.setCurrentValue(window.getLayoutParams().x + window.getChildAt(0).getX()).setAtRest();
                ySpring.setCurrentValue(window.getLayoutParams().y + window.getChildAt(0).getY()).setAtRest();
            }

            xSpring.setEndValue(destination.x);
            ySpring.setEndValue(destination.y);

            mSpringListener.setIsActivated(true);


        }

        public static void onAnimateWindowFinished(int windowID, Window window, int returnCode) {

            Bundle data = new Bundle();
            data.putInt(DATA_KEY_WINDOW_MOVE_RETURN_CODE, returnCode);
            StandOutWindow.sendData(window.getContext(), FloatingWindowsManager.class, windowID, DATA_REQ_ROVER_MOVE_FINISHED, data, FloatingWindowsManager.class, DISREGARD_ID);
        }

        private static void moveInstant(final Window window, Point destination) {

            if (window == null) return;
            window.edit().setPosition(destination.x, destination.y).commit();


        }

        private static void captureDistance(PointF currentLocation, Point destination, Context context) {
            if (context == null) return;

            double distance = Math.sqrt((currentLocation.x - destination.x) * (currentLocation.x - destination.x) +
                    (currentLocation.y - destination.y) * (currentLocation.y - destination.y));

            int dpi = Utils.getDeviceDpi(context);
            if (dpi != 0) {
                RoverlyticsUtils.addDistance(context, (float) (distance / dpi));
            }

        }

        static class WindowSpringListener implements SpringListener {
            Window mWindow;
            int mWindowID;
            boolean mIsAnnouncedFinished = false;
            int mReturnCode;
            //Utils.MoveType mMoveType;
            boolean mIsActivated = false;

            public static WindowSpringListener getInstance() {
                return new WindowSpringListener();
            }

            public void setIsAnnounced(boolean isAnnounced) {
                mIsAnnouncedFinished = isAnnounced;
            }

            public void setIsActivated(boolean isActivated) {
                mIsActivated = isActivated;
            }

            public void setWindow(Window window) {
                mWindow = window;
            }

            public void setWindowID(int windowID) {
                mWindowID = windowID;
            }

            public void setReturnCode(int returnCode) {
                mReturnCode = returnCode;
            }

            public void notifyFinished() {

                if (!mIsAnnouncedFinished) {
                    mIsAnnouncedFinished = true;
                    onAnimateWindowFinished(mWindowID, mWindow, mReturnCode);
                }
            }

            @Override
            public void onSpringUpdate(Spring spring) {
                if (!mIsActivated || xSpring == null || ySpring == null) {
                    return;
                }

                double x = xSpring.getCurrentValue();
                double y = ySpring.getCurrentValue();

                moveInstant(mWindow, new Point((int) x, (int) y));

                if (mReturnCode != DEFAULT_WINDOW_MOVE_RETURN_CODE) {
                    if ((int) x == (int) xSpring.getEndValue()) {
                        xSpring.setAtRest();
                        notifyFinished();
                    }

                    if ((int) y == (int) ySpring.getEndValue()) {
                        ySpring.setAtRest();
                        notifyFinished();
                    }
                }


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
        }

        public static void onDestroy() {
            if (xSpring != null) {
                xSpring.removeAllListeners();
                xSpring = null;
            }

            if (ySpring != null) {
                ySpring.removeAllListeners();
                ySpring = null;
            }

            if (mSpringListener != null) {
                mSpringListener.setWindow(null);
                mSpringListener = null;
            }

            mRegularSpringConfig = null;
            mSuddenSpringConfig = null;
        }
    }

    //endregion Window Animator


    @Override
    public Notification getPersistentNotification(int id) {
        Intent hideIntent = getHideIntent(this, FloatingWindowsManager.class, WindowHelperBase.WINDOW_ID_ROVER);
        PendingIntent hidePendingIntent = PendingIntent.getService(this, 0, hideIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent appIntent = new Intent(this, MainActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(this, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        boolean aboveApi16 = Utils.isAndroidVersionEqualOrAbove(Build.VERSION_CODES.JELLY_BEAN);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentIntent(hidePendingIntent)
                        .setSmallIcon(aboveApi16 ? R.drawable.ic_navigation_switch : R.drawable.ic_notification_logo)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                        .setContentTitle(Utils.getString(getApplicationContext(), R.string.notification_title))
                        .setContentText(Utils.getString(getApplicationContext(), R.string.notification_hide_trigger))
                        .setOngoing(true)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setTicker(Utils.getString(getApplicationContext(), R.string.notification_title))
                        .addAction(R.drawable.ic_notification_settings, Utils.getString(getApplicationContext(), R.string.notification_settings), appPendingIntent);

        return mBuilder.build();

    }

    @Override
    public Notification getHiddenNotification(int id) {
        Intent showIntent = getShowIntent(this, FloatingWindowsManager.class, WindowHelperBase.WINDOW_ID_ROVER);
        PendingIntent showPendingIntent = PendingIntent.getService(this, 0, showIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent appIntent = new Intent(this, MainActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(this, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        boolean aboveApi16 = Utils.isAndroidVersionEqualOrAbove(Build.VERSION_CODES.JELLY_BEAN);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentIntent(showPendingIntent)
                        .setSmallIcon(aboveApi16 ? R.drawable.ic_navigation_switch : R.drawable.ic_notification_logo)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                        .setContentTitle(Utils.getString(getApplicationContext(), R.string.notification_title))
                        .setContentText(Utils.getString(getApplicationContext(), R.string.notification_show_trigger))
                        .setOngoing(true)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setTicker(Utils.getString(getApplicationContext(), R.string.notification_title))
                        .addAction(R.drawable.ic_notification_settings, Utils.getString(getApplicationContext(), R.string.notification_settings), appPendingIntent);

        return mBuilder.build();
    }
}
