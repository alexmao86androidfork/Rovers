package com.schiztech.rovers.app.roverhosts;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.schiztech.rovers.app.activities.AddRoverActivity;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.roveritems.ExecutableRover;
import com.schiztech.rovers.app.roveritems.FolderRover;
import com.schiztech.rovers.app.roveritems.IRover;
import com.schiztech.rovers.app.roveritems.RoversManager;
import com.schiztech.rovers.app.ui.RoverView;
import com.schiztech.rovers.app.ui.handlers.DragAndDropHandler;
import com.schiztech.rovers.app.ui.listeners.SwipeDismissTouchListener;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.PrefUtils;
import com.schiztech.rovers.app.utils.RoverlyticsUtils;
import com.schiztech.rovers.app.utils.RoversUtils;
import com.schiztech.rovers.app.utils.Utils;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by schiz_000 on 6/5/2014.
 */
public abstract class HostManagerBase implements RoversManager.OnChangeListener {
    private static final String TAG = LogUtils.makeLogTag("HostManagerBase");

    protected static final int ROVER_ITEM_PLACEHOLDER_MULTIPLIER = 4;

    //the whole damn layout xml

    int mTriggerSize;
    int mRoverSize;
    //the linear layout that contains the scrollview
    View mParentView;
    ViewGroup mParentLayout;
    View mScrollLayout;
    ViewGroup mScrollContainerLayout;
    ViewGroup mHostLayout;
    RoversManager mRoversManager;
    Context mContext;
    Utils.Corner mCurrentCorner;

    long mOpeningTime = Long.MAX_VALUE;
    long mEditModeTime = Long.MAX_VALUE;
    static final int MAX_OPENED_TIME_REPORT = 60 * 1000;// 1 minute

    public HostManagerBase(Context context) {
        mContext = context;
    }

    protected Context getContext() {
        return mContext;
    }

    public View getParentView() {
        return mParentView;
    }

    public Utils.Corner getCurrentCorner() {
        return mCurrentCorner;
    }

    // region Loading/Unloading methods
    //region Preload Layout
    private static View sPreloadedLayout;

    public static void preloadLayoutView(Context context, int layout) {
        LayoutInflater inflater = LayoutInflater.from(context);
        sPreloadedLayout = inflater.inflate(layout, null, false);
    }
    //endregion Preload Layout

    public void init() {
        LogUtils.LOGV(TAG, "init");
        RoverlyticsUtils.addLaunch(getContext());

        mTriggerSize = RoversUtils.getTriggerSize(getContext());
        mRoverSize = RoversUtils.getRoverSize(getContext());
        mCurrentCorner = getPrefsCurrentCorner();

        if (sPreloadedLayout == null) {//make sure layout is loaded.
            preloadLayoutView(getContext(), getHostLayoutResource());
        }
        mParentView = sPreloadedLayout;
        sPreloadedLayout = null; //empty preloaded view for next reloads.


        mParentLayout = (ViewGroup) mParentView.findViewById(R.id.roversHost_parentLayout);
        mScrollLayout = mParentView.findViewById(R.id.roversHost_scrollLayout);
        mScrollContainerLayout = (ViewGroup) mParentView.findViewById(R.id.roversHost_scrollContainer);
        mHostLayout = (ViewGroup) mParentView.findViewById(R.id.roversHost_itemsLayout);


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mParentLayout.setLayoutParams(params);

        setGravity(mParentLayout, mCurrentCorner);
        ((LinearLayout) mScrollContainerLayout).setGravity(getSecondaryGravity(mCurrentCorner));


        mParentLayout.setOnTouchListener(mRoverPlaceHolderTouch);
        mScrollContainerLayout.setOnTouchListener(mRoverPlaceHolderTouch);

        addAddRoverButton();
        addPlaceholder();
    }

    public void attach() {
        mOpeningTime = System.currentTimeMillis();
        mRoversManager = RoversManager.getInstance(getContext());
        mRoversManager.addOnChangeListener(this);
        mRoversManager.init();

    }

