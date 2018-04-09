package com.schiztech.rovers.app.roverhosts;

import android.content.Context;
import android.graphics.Point;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.ui.handlers.DragAndDropHandler;
import com.schiztech.rovers.app.ui.handlers.DragAndDropHandlerVertical;
import com.schiztech.rovers.app.ui.listeners.SwipeDismissTouchListenerHorizontal;
import com.schiztech.rovers.app.ui.listeners.SwipeDismissTouchListener;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.Utils;

import wei.mark.standout.StandOutWindow;

/**
 * Created by schiz_000 on 6/5/2014.
 */
public class HostManagerVertical extends HostManagerBase {
    private static final String TAG = LogUtils.makeLogTag("HostManagerVertical");

    private static HostManagerVertical sInstance;

    private HostManagerVertical(Context context) {
        super(context);
    }


    public static HostManagerBase getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new HostManagerVertical(context);
        }

        return sInstance;
    }

    //region Layout Params methods




    @Override
    protected int getPrimaryGravity(Utils.Corner currentCorner){
        return (currentCorner == Utils.Corner.LeftBottom || currentCorner == Utils.Corner.RightBottom) ?
                Gravity.BOTTOM : Gravity.TOP;
    }

    @Override
    protected int getSecondaryGravity(Utils.Corner currentCorner){
        return (currentCorner == Utils.Corner.LeftBottom || currentCorner == Utils.Corner.LeftTop) ?
                Gravity.LEFT : Gravity.RIGHT;
    }

    @Override
    public int getRequiredWidthSize(int triggerSize) {
        return StandOutWindow.StandOutLayoutParams.WRAP_CONTENT;
    }

    @Override
    public int getRequiredHeightSize(int triggerSize) {
        return Utils.getDisplayDimensions(getContext()).y - triggerSize;
    }

    @Override
    public int getRequiredYPosition(int triggerSize) {
        return
                (Utils.isCornerTop(mCurrentCorner)) ?
                        triggerSize : 0;
    }

    @Override
    public int getRequiredXPosition(int triggerSize) {
        return
                (Utils.isCornerLeft(mCurrentCorner)) ?
                        StandOutWindow.StandOutLayoutParams.LEFT : StandOutWindow.StandOutLayoutParams.RIGHT;
    }

    //endregion Layout Params methods

    //region Layout ResourcesMethods
    //region Layout ResourcesMethods
    public static int getStaticHostLayoutResource() {
        return R.layout.rovers_host_vertical;
    }

    @Override
    protected int getHostLayoutResource() {
        return getStaticHostLayoutResource();
    }

    @Override
    protected int getItemLayoutResource() {
        return R.layout.rover_item_vertical;
    }


    //endregion Layout ResourcesMethods

    //region Swipe To Dismiss

    @Override
    protected SwipeDismissTouchListener getSwipeToDismissListener(View dismissableView) {
        return new SwipeDismissTouchListenerHorizontal(
                dismissableView, null);
    }
    protected DragAndDropHandler getDragAndDropHandler(View draggedView, View shadowView, ViewGroup containerView) {
        return new DragAndDropHandlerVertical(draggedView, shadowView, containerView);
    }


    //endregion Swipe To Dismiss

    //region Scroll Methods

    @Override
    protected void scrollEnd() {
        ScrollView scrollView = (ScrollView)mScrollLayout;
        if(Utils.isCornerBottom(mCurrentCorner)){
            scrollView.fullScroll(View.FOCUS_UP);
        }
        else{
            scrollView.fullScroll(View.FOCUS_DOWN);
        }
    }

    @Override
    protected void scrollStart() {
        try {
            ScrollView scrollView = (ScrollView) mScrollLayout;
            if (Utils.isCornerBottom(mCurrentCorner)) {
                scrollView.fullScroll(View.FOCUS_DOWN);
            } else {
                scrollView.fullScroll(View.FOCUS_UP);
            }
        }
        catch (ClassCastException e){
            LogUtils.LOGE(TAG,"Error while scrollStart - ClassCast: " + e.getMessage());
        }
    }

    //endregion Scroll Methods

    //region Animations

    @Override
    protected boolean isItemsOrderReversed() {
        return Utils.isCornerBottom(mCurrentCorner);
    }
    //endregion Animations

    //region Placeholder

    @Override
    protected Utils.Direction getRoverPlaceholderDirection() {
        return (Utils.isCornerLeft(mCurrentCorner) )?
                Utils.Direction.Right : Utils.Direction.Left;
    }

    @Override
    protected int getPlaceHolderHeight() {
        return 1;
    }

    @Override
    protected int getPlaceHolderWidth() {
        return (int)(mRoverSize*1.6);
    }

    //endregion Placeholder

    //region AddRover

    @Override
    protected int getAddRoverButtonLayout() {
        return R.layout.rover_item_vertical;
    }

    //endregion


    @Override
    protected int getRoverItemPlaceholderSize(int roverSize) {
        Point dimens = Utils.getDisplayDimensions(getContext());
        //placeholder
        return Math.min(dimens.x - roverSize, roverSize * ROVER_ITEM_PLACEHOLDER_MULTIPLIER);
    }
}
