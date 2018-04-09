package com.schiztech.rovers.app.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.utils.BitmapUtils;
import com.schiztech.rovers.app.utils.LogUtils;

/**
 * Created by schiz_000 on 8/8/2014.
 */
public class CircleButton extends ImageView {
    private static final String TAG = LogUtils.makeLogTag("CircleButton");
    private static final int PRESSED_COLOR_LIGHTUP = 255 / 25;
    private static final int DEFAULT_SHADOW_WIDTH_DIP = 2;
    private static final int DEFAULT_SHADOW_COLOR = 0x66000000;
    private static final int DEFAULT_CIRCLE_COLOR = Color.BLACK;

    private Paint mCirclePaint;
    private Paint mShadowPaint;

    private boolean mIsShadowTilted = true;

    private int mOuterRadius;

    private int mShadowWidth;

    private int mCircleColor;
    private int mPressedColor;
    private int mShadowColor;

    private int mCenterX;
    private int mCenterY;

    private float mRotationAngel = 0;
    private float mScaleLevel = 1;

    private boolean mIsForcedPressed = false;

    //region C'tors
    public CircleButton(Context context) {
        super(context);
        init(context, null);
    }

    public CircleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    //endregion C'tors

    private void init(Context context, AttributeSet attrs) {
        this.setFocusable(true);
        this.setScaleType(ScaleType.CENTER_INSIDE);
        setClickable(true);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.FILL);

        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setStyle(Paint.Style.STROKE);

        mShadowWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SHADOW_WIDTH_DIP, getResources().getDisplayMetrics());
        int color = DEFAULT_CIRCLE_COLOR;
        int shadowColor = DEFAULT_SHADOW_COLOR;

        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleButton);
            color = a.getColor(R.styleable.CircleButton_circle_color, color);
            shadowColor = a.getColor(R.styleable.CircleButton_shadow_color, shadowColor);
            mIsShadowTilted = a.getBoolean(R.styleable.CircleButton_is_shadow_tilted, mIsShadowTilted);
            mShadowWidth = (int) a.getDimension(R.styleable.CircleButton_shadow_size, mShadowWidth);
            a.recycle();
        }

        setCircleColor(color);
        mShadowColor = shadowColor;

        mShadowPaint.setStrokeWidth(mShadowWidth);


    }

    public void setCircleColor(int color) {
        mCircleColor = color;
        mPressedColor = BitmapUtils.getHighlightColor(color, PRESSED_COLOR_LIGHTUP);
        mCirclePaint.setColor(mCircleColor);
        setLayerType(LAYER_TYPE_SOFTWARE, mCirclePaint);

       // mCirclePaint.setShadowLayer(mShadowWidth, 0.0f, mShadowWidth/2, Color.BLACK);

        this.invalidate();
    }

    public void setShadowColor(int color){
        mShadowColor = color;
        mShadowPaint.setColor(mShadowColor);
        setLayerType(LAYER_TYPE_SOFTWARE, mShadowPaint);

        this.invalidate();

    }

    public boolean isShadowTilted(){
        return mIsShadowTilted;
    }

    public void setShadowTilted(boolean isTilted){
        mIsShadowTilted = isTilted;
        invalidate();
    }

    public int getCircleColor(){
        return mCircleColor;
    }

    public int getCircleColorPressed(){
        return mPressedColor;
    }

    public void setCircleSize(int size){
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
        params.height = size;
        params.width = size;
        setLayoutParams(params);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h / 2;
        mOuterRadius = Math.min(w, h) / 2;

        float radialRadius = ((float)(mOuterRadius - mShadowWidth) / (float)mOuterRadius);
        mShadowPaint.setColor(mShadowColor);
        requestLayout();
//        mShadowPaint.setShader(new RadialGradient(
//                mCenterX,
//                mCenterY + mShadowWidth,
//                mOuterRadius,
//                new int[] {0x99000000,0x99000000 , 0x00000000,0x00000000},
//                new float[] {0,radialRadius,radialRadius+ (1-radialRadius)/1.5f,1},
//                Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int radius = mOuterRadius;
        if(isPressed()){
            canvas.scale(0.925f,0.925f,canvas.getWidth()/2, canvas.getHeight()/2);
        }

        if(isRotating()){
            canvas.rotate(mRotationAngel, canvas.getWidth()/2, canvas.getHeight()/2);
        }

        if(isScaled()){
            canvas.scale(mScaleLevel, mScaleLevel, canvas.getWidth()/2, canvas.getHeight()/2);
        }

        //draw shadow
        if(mIsShadowTilted) {

            canvas.drawCircle(mCenterX, mCenterY + mShadowWidth / 3, radius - mShadowWidth, mShadowPaint);
        }
        else{
            canvas.drawCircle(mCenterX, mCenterY, radius- mShadowWidth, mShadowPaint);
        }

        //circle layer
        canvas.drawCircle(mCenterX, mCenterY, radius - mShadowWidth, mCirclePaint);

         super.onDraw(canvas);
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if(isEnabled() && isClickable()) {
            if (mCirclePaint != null) {
                mCirclePaint.setColor(pressed ? mPressedColor : mCircleColor);
                invalidate();
            }
        }
    }

    public void markPressed(boolean pressed){
        mIsForcedPressed = pressed;
        setPressed(pressed);
    }

    @Override
    public boolean isPressed(){
        return super.isPressed() || mIsForcedPressed;
    }

    //region Rotation
    @Override
    public void setRotation(float angel){
        mRotationAngel = angel;
        invalidate();
    }

    public boolean isRotating(){
        return mRotationAngel != 0;
    }
    //endregion

    //region Scale
    public void setScaleLevel(float scaleLevel){
        mScaleLevel = scaleLevel;
        invalidate();
    }

    public float getScaleLevel(){
        return mScaleLevel;
    }


    private boolean isScaled(){
        return mScaleLevel != 1 ;
    }

    //endregion

    @Override
    public void setImageBitmap(Bitmap bitmap){
        if(bitmap == null || !bitmap.isRecycled()) {
            super.setImageBitmap(bitmap);
        }
        else{
            LogUtils.LOGD(TAG, "Bitmap image recycled");
        }
    }

    public int getInnerWidth(){
        return this.getLayoutParams().width - mShadowWidth*2;
    }
}
