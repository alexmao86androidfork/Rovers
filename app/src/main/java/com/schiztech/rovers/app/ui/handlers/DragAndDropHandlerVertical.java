package com.schiztech.rovers.app.ui.handlers;

import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by schiz_000 on 6/11/2014.
 */
public class DragAndDropHandlerVertical extends DragAndDropHandler {

    public DragAndDropHandlerVertical(View draggedView, View shadowView, ViewGroup container) {
        super(draggedView, shadowView, container);
    }

    @Override
    protected float getDragEventLength(DragEvent event) {
        return event.getY();
    }

    @Override
    protected int getViewLength(View view) {
        return view.getHeight();
    }
}
