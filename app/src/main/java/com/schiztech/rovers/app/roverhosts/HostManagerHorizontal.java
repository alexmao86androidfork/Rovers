package com.schiztech.rovers.app.roverhosts;

import android.content.Context;
import android.graphics.Point;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.crashlytics.android.Crashlytics;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.ui.handlers.DragAndDropHandler;
import com.schiztech.rovers.app.ui.handlers.DragAndDropHandlerHorizontal;
import com.schiztech.rovers.app.ui.listeners.SwipeDismissTouchListener;
import com.schiztech.rovers.app.ui.listeners.SwipeDismissTouchListenerVertical;
import com.schiztech.rovers.app.utils.Utils;

import wei.mark.standout.StandOutWindow;

/**
 * Created by schiz_000 on 6/5/2014.
 */
public class HostManagerHorizontal extends HostManagerBase {

    private static HostManagerHorizontal sInstance;

    private HostManagerHorizontal(Context context) {
        super(context);
    }



    //region Layout Params methods



    @Override
    protected int getPrimaryGravity(Utils.Corner currentCorner){
        return (currentCorner == Utils.Corner.LeftBottom || currentCorner == Utils.Corner.LeftTop) ?
                Gravity.LEFT : Gravity.RIGHT;
    }

    @Override
    protected int getSecondaryGravity(Utils.Corner currentCorner){
        return (currentCorner == Utils.Corner.LeftBottom || currentCorner == Utils.Corner.RightBottom) ?
                Gravity.BOTTOM : Gravity.TOP;
    }

    @Override
    public int getRequiredWidthSize(int triggerSize) {
        return Utils.getDisplayDimensions(getContext()).x - triggerSize;
    }

    @Override
    public int getRequiredHeightSize(int triggerSize) {
        return StandOutWindow.StandOutLayoutParams.WRAP_CONTENT;
    }

    @Override
    public int getRequiredYPosition(int triggerSize) {
        return
                (Utils.isCornerBottom(mCurrentCorner)) ?
                        StandOutWindow.StandOutLayoutParams.BOTTOM : StandOutWindow.StandOutLayoutParams.TOP;
    }

    @Override
    public int getRequiredXPosition(int triggerSize) {
        return
                (Utils.isCornerLeft(mCurrentCorner)) ?
                        triggerSize : 0;
    }


    public static HostManagerBase getInstance(Context context){
        if(sInstance == null) {
            sInstance = new HostManagerHorizontal(context);
        }

        return sInstance;
    }

    //endregion Layout Params methods

    //region Layout ResourcesMethods
    public static int getStaticHostLayoutResource() {
        return R.layout.rovers_host_horizontal;
    }

    @Override
    protected int getHostLayoutResource() {
        return getStaticHostLayoutResource();
    }

    @Override
    protected int getItemLayoutResource() {
        return R.layout.rover_item_horizontal;
    }

    @Override
    protected SwipeDismissTouchListener getSwipeToDismissListener(View dismissableView) {
        return new SwipeDismissTouchListenerVertical(
                dismissableView,
                null);

    }

    protected DragAndDropHandler getDragAndDropHandler(View draggedView, View shadowView, ViewGroup containerView) {
        return new DragAndDropHandlerHorizontal(draggedView, shadowView, containerView);

    }


    //endregion Layout ResourcesMethods

    //region Scroll Methods

    @Override
    protected void scrollEnd() {
        HorizontalScrollView scrollView = (HorizontalScrollView)mScrollLayout;
        if(Utils.isCornerRight(mCurrentCorner)){
            scrollView.fullScroll(View.FOCUS_LEFT);
        }

        else{
            scrollView.fullScroll(View.FOCUS_RIGHT);
        }
    }

    @Override
    protected void scrollStart() {
        try {
            HorizontalScrollView scrollView = (HorizontalScrollView) mScrollLayout;
            if (Utils.isCornerRight(mCurrentCorner)) {
                scrollView.fullScroll(View.FOCUS_RIGHT);
            } else {
                scrollView.fullScroll(View.FOCUS_LEFT);
            }
        }
        catch (Exception e){
            if(e instanceof ClassCastException){
                ScrollView scrollView = (ScrollView) mScrollLayout;
                if (Utils.isCornerRight(mCurrentCorner)) {
                    scrollView.fullScroll(View.FOCUS_RIGHT);
                } else {
                    scrollView.fullScroll(View.FOCUS_LEFT);
                }

            }
            Crashlytics.getInstance().core.logException(e);
        }
    }




    //endregion Scroll Methods

    //region Animations

    @Override
    protected boolean isItemsOrderReversed() {
        return Utils.isCornerRight(mCurrentCorner);
    }


    //endregion Animations

    //region Placeholder

    @Override
    protected Utils.Direction getRoverPlaceholderDirection() {
        return (Utils.isCornerBottom(mCurrentCorner) )?
                Utils.Direction.Top : Utils.Direction.Bottom;
    }

    @Override
    protected int getPlaceHolderWidth() {
        return 1;
    }

    @Override
    protected int getPlaceHolderHeight() {
        return (int)(mRoverSize*1.6);
    }
    //endregion Placeholder

    //region AddRover

    @Override
    protected int getAddRoverButtonLayout() {
        return R.layout.rover_item_horizontal;
    }

    //endregion


    @Override
    protected int getRoverItemPlaceholderSize(int roverSize) {
        Point dimens = Utils.getDisplayDimensions(getContext());

        return Math.min(dimens.y - roverSize, roverSize * ROVER_ITEM_PLACEHOLDER_MULTIPLIER);
    }
}


