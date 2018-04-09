package com.schiztech.rovers.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.util.TypedValue;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.roveritems.RoversManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by schiz_000 on 5/14/2014.
 */
public class BitmapUtils {

    private static final String TAG = LogUtils.makeLogTag("BitmapUtils");
    public static int ICON_COLOR_HOLO_LIGHT = 0xff424242;
    public static int ICON_COLOR_HOLO_DARK = 0xffffffff;
    public static int ICON_COLOR_ALPHA = (int)(1f * 255);

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    //region Bitmap Storage Methods

    private static void addInBitmapOptions(BitmapFactory.Options options) {
        // inBitmap only works with mutable bitmaps, so force the decoder to
        // return mutable bitmaps.
        options.inMutable = true;


            // Try to find a bitmap to use for inBitmap.
            Bitmap inBitmap = CacheUtils.getBitmapFromReusableSet(options);

            if (inBitmap != null) {
                // If a suitable bitmap has been found, set it as the value of
                // inBitmap.
                options.inBitmap = inBitmap;
            }

    }

    public static Bitmap readBitmapFromStorage(Context context, long bitmapID) {
        FileInputStream fis;
        Bitmap icon;
        try {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            options.inSampleSize = 1;
            options.outWidth = 144;
            options.outHeight = 144;
            fis = context.openFileInput("BITMAP_" + bitmapID);
            addInBitmapOptions(options);//try to find if there's an unused bitmap allocation
            icon = BitmapFactory.decodeStream(fis,new Rect(0,0,0,0), options);
            fis.close();
            return icon;
        } catch (FileNotFoundException e) {
            LogUtils.LOGE(TAG, "File not found exception: " + e);
        } catch (IOException e) {
            LogUtils.LOGE(TAG, "IO exception: " + e);
        }
        return null;

    }

    public static  long saveBitmapToStorage(Bitmap icon, Context context) {

        long bitmapID = RoversManager.generateNewBitmapID(context);

        FileOutputStream fos;
        try {
            fos = context.openFileOutput("BITMAP_" + bitmapID, Context.MODE_PRIVATE);
            icon.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            return  bitmapID;
        } catch (FileNotFoundException e) {
            LogUtils.LOGE(TAG, "File not found exception: " + e);
        } catch (IOException e) {
            LogUtils.LOGE(TAG, "IO exception: " + e);
        }

        return -1;
    }



    //endregion

    //region Recoloring Bitmaps Methods

    private static final int BRIGHTNESS_THRESHOLD = 150;



    public static boolean isColorDark(int color) {
        return ((30 * Color.red(color) +
                59 * Color.green(color) +
                11 * Color.blue(color)) / 100) <= BRIGHTNESS_THRESHOLD;
    }

    public static Bitmap recolorBitmap(Drawable drawable, int color) {
        if (drawable == null) {
            return null;
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width <= 0 || height <= 0) {
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }

        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outBitmap);


        drawable.setBounds(0, 0, outBitmap.getWidth(), outBitmap.getHeight());
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        drawable.draw(canvas);

        drawable.setColorFilter(null);
        drawable.setCallback(null); // free up any references

