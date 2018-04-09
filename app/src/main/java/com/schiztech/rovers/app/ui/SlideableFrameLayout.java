package com.schiztech.rovers.app.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by schiz_000 on 10/17/2014.
 */
public class SlideableFrameLayout extends FrameLayout {

    //region c'tors
    public SlideableFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SlideableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideableFrameLayout(Context context) {
        super(context);
    }
//endregion c'tors

    //region XFraction

    public float getXFraction() {
        return getX() / getWidth();
    }

    public void setXFraction(float xFraction) {
        final int width = getWidth();
        setX((width > 0) ? (xFraction * width) : -9999);
    }


    //endregion XFraction
}
