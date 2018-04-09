package com.schiztech.rovers.app.ui.handlers;

import android.content.ClipData;
import android.content.ClipDescription;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.Utils;

/**
 * Created by schiz_000 on 6/11/2014.
 */
public abstract class DragAndDropHandler  implements  View.OnDragListener{

    protected View mView;
    protected View mShadowView;
    protected int mViewLength = 1;
    protected ViewGroup mContainer;
    protected DragAndDropCallbacks mCallbacks;
    protected int mOriginalPosition;

    public DragAndDropHandler(View draggedView, View shadowView, ViewGroup container){
        mView = draggedView;
        mShadowView = shadowView;
        mContainer = container;
    }

    //region Callbacks

    /**
     * The callback interface used by {@link DragAndDropHandler} to inform its client
     * about a drag and drop action of the view for which it was created.
     */
    public interface DragAndDropCallbacks {
        /**
         * Called to determine whether the view can be dismissed.
         */
        boolean canDragAndDrop();

        /**
         * Called to indicate that the view started swiping
         * @param view The originating {@link View} .
         */
        void onDragAndDropStarted(ViewGroup container, View view, int originalPosition);


        /**
         * Called to indicate that the view finished swiping
         * @param view The originating {@link View} .
         */
        void onDragAndDropFinished(ViewGroup container, View view, int originalPosition, int droppedPosition);

    }

    public void setCallbacksListener(DragAndDropCallbacks callbacks){
        mCallbacks = callbacks;
    }

    //endregion Callbacks

    //region Is Dragging

    protected boolean pDragging;

    protected void setDragging(boolean isDragging){
        boolean oldValue = pDragging;
        pDragging = isDragging;

        if(pDragging != oldValue) {//only announce on new value of the swiping flag
            if (pDragging) {
                Utils.Vibrate(mView.getContext(), Utils.VIBRATE_MINIMAL);
            }
        }
    }

    public boolean isDragging(){return pDragging;}

    //endregion Is Dragging

    //region Directional Methods
    abstract protected float getDragEventLength(DragEvent event);
    abstract protected int getViewLength(View view);

    //endregion Directional Methods

    protected int getViewPosition(){
        return mContainer.indexOfChild(mView);
    }

    public void startDragAndDrop() {
        if (mViewLength < 2) {
            mViewLength = getViewLength(mView);
        }
        setDragging(true);
        mContainer.setOnDragListener(this);
        mView.setTag("DraggedObject");

        ClipData.Item item = new ClipData.Item((CharSequence)mView.getTag());

        String[] mimeTypes = { ClipDescription.MIMETYPE_TEXT_PLAIN };
        ClipData data = new ClipData(mView.getTag().toString(), mimeTypes, item);
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(mShadowView);

        mOriginalPosition = getViewPosition();
        mCallbacks.onDragAndDropStarted(mContainer, mView, mOriginalPosition);

        mView.startDrag(data, //data to be dragged
                shadowBuilder, //drag shadow
                mView, //local data about the drag and drop operation
                0   //no needed flags
        );


        mView.setVisibility(View.INVISIBLE);

    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        if(event == null || v == null) return true;

        // Handles each of the expected events
        switch (event.getAction()) {

            //signal for the start of a drag and drop operation.
            case DragEvent.ACTION_DRAG_STARTED:

                break;

            //the drag point has entered the bounding box of the View
            case DragEvent.ACTION_DRAG_ENTERED:

                break;

            //the user has moved the drag shadow outside the bounding box of the View
            case DragEvent.ACTION_DRAG_EXITED:
                // Cancel listview's touch

                break;

            case DragEvent.ACTION_DRAG_LOCATION:
                View view = (View) event.getLocalState();

                int index = (int)(getDragEventLength(event) / mViewLength);

                ViewGroup owner = (ViewGroup) view.getParent();
                if(owner != null) {
                    owner.removeView(view);
                }
                LinearLayout container = (LinearLayout) v;
                if(container != null) {
                    index = Math.min(container.getChildCount(), index);
                    container.addView(view, index);
                }
                break;
            //drag shadow has been released,the drag point is within the bounding box of the View
            case DragEvent.ACTION_DRAG_ENDED:
                mView.setTag(null);//delete tag
                mContainer.setOnDragListener(null);
                View droppedView = (View) event.getLocalState();
                droppedView.setVisibility(View.VISIBLE);
                setDragging(false);
                mCallbacks.onDragAndDropFinished(mContainer, mView, mOriginalPosition, getViewPosition());

                break;

        }
        return true;
    }


}