    public void detach(final Utils.AnimationFinishedListener listener) {
        removeAddRoverButton();
        removeAllChildren(new Utils.AnimationFinishedListener() {
                              @Override
                              public void onAnimationFinished() {
                                  if (listener != null)
                                      listener.onAnimationFinished();
                                  removeAddRoverButton();
                                  removePlaceholder();
                                  mHostLayout.removeAllViews();
                                  ViewParent parent = mParentLayout.getParent();
                                  ((ViewGroup) parent).removeView(mParentView);
                                  LogUtils.LOGV(TAG, "Host is detached");
                              }
                          }
        );
        mEmptyTouchListener = null;
        mRoversManager.removeOnChangeListener(this);
        mParentLayout.setOnTouchListener(null);
        mScrollContainerLayout.setOnTouchListener(null);


        if (mOpeningTime != Long.MAX_VALUE) {
            long timePassed = System.currentTimeMillis() - mOpeningTime;
            timePassed = Math.min(MAX_OPENED_TIME_REPORT, timePassed);//check that time passed lower than maximum
            RoverlyticsUtils.addTime(getContext(), timePassed / 1000.0f);
            LogUtils.LOGD(TAG, "Host launched for " + (timePassed / 1000.0f) + "seconds");
            mOpeningTime = Long.MAX_VALUE;
        }

    }

    //endregion Loading/Unloading methods

    //region Layout Params methods
    //abstract protected void getPlaceholderLayoutParams();

    protected void setGravity(ViewGroup view, Utils.Corner currentCorner) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();

        int gravity = getPrimaryGravity(currentCorner);