        return outBitmap;
    }

    public static Drawable makeRecoloredDrawable(Context context, BitmapDrawable drawable,
                                                 int color, boolean withStates) {
        Bitmap recoloredBitmap = recolorBitmap(drawable, color);
        BitmapDrawable recoloredDrawable = new BitmapDrawable(
                context.getResources(), recoloredBitmap);

        if (!withStates) {
            return recoloredDrawable;
        }

        StateListDrawable stateDrawable = new StateListDrawable();
        stateDrawable.addState(new int[]{android.R.attr.state_pressed}, drawable);
        stateDrawable.addState(new int[]{android.R.attr.state_focused}, drawable);
        stateDrawable.addState(new int[]{}, recoloredDrawable);
        return stateDrawable;
    }

    public static int getHighlightColor(int color, int amount) {
        return Color.argb(Math.min(255, Color.alpha(color)), Math.min(255, Color.red(color) + amount),
                Math.min(255, Color.green(color) + amount), Math.min(255, Color.blue(color) + amount));
    }

    public static int changeAlpha(int origColor, int newAlpha) {
        origColor = origColor & 0x00ffffff; //drop the previous alpha value
        return (newAlpha << 24) | origColor; //add the one the user inputted
    }

    public static ColorFilter getColorFilter(int color){
        float r = Color.red(color) / 255f;
        float g = Color.green(color) / 255f;
        float b = Color.blue(color) / 255f;
        float a = Color.alpha(color) / 255f;

        ColorMatrix cm = new ColorMatrix(new float[] {
                // Change red channel
                0, 0, 0, 0, r,
                // Change green channel
                0, 0, 0, 0, g,
                // Change blue channel
                0, 0, 0, 0, b,
                // Keep alpha channel
                0, 0, 0, a, 0,
        });

        return new ColorMatrixColorFilter(cm);
    }

    public static Drawable recolorDrawableByBackground(Context context, int drawableResource, int backgroundColor){
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawableResource);
        Drawable result = recolorDrawableByBackground(context, bitmap, backgroundColor);
        bitmap.recycle();

        return result;
    }

    public static Drawable recolorDrawableByBackground(Context context, Drawable drawable, int backgroundColor) {
        Bitmap bitmap = convertToBitmap(drawable);
        return recolorDrawableByBackground(context, bitmap, backgroundColor);
    }

    private static Drawable recolorDrawableByBackground(Context context, Bitmap bitmap, int backgroundColor){
        int recolor = BitmapUtils.isColorDark(backgroundColor) ? BitmapUtils.ICON_COLOR_HOLO_DARK : BitmapUtils.ICON_COLOR_HOLO_LIGHT;
        recolor = changeAlpha(recolor, ICON_COLOR_ALPHA);
        return BitmapUtils.makeRecoloredDrawable(context, new BitmapDrawable(context.getResources(), bitmap), recolor,false);
    }



        //endregion

    //region Flatten Bitmaps Methods

    public static Bitmap flattenDrawable(Drawable baseIcon, int color) {
        if (baseIcon == null) {
            return null;
        }

        Bitmap outBitmap = Bitmap.createBitmap(baseIcon.getIntrinsicWidth(), baseIcon.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outBitmap);
        baseIcon.setBounds(0, 0, baseIcon.getIntrinsicWidth(), baseIcon.getIntrinsicHeight());
        baseIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        baseIcon.draw(canvas);
        baseIcon.setColorFilter(null);
        baseIcon.setCallback(null); // free up any references
        return outBitmap;
    }

    public static Bitmap flattenBitmap(Context context, Bitmap baseIcon, int color) {
        return flattenDrawable(new BitmapDrawable(context.getResources(), baseIcon), color);
    }


    //endregion

    //region Rounded Shaped Bitmaps

    public static Bitmap getCroppedBitmap(Bitmap bitmap, int diameter) {

        int newWidth = (int)(diameter );
        int newHeight =(int)(bitmap.getHeight() * ((float)((float)newWidth / (float)bitmap.getWidth())));
        Bitmap resizedBitmap = getResizedBitmap(bitmap, newWidth, newHeight);


        Bitmap output = Bitmap.createBitmap( diameter ,
                 (int)(newHeight) , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();

        final Rect rect1 = new Rect(0, 0,  newWidth ,   (int)(newHeight));
        final Rect rect2 = new Rect(0, 0,  diameter ,   (int)(newHeight));

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);


        canvas.drawCircle(diameter / 2, (int)((newHeight) / 2),
                (diameter ) / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(resizedBitmap, rect1, rect2, paint);


        resizedBitmap.recycle();
        return output;
    }


    public static Bitmap removeBitmapMargins(Bitmap bmp, int color) {
        // TODO Auto-generated method stub


        long dtMili = System.currentTimeMillis();
        int MTop = 0, MBot = 0, MLeft = 0, MRight = 0;
        boolean found1 = false, found2 = false;

        int[] bmpIn = new int[bmp.getWidth() * bmp.getHeight()];
        int[][] bmpInt = new int[bmp.getWidth()][bmp.getHeight()];

        bmp.getPixels(bmpIn, 0, bmp.getWidth(), 0, 0, bmp.getWidth(),
                bmp.getHeight());

        for (int ii = 0, contX = 0, contY = 0; ii < bmpIn.length; ii++) {
            bmpInt[contX][contY] = bmpIn[ii];
            contX++;
            if (contX >= bmp.getWidth()) {
                contX = 0;
                contY++;
                if (contY >= bmp.getHeight()) {
                    break;
                }
            }
        }

        for (int hP = 0; hP < bmpInt[0].length && !found2; hP++) {
            // looking for MTop
            for (int wP = 0; wP < bmpInt.length && !found2; wP++) {
                if (bmpInt[wP][hP] != color) {
                    Log.e("MTop 2", "Pixel found @" + hP);
                    MTop = hP;
                    found2 = true;
                    break;
                }
            }
        }
        found2 = false;

        for (int hP = bmpInt[0].length - 1; hP >= 0 && !found2; hP--) {
            // looking for MBot
            for (int wP = 0; wP < bmpInt.length && !found2; wP++) {
                if (bmpInt[wP][hP] != color) {
                    Log.e("MBot 2", "Pixel found @" + hP);
                    MBot = bmp.getHeight() - hP;
                    found2 = true;
                    break;
                }
            }
        }
        found2 = false;

        for (int wP = 0; wP < bmpInt.length && !found2; wP++) {
            // looking for MLeft
            for (int hP = 0; hP < bmpInt[0].length && !found2; hP++) {
                if (bmpInt[wP][hP] != color) {
                    Log.e("MLeft 2", "Pixel found @" + wP);
                    MLeft = wP;
                    found2 = true;
                    break;
                }
            }
        }
        found2 = false;

        for (int wP = bmpInt.length - 1; wP >= 0 && !found2; wP--) {
            // looking for MRight
            for (int hP = 0; hP < bmpInt[0].length && !found2; hP++) {
                if (bmpInt[wP][hP] != color) {
                    Log.e("MRight 2", "Pixel found @" + wP);
                    MRight = bmp.getWidth() - wP;
                    found2 = true;
                    break;
                }
            }

        }
        found2 = false;

        int sizeY = bmp.getHeight() - MBot - MTop, sizeX = bmp.getWidth()
                - MRight - MLeft;

        Bitmap bmp2 = Bitmap.createBitmap(bmp, MLeft, MTop, sizeX, sizeY);
        dtMili = (System.currentTimeMillis() - dtMili);
        Log.e("Margin   2",
                "Time needed " + dtMili + "mSec\nh:" + bmp.getWidth() + "w:"
                        + bmp.getHeight() + "\narray x:" + bmpInt.length + "y:"
                        + bmpInt[0].length
        );
        return bmp2;
    }



    public static Drawable getCircularDrawable(GradientDrawable drawable , int color,Resources res , boolean selected){

        drawable.setShape(GradientDrawable.OVAL);

        // Set stroke to dark version of color
        int darkenedColor = getDarkenedColorForColor(color);

        drawable.setColor(color);
        drawable.setStroke((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, res.getDisplayMetrics()), darkenedColor);


        Drawable layeredDrawable = drawable;
        if (selected) {
            layeredDrawable = new LayerDrawable(new Drawable[]{
                    drawable,
                    res.getDrawable(BitmapUtils.isColorDark(color)
                            ? R.drawable.checkmark_white
                            : R.drawable.checkmark_black)
            });
        }

        return layeredDrawable;
    }

    public static Drawable getCircularDrawable(int color,Resources res , boolean selected){
        return getCircularDrawable(new GradientDrawable(), color,res,selected);
    }

    public static Drawable getStrokedOnlyCircularDrawable(int color, Resources res){
        GradientDrawable strokeDrawable= new GradientDrawable();
        strokeDrawable.setShape(GradientDrawable.OVAL);

        // Set stroke to dark version of color
        int darkenedColor = getDarkenedColorForColor(color);

        strokeDrawable.setStroke((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, res.getDisplayMetrics()), darkenedColor);

        return strokeDrawable;
    }

    public static int getDarkenedColorForColor(int color){
        return Color.rgb(
                Color.red(color) * 192 / 256,
                Color.green(color) * 192 / 256,
                Color.blue(color) * 192 / 256);
    }

    //endregion

    //region Resize Bitmaps

    public static Bitmap getResizedBitmap(Bitmap original, int newWidth, int newHeight){
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_4444);

        float ratioX = newWidth / (float) original.getWidth();
        float ratioY = newHeight / (float) original.getHeight();

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, 0, 0);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(original, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;

    }

    //endregion

    public static Bitmap convertToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    public static Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);

        return mutableBitmap;
    }

}
