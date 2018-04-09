package com.schiztech.rovers.app.ui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.Utils;

/**
 * Created by schiz_000 on 1/4/2015.
 */
public class ArcTextView extends View {

    //region  Defaults

    private final float DEFAULT_START_ANGEL = 0;
    private final float DEFAULT_SWEEP_ANGEL = 180;
    private final int DEFAULT_DIAMETER_DP = 100;
    private final String DEFAULT_TEXT = "EXAMPLE";
    private final int DEFAULT_TEXT_COLOR = Color.BLACK;
    private final float DEFAULT_TEXT_SIZE_SP = 28;
    private final String DEFUALT_FONTFAMILY_JELLYBEAN = "sans-serif-light";

    //endregion

    private float mStartAngle;
    private float mSweepAngle;
    private int mDiameter;
    private String mText;
    private int mTextColor;
    private float mTextSize;
    private String mFontFamily;

    // private Paint cPaint;
    private Paint mPaint;


    //region ctors & Init
    public ArcTextView(Context context) {
        super(context);
        init(context, null);
    }

    public ArcTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public ArcTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {


//        cPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        cPaint.setStyle(Paint.Style.STROKE);
//        cPaint.setColor(mTextColor);
//        cPaint.setStrokeWidth(0);

        mStartAngle = DEFAULT_START_ANGEL;
        mSweepAngle = DEFAULT_SWEEP_ANGEL;

        mDiameter = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_DIAMETER_DP, getResources().getDisplayMetrics());;
        mText = DEFAULT_TEXT;
        mTextColor = DEFAULT_TEXT_COLOR;
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE_SP, getResources().getDisplayMetrics());

        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArcTextView);
            mStartAngle = a.getFloat(R.styleable.ArcTextView_start_angle, mStartAngle);
            mSweepAngle = a.getFloat(R.styleable.ArcTextView_sweep_angle, mSweepAngle);
            mDiameter =(int) a.getDimensionPixelSize(R.styleable.ArcTextView_diameter, mDiameter);
            mText = a.getString(R.styleable.ArcTextView_text);
            mTextColor = a.getColor(R.styleable.ArcTextView_text_color, mTextColor);
            mTextSize = a.getDimension(R.styleable.ArcTextView_text_size, mTextSize);
            mFontFamily = a.getString(R.styleable.ArcTextView_font_family);
            a.recycle();
        }

        if(mFontFamily == null) {
            mFontFamily = DEFUALT_FONTFAMILY_JELLYBEAN;
        }

        updatePaint();
    }
    //endregion

    //region Setters

    public void setStartAngle(float startAngle) {
        mStartAngle = startAngle;
        updatePaint();
    }

    public void setSweepAngle(float sweepAngle) {
        mSweepAngle = sweepAngle;
        updatePaint();
    }

    public void setDiameter(int diameter) {
        mDiameter = diameter;
        updatePaint();
    }

    public void setText(String text) {
        mText = text;
        updatePaint();
    }


    public void setTextColor(int textColor) {
        mTextColor = textColor;
        updatePaint();
    }


    public void setFontFamily(String fontFamily) {
        mFontFamily = fontFamily;
        updatePaint();
    }

    public void setTextSize(float textSize) {
        mTextSize = textSize;
        updatePaint();
    }
    //endregion

    //region Getters

    public float getStartAngle() {
        return mStartAngle;
    }

    public float getSweepAngle() {
        return mSweepAngle;
    }

    public int getDiameter() {
        return mDiameter;
    }

    public String getText() {
        return mText;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public String getFontFamily() {
        return mFontFamily;
    }

    public float getTextSize() {
        return mTextSize;
    }
    //endregion

    @Override
    protected void onDraw(Canvas canvas) {
//        float pxDiameter = Utils.convertDpToPixel(mDiameter);
//        LogUtils.LOGE("akka", "diameter: "+ mDiameter);
        RectF oval = new RectF(0, 0, mDiameter, mDiameter);
        Path circle = new Path();
        circle.addArc(oval, mStartAngle, mSweepAngle);//180, -180

        canvas.drawTextOnPath(mText, circle, 0, 0, mPaint);
        invalidate();
    }

    protected void updatePaint() {
        if (mPaint == null)
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(mTextSize);
        if (Utils.isAndroidVersionEqualOrAbove(Build.VERSION_CODES.JELLY_BEAN))
            mPaint.setTypeface(Typeface.create(mFontFamily, Typeface.NORMAL));

        invalidate();
    }


}