        params.gravity = gravity;
        ((LinearLayout) view).setGravity(gravity);


    }

    abstract protected int getPrimaryGravity(Utils.Corner currentCorner);

    abstract protected int getSecondaryGravity(Utils.Corner currentCorner);

    abstract public int getRequiredWidthSize(int triggerSize);

    abstract public int getRequiredHeightSize(int triggerSize);

    abstract public int getRequiredYPosition(int triggerSize);

    abstract public int getRequiredXPosition(int triggerSize);

    //endregion Layout Params methods

    //region Shared Prefs Methods

    private Utils.Corner getPrefsCurrentCorner() {

        Point screenDims = Utils.getDisplayDimensions(getContext());
        Point currentLocation;

        int x = (int) (screenDims.x * PrefUtils.getRoverWindowXValue(getContext()));
        x += mTriggerSize / 2;
        int y = (int) (screenDims.y * PrefUtils.getRoverWindowYValue(getContext()));
        y += mTriggerSize / 2;
        currentLocation = new Point(x, y);

        Utils.Corner corner = Utils.getExpandCorner(getContext(), currentLocation);
        LogUtils.LOGD(TAG, "Host Corner: " + corner);
        return corner;

    }

    //endregion Shared Prefs Methods

    //region Children Management Methods

    protected abstract int getRoverItemPlaceholderSize(int roverSize);

    /**
     * removes the view from the host & removes also the IRover from the folder
     *
     * @param view
     */
    private void removeAndDeleteChild(View view) {
        int position = mHostLayout.indexOfChild(view);
        if (position != -1) {
            mRoversManager.removeChildFromCurrentFolder(getOriginalPosition(position));
        }
    }

    private void removeChildImmediate(View view) {

        if (view instanceof RoverView) {
            ((RoverView) view).clearRoverView();
        }
        mHostLayout.removeView(view);


        if (mHostLayout.getChildCount() == 0) {//makes sure the layout isn't empty
            addPlaceholder();
        }
    }

    /**
     * Removes the view from the host only.
     *
     * @param view
     */
    private void removeChild(final View view, Utils.AnimationFinishedListener listener) {
        if (view instanceof RoverView) {//don't remove any view that is not RoverView
            if (mIsEditMode) {
                ((RoverView) view).cancelShake();

            }
            ((RoverView) view).setTouchActionsListener(null);
            ((RoverView) view).setTouchHandlerEnabled(false);
            view.setEnabled(false);
            animateOut(view, listener);
        }

    }

    private void addChild(IRover item, final int position) {
        if (mHostLayout.getChildCount() == 1 && !(mHostLayout.getChildAt(0) instanceof RoverView)) {
            removePlaceholder();
        }

        View view = roverToView(item);


        mHostLayout.addView(view, isItemsOrderReversed() ? mHostLayout.getChildCount() - position : position);
        mParentLayout.requestLayout();
        mHostLayout.requestLayout();

        animateIn(view, position);


        if (mIsEditMode) {
            animateAllChildren();
        }


    }

    private View roverToView(IRover rover) {
        RoverView roverView = new RoverView.Builder(getContext(), getItemLayoutResource())
                .setSizeAdjustableToTrigger(true)
                .setRover(rover)
                .setOnTouchActionsListener(mOnRoverTouchActionsListener)
                .enableTouchHandler(true)
                .setPlaceholder(getRoverItemPlaceholderSize(mRoverSize), getRoverPlaceholderDirection(), mRoverPlaceHolderTouch)
                .build();

        roverView.setSwipeToDismissListener(getSwipeToDismissListener(roverView));
        roverView.setDragAndDropHandler(getDragAndDropHandler(roverView, roverView.getCircleView(), mHostLayout));

        return roverView;
    }

    private View.OnTouchListener mRoverPlaceHolderTouch = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (mEmptyTouchListener != null) {
                mEmptyTouchListener.onEmptyTouch();
            }

            return false;
        }
    };

    private void removeAllChildren(Utils.AnimationFinishedListener listener) {
        int childCount = mHostLayout.getChildCount();
        List<View> oldChildren = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            if (mHostLayout.getChildAt(i) instanceof RoverView) {
                oldChildren.add(mHostLayout.getChildAt(i));
            }
        }

        for (int i = 0; i < oldChildren.size(); i++) {
            if ((isItemsOrderReversed() && i == oldChildren.size() - 1)
                    || (!isItemsOrderReversed() && i == 0))
                removeChild(oldChildren.get(i), listener);
            else
                removeChild(oldChildren.get(i), null);
        }

        if (oldChildren.size() == 0 && listener != null) {
            listener.onAnimationFinished();
        }
    }

    private void addChildrenList(List<IRover> rovers) {
        for (int i = 0; i < rovers.size(); i++) {
            addChild(rovers.get(i), i);
        }
    }


    //endregion Children Management Methods

    //region Layout Resources Methods

    protected abstract int getHostLayoutResource();

    protected abstract int getItemLayoutResource();
    //endregion Layout Resources Methods

    //region Touch Listeners

    //region mOnRoverChildClickListener
    private RoverView.OnTouchActionsListener mOnRoverTouchActionsListener = new
            RoverView.OnTouchActionsListener() {
                @Override
                public void onSingleTap(View view, MotionEvent e) {
                    if (isAnimating()) return; //don't do anything while animating.

                    if (mIsEditMode == true) {
                        cancelEditMode();
                    } else {
                        // get the clicked view position
                        int position = mHostLayout.indexOfChild(view);
                        if (position == -1) return;//view's position wasn't found
                        position = getOriginalPosition(position); // fix position if order is reversed

                        //make sure position is valid and in bounds
                        if(position >= mRoversManager.getCurrentFolderChildren().size())
                            return;

                        //get the rover connected to the clicked view
                        IRover roverItem = mRoversManager.getCurrentFolderChildren().get(position);

                        //launch the action
                        if (roverItem instanceof ExecutableRover) {
                            if (RoversManager.launchRover((ExecutableRover) roverItem, getContext()) && mRoverLaunchListener != null) {
                                mRoverLaunchListener.onRoverLaunch(roverItem);
                            }
                        } else if (roverItem instanceof FolderRover) {
                            mRoversManager.navigateToFolder((FolderRover) roverItem);
                        }
                    }
                }

                @Override
                public void onLongPressDown(View view, MotionEvent e) {
                    Utils.Vibrate(getContext(), Utils.VIBRATE_SHORT);

                    startEditMode();
                }

                @Override
                public void onLongPressUp(View view, MotionEvent e) {

                }

                @Override
                public boolean canDismiss(Object token) {
                    return mIsEditMode;
                }

                @Override
                public void onSwipeStarted(View view) {
                    if (view instanceof RoverView) {
                        ((RoverView) view).markPressed(true);
                        ((RoverView) view).showOverlay(RoverView.OverlayType.Delete);
                    }
                }

                @Override
                public void onSwipeFinished(View view) {
                    if (view instanceof RoverView) {
                        ((RoverView) view).markPressed(false);
                        ((RoverView) view).hideOverlay();
                    }
                }

                @Override
                public void onDismiss(View v, Object token) {
                    v.setVisibility(View.GONE);
                    if (v instanceof RoverView) {
                        ((RoverView) v).cancelShake();
                    }

                    removeAndDeleteChild(v);
                }

                @Override
                public void onDragAndDropStarted(ViewGroup container, View view, int originalPosition) {
                    if (view instanceof RoverView) {
                        ((RoverView) view).showOverlay(RoverView.OverlayType.Drag);
                    }
                }

                @Override
                public void onDragAndDropFinished(ViewGroup container, View view, int originalPosition, int droppedPosition) {
                    if (view instanceof RoverView) {
                        ((RoverView) view).hideOverlay();
                    }
                    if (originalPosition != droppedPosition) {
                        mRoversManager.repositionChildInCurrentFolder(getOriginalPosition(originalPosition), getOriginalPosition(droppedPosition));
                    }
                }

                @Override
                public boolean canDragAndDrop() {
                    return mIsEditMode;
                }
            };


    //endregion mOnRoverChildClickListener

    protected abstract SwipeDismissTouchListener getSwipeToDismissListener(View dismissableView);

    protected abstract DragAndDropHandler getDragAndDropHandler(View draggedView, View shadowView, ViewGroup containerView);

    //endregion

    //region Scroll Methods

    protected enum scrollType {START, END}

    protected abstract void scrollEnd();

    protected abstract void scrollStart();

    protected void manageScroll(final scrollType type) {
        mScrollLayout.post(new Runnable() {

            @Override
            public void run() {
                if (type == scrollType.START)
                    scrollStart();
                else
                    scrollEnd();
            }
        });

    }

    //endregion Scroll Methods

    //region Edit Mode

    protected boolean mIsEditMode = false;

    public void startEditMode() {
        mEditModeTime = System.currentTimeMillis();
        mIsEditMode = true;

        animateAllChildren();

        //show add button on edit mode if not visible before
        if (PrefUtils.getAddOnEditOnlyValue(getContext())) {
            showAddRoverButton();
        }

        if (mEditModeListener != null)
            mEditModeListener.onEditModeStarted();//todo change to directional size
    }

    public void cancelEditMode() {
        if (mEditModeTime != Long.MAX_VALUE) {//remove the time of edit mode from open time.
            long editModeTotalTime = System.currentTimeMillis() - mEditModeTime;
            mOpeningTime += editModeTotalTime;
            mEditModeTime = Long.MAX_VALUE;//reset edit mode time.
        }
        mIsEditMode = false;

        cancelAnimationAllChildren();

        //hide add button when edit mode is finished if defined 'add on edit mode only'
        if (PrefUtils.getAddOnEditOnlyValue(getContext())) {
            //of course, if folder is empty dont hide the add button
            if (mRoversManager.getCurrentFolderChildren().size() != 0) {
                hideAddRoverButton();
            }
        }

        if (mEditModeListener != null)
            mEditModeListener.onEditModeFinished();
    }

    public void setEditModeListener(EditModeListener listener) {
        mEditModeListener = listener;
    }

    protected EditModeListener mEditModeListener;

    public interface EditModeListener {
        void onEditModeStarted();


        void onEditModeFinished();
    }

    //endregion Edit Mode

    //region Animation Methods

    protected void animateAllChildren() {

        cancelAnimationAllChildren();

        for (int i = 0; i < mHostLayout.getChildCount(); i++) {
            if (mHostLayout.getChildAt(i) instanceof RoverView) {
                ((RoverView) mHostLayout.getChildAt(i)).animateShake();
            }
        }
    }

    protected void cancelAnimationAllChildren() {
        for (int i = 0; i < mHostLayout.getChildCount(); i++) {
            if (mHostLayout.getChildAt(i) instanceof RoverView) {
                ((RoverView) mHostLayout.getChildAt(i)).cancelShake();
            }
        }
    }


    //endregion Animation Methods

    //region RoversManager.OnChangeListener Methods
    @Override
    public void onRoversFolderChanged(IRover item) {
        if (mHostLayout == null || getContext() == null) return;

        hideAddRoverButton();

        if (mHostLayout.getChildCount() > 0) {
            removeAllChildren(new Utils.AnimationFinishedListener() {
                                  @Override
                                  public void onAnimationFinished() {
                                      if (mFolderChangedListener != null) {
                                          mFolderChangedListener.onFolderChanged(mRoversManager.getCurrentFolder(), mRoversManager.isCurrentFolderRoot());
                                      }
                                      addChildrenList(mRoversManager.getCurrentFolderChildren());
                                      manageScroll(scrollType.START);
                                  }
                              }
            );

        } else {
            addChildrenList(mRoversManager.getCurrentFolderChildren());
            manageScroll(scrollType.START);
        }

        //make sure the add button shows when root is empty
        if (mRoversManager.getCurrentFolderChildren().size() == 0 && mRoversManager.isCurrentFolderRoot()) {
            showAddRoverButton();
        }


    }

    @Override
    public void onRoverAdded(IRover item, int position) {
        addChild(item, position);
        manageScroll(scrollType.END);
    }

    @Override
    public void onRoverRemoved(IRover item, int position) {
        View v = mHostLayout.getChildAt(getOriginalPosition(position));
        removeChildImmediate(v);

        if (mRoversManager.getCurrentFolderChildren().size() == 0)
            showAddRoverButton();
    }

    @Override
    public void onRoverRepositioned(IRover item, int oldPosition, int newPosition) {
        LogUtils.LOGD(TAG, "item moved from " + oldPosition + " to " + newPosition);
    }

    //endregion RoversManager.OnChangeListener Methods

    //region Empty Touch Methods

    public interface EmptyTouchListener {
        void onEmptyTouch();
    }

    private EmptyTouchListener mEmptyTouchListener;

    public void setEmptyTouchListener(EmptyTouchListener listener) {
        mEmptyTouchListener = listener;
    }

    //endregion Touch Outside Methods

    //region Animations

    public boolean isAnimating() {
        return mIsAnimatingIn || mIsAnimatingOut;
    }

    protected final static int ANIMATION_GAP_TIME = 45;

    private boolean mIsAnimatingIn = false;

    protected void animateIn(View view, int position) {
        mIsAnimatingIn = true;
        int delay = ANIMATION_GAP_TIME * (position);
        boolean isLast = mRoversManager.getCurrentFolderChildren().size() == position + 1;
        Utils.AnimationFinishedListener listener = null;

        if (isLast) {
            listener = new Utils.AnimationFinishedListener() {
                @Override
                public void onAnimationFinished() {
                    if (!PrefUtils.getAddOnEditOnlyValue(getContext())) {
                        showAddRoverButton();
                    }
                    mIsAnimatingIn = false;//done animating;
                    System.gc();//called to prevent multi GC_FOR_ALLOC calls which delays the ui. handles the gc cleanup right before the rovers are showing.
                }
            };
        }


        ((RoverView) view).animateIn(delay, 1, listener);


    }

    private boolean mIsAnimatingOut = false;

    protected void animateOut(final View view, final Utils.AnimationFinishedListener listener) {
        mIsAnimatingOut = true;
        int position = mHostLayout.indexOfChild(view);
        int delay = ANIMATION_GAP_TIME * (isItemsOrderReversed() ? position : (mHostLayout.getChildCount() - position));
        delay = Math.abs(delay);

        ((RoverView) view).animateOut(delay, 1000f, new Utils.AnimationFinishedListener() {
            @Override
            public void onAnimationFinished() {
                if (view instanceof RoverView) {
                    ((RoverView) view).clearRoverView();
                }
                mHostLayout.removeView(view);

                if (mHostLayout.getChildCount() == 0) {
                    mIsAnimatingOut = false;
                    addPlaceholder();
                    if (mRoversManager.getCurrentFolderChildren().size() == 0)
                        showAddRoverButton();

                    System.gc();//called to prevent multi GC_FOR_ALLOC calls which delays the ui. handles the gc cleanup right before the rovers are showing.
                }
                if (listener != null)
                    listener.onAnimationFinished();
            }
        });

    }


    //endregion Animations

    //region Placeholder
    protected void addPlaceholder() {
        View placeholder;

        try {
            placeholder = new View(getContext());
            placeholder.setId(R.id.host_placeholder);
            placeholder.setLayoutParams(new ViewGroup.LayoutParams(getPlaceHolderWidth(), getPlaceHolderHeight()));
            placeholder.setBackgroundColor(0x01000000);
            mHostLayout.addView(placeholder);

        } catch (Exception e) {
            LogUtils.LOGW(TAG, "Could not add placeholder view");
        }


    }

    protected abstract int getPlaceHolderWidth();

    protected abstract int getPlaceHolderHeight();

    protected void removePlaceholder() {
        View placeholder = mHostLayout.findViewById(R.id.host_placeholder);
        if (placeholder != null) {
            mHostLayout.removeView(placeholder);
        } else {
            LogUtils.LOGW(TAG, "Tried to remove placeholder, which does not exist");
        }
    }

    protected abstract Utils.Direction getRoverPlaceholderDirection();

    //endregion Placeholder

    //region Back event

    public boolean navigateBack() {
        if (isAnimating()) return true;//dont do anything is still animating...

        if (mRoversManager.isCurrentFolderLastInStack() || mRoversManager.isCurrentFolderRoot())
            return false;

        return mRoversManager.navigateBack();
    }

    public void navigateToRoot() {
        mRoversManager.navigateToRoot();
    }

    //endregion Back event

    //region Rover Launch Event

    public interface RoverLaunchListener {
        void onRoverLaunch(IRover rover);
    }

    private RoverLaunchListener mRoverLaunchListener;

    public void setRoverLaunchListener(RoverLaunchListener listener) {
        mRoverLaunchListener = listener;
    }

    //endregion Rover Launch Event

    //region FolderChanged Listener

    public void setFolderChangedListener(FolderChangedListener listener) {
        mFolderChangedListener = listener;
    }

    protected FolderChangedListener mFolderChangedListener;

    public interface FolderChangedListener {
        void onFolderChanged(FolderRover folder, boolean isRoot);
    }

    //endregion FolderChanged Listener

    //region Items Order
    abstract protected boolean isItemsOrderReversed();


    private int getOriginalPosition(int position) {
        return isItemsOrderReversed() ? mHostLayout.getChildCount() - position - 1 : position;
    }
    //endregion Items Order

    //region Add Rover
    public static final float ADD_ROVER_ALPHA = 0.4f;
    public static final float ADD_ROVER_SCALE = 0.7f;

    protected abstract int getAddRoverButtonLayout();

    protected void addAddRoverButton() {
        RoverView addRover = new RoverView.Builder(getContext(), getAddRoverButtonLayout())
                .setSize(Math.min(RoversUtils.getRoverDefaultSize(mContext), RoversUtils.getRoverSize(mContext)), true)
                        .build();
        addRover.setIconResource(R.drawable.ic_action_add);
        addRover.setCircleColor(0xffffffff);

        addRover.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, getSecondaryGravity(mCurrentCorner)));
        if (addRover.getCircleView() != null) {
            addRover.getCircleView().setOnClickListener(mAddRoverClick);
        }
        addRover.setScaleCircle(ADD_ROVER_SCALE);
        addRover.setAlpha(ADD_ROVER_ALPHA);
        addRover.setId(R.id.host_addrover);
        addRover.setPlaceHolder(getRoverItemPlaceholderSize(mRoverSize), getRoverPlaceholderDirection(), mRoverPlaceHolderTouch);
        addRover.setVisibility(View.INVISIBLE);

        if (PrefUtils.getMiscMuteClickSoundValue(getContext())) {
            addRover.setSoundEffectsEnabled(false);
        }

        try {
            if (isItemsOrderReversed())
                mScrollContainerLayout.addView(addRover, 0);
            else
                mScrollContainerLayout.addView(addRover);
        } catch (Exception e) {
            LogUtils.LOGW(TAG, "Could not add addrover view");
        }
    }

    protected void showAddRoverButton() {
        View addRover = mScrollContainerLayout.findViewById(R.id.host_addrover);
        if (addRover != null) {
            if (addRover.getVisibility() != View.VISIBLE) {
                ((RoverView) addRover).animateIn(ANIMATION_GAP_TIME, ADD_ROVER_SCALE, null);
                addRover.setVisibility(View.VISIBLE);
            }
        }
    }

    protected void hideAddRoverButton() {
        View addRover = mScrollContainerLayout.findViewById(R.id.host_addrover);
        if (addRover != null) {
            addRover.setVisibility(View.INVISIBLE);
        }
    }

    protected void removeAddRoverButton() {
        View addRover = mScrollContainerLayout.findViewById(R.id.host_addrover);
        if (addRover != null) {
            addRover.setOnClickListener(null);

            if (addRover instanceof RoverView) {
                if (((RoverView) addRover).getCircleView() != null) {
                    ((RoverView) addRover).getCircleView().setOnClickListener(null);
                }
                ((RoverView) addRover).clearRoverView();
                ((RoverView) addRover).setTouchActionsListener(null);
                addRover.setEnabled(false);
            }
            mScrollContainerLayout.removeView(addRover);

        }
    }

    protected View.OnClickListener mAddRoverClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                mRoverLaunchListener.onRoverLaunch(null);
                Intent intent = new Intent(getContext(), AddRoverActivity.class);
                intent.putExtra(AddRoverActivity.ROVERS_COUNT_KEY, mRoversManager.countRoot());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
            catch (Exception e){
                Crashlytics.getInstance().core.logException(e);
            }
        }
    };


    //endregion


}
