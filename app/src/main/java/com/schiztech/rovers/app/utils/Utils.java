package com.schiztech.rovers.app.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.BoolRes;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.StringRes;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.rebound.SpringSystem;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.MainActivity;
import com.schiztech.rovers.app.activities.SyncRoverActivity;
import com.schiztech.rovers.app.managers.AnalyticsManager;

import org.onepf.oms.appstore.AmazonAppstore;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import wei.mark.standout.StandOutWindow;

/**
 * Created by schiz_000 on 5/21/2014.
 */
public class Utils {
    private static final String TAG = LogUtils.makeLogTag("Utils");
    public enum MoveType { Regular, Sudden, Instant;
        public static MoveType fromInteger(int x) {
            switch(x) {
                case 0:
                    return Regular;
                case 1:
                    return Sudden;
                case 2:
                    return Instant;
            }
            return null;
        }
    };
    public enum RoverType {
        App,
        Shortcut,
        Action,
        Folder
    }


    public static boolean isAndroidVersionEqualOrAbove(int version){
        return android.os.Build.VERSION.SDK_INT >= version;
    }

    public static boolean isAndroidVersionEqualOrBelow(int version){
        return android.os.Build.VERSION.SDK_INT <= version;
    }


    //region Vibration
    public static final int VIBRATE_MINIMAL = 20;
    public static final int VIBRATE_SHORT = 50;
    public static final int VIBRATE_MEDIUM = 100;
    public static final int VIBRATE_LONG = 200;

    public static void Vibrate(Context context, int multiseconds) {
        try {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(multiseconds);
        } catch (Exception e) {
            LogUtils.LOGE(TAG, "Vibration Failed");
        }
    }

    //endregion Vibration

    //region Corner Logic

    public enum Corner {LeftTop, LeftBottom, RightTop, RightBottom}

    public enum Direction { Left, Right, Top, Bottom};

    public static Point getCornerLocation(Context context, Corner corner) {
        Point p = new Point();
        Point displayDimensions = getDisplayDimensions(context);
        switch (corner) {
            case LeftTop:
                p.x = 0;
                p.y = 0;
                break;
            case LeftBottom:
                p.x = 0;
                p.y = displayDimensions.y;
                break;

            case RightTop:
                p.x = displayDimensions.x;
                p.y = 0;
                break;

            case RightBottom:
                p.x = displayDimensions.x;
                p.y = displayDimensions.y;
                break;

        }

        return p;

    }

    public static Corner getClosestCorner(Context context, Point currentLocation){
        Utils.Corner closestCorner = Corner.LeftBottom;
        int closestDistance = Utils.getDistanceFromCorner(currentLocation, closestCorner, context);



        if (closestCorner != Utils.Corner.LeftTop) {
            int leftTopDistance = Utils.getDistanceFromCorner(currentLocation, Utils.Corner.LeftTop, context);
            if (leftTopDistance < closestDistance) {
                closestCorner = Utils.Corner.LeftTop;
                closestDistance = leftTopDistance;
            }
        }

        if (closestCorner != Utils.Corner.RightBottom) {
            int rightBottomDistance = Utils.getDistanceFromCorner(currentLocation, Utils.Corner.RightBottom, context);
            if (rightBottomDistance < closestDistance) {
                closestCorner = Utils.Corner.RightBottom;
                closestDistance = rightBottomDistance;
            }
        }

        if (closestCorner != Utils.Corner.RightTop) {
            int rightBottomDistance = Utils.getDistanceFromCorner(currentLocation, Utils.Corner.RightTop, context);
            if (rightBottomDistance < closestDistance) {
                closestCorner = Utils.Corner.RightTop;
                closestDistance = rightBottomDistance;
            }
        }


        return closestCorner;
    }

    public static Utils.Corner getExpandCorner(Context context, Point currentLocation) {

       int cornerID = PrefUtils.getHostStickyCornerValue(context);

        switch(cornerID){
            case PrefUtils.HOST_STICKY_CORNER_BOTTOMLEFT:
                return Corner.LeftBottom;
            case PrefUtils.HOST_STICKY_CORNER_BOTTOMRIGHT:
                return Corner.RightBottom;
            case PrefUtils.HOST_STICKY_CORNER_TOPLEFT:
                return Corner.LeftTop;
            case PrefUtils.HOST_STICKY_CORNER_TOPRIGHT:
                return Corner.RightTop;
            case PrefUtils.HOST_STICKY_CORNER_CLOSESTCORNER:
                return getClosestCorner(context, currentLocation);
            default:
                return getClosestCorner(context, currentLocation);
        }

    }

    public static int getDistanceFromCorner(Point source, Corner corner, Context context) {
        Point cornerLocation = getCornerLocation(context, corner);
        return getDistance(source, cornerLocation);
    }

    public static Point getCornerParams(Utils.Corner corner) {
        Point cornerParams = new Point(0,0);
        switch (corner) {
            case RightBottom:
                cornerParams.x = StandOutWindow.StandOutLayoutParams.RIGHT;
                cornerParams.y = StandOutWindow.StandOutLayoutParams.BOTTOM;
                break;

            case RightTop:
                cornerParams.x = StandOutWindow.StandOutLayoutParams.RIGHT;
                cornerParams.y = StandOutWindow.StandOutLayoutParams.TOP;
                break;
            case LeftBottom:
                cornerParams.x = StandOutWindow.StandOutLayoutParams.LEFT;
                cornerParams.y = StandOutWindow.StandOutLayoutParams.BOTTOM;
                break;

            case LeftTop:
                cornerParams.x = StandOutWindow.StandOutLayoutParams.LEFT;
                cornerParams.y = StandOutWindow.StandOutLayoutParams.TOP;
                break;
        }

        return cornerParams;
    }

    public static boolean isCornerLeft(Corner corner){
        return corner == Corner.LeftBottom || corner == Corner.LeftTop;
    }
    public static boolean isCornerRight(Corner corner){
        return corner == Corner.RightBottom || corner == Corner.RightTop;
    }
    public static boolean isCornerTop(Corner corner){
        return corner == Corner.RightTop || corner == Corner.LeftTop;
    }
    public static boolean isCornerBottom(Corner corner){
        return corner == Corner.LeftBottom || corner == Corner.RightBottom;
    }

    //endregion

    //region Screen & Display

    public static Point getDisplayDimensions(Context context) {
        DisplayMetrics metrics = context.getResources()
                .getDisplayMetrics();
        Point displayDimensions = new Point();
        displayDimensions.x = metrics.widthPixels;
        displayDimensions.y = (int) (metrics.heightPixels - 25 * metrics.density);


        return displayDimensions;
    }

    public static int getDistance(Point source, Point destination) {
        return (int)Math.sqrt((source.x-destination.x)*(source.x-destination.x) + (source.y-destination.y)*(source.y-destination.y));
    }

    public static List<Point> getLinearLine(Point source, Point destination) {

        List<Point> line = new ArrayList<Point>();

        int dx = Math.abs(destination.x - source.x);
        int dy = Math.abs(destination.y - source.y);

        int sx = source.x < destination.x ? 1 : -1;
        int sy = source.y < destination.y ? 1 : -1;

        int err = dx - dy;
        int e2;
        int currentX = source.x;
        int currentY = source.y;

        while (true) {
            line.add(new Point(currentX, currentY));

            if (currentX == destination.x && currentY == destination.y) {
                break;
            }

            e2 = 2 * err;
            if (e2 > -1 * dy) {
                err = err - dy;
                currentX = currentX + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                currentY = currentY + sy;
            }
        }

        return line;
    }


    //endregion Screen & Display

    //region Fling Logic

    private static final float FLING_THRESHOLD_Y = 0.75f;
    private static final float FLING_THRESHOLD_X = 0.1f;

    public static Point computeEndPoint(Context context, int roverSize, int currentX, int currentY, float velocityX, float velocityY){
        Point screenDims = getDisplayDimensions(context);
        double finalY = currentY;
        double finalX = computeEndX(screenDims, currentX, currentY, velocityX, velocityY);

        if(velocityY > FLING_THRESHOLD_Y){
            finalY = screenDims.y;
        }
        else if(velocityY < -1 * FLING_THRESHOLD_Y){
            finalY = 0;
        }
        else{

            finalY = finalY + screenDims.y * velocityY;
            finalY -= roverSize/2;
        }


        finalY = Math.max(0 , finalY);
        finalY = Math.min(finalY, screenDims.y);

        return new Point((int)finalX, (int)finalY);

    }

    private static double computeEndX(Point screenDims, int currentX, int currentY, float velocityX, float velocityY){

        if(velocityX> -1 * FLING_THRESHOLD_X && (currentX >= screenDims.x / 2 || velocityX > FLING_THRESHOLD_X)  )
            return screenDims.x;
        else {
            return 0;
        }
    }

    //endregion Fling Logic

    //region Animations

    public interface AnimationFinishedListener {
        void onAnimationFinished();
    }

    //endregion Animations

    //region Rebound

    private static SpringSystem mSpringSystem;

    public static SpringSystem getSpringSystem(){
        if(mSpringSystem == null)
            return SpringSystem.create();

        return  mSpringSystem;
    }


    //endregion

    //region Resources

    public static String getString(Context context, @StringRes int resID){
        if(context == null)
            return "";

        return context.getResources().getString(resID);
    }

    public static int getColor(Context context,@ColorRes int resID){
        return context.getResources().getColor(resID);
    }

    public static Drawable getDrawable(Context context, @DrawableRes int resID){
        return context.getResources().getDrawable(resID);
    }

    public static int getInteger(Context context,@IntegerRes int resID){
        return context.getResources().getInteger(resID);
    }

    public static boolean getBoolean(Context context,@BoolRes int resID){
        return context.getResources().getBoolean(resID);
    }

    public static int getDimensionPixelSize(Context context,@DimenRes int resID){
        return context.getResources().getDimensionPixelSize(resID);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    public static int getDeviceDpi(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.densityDpi;
    }

    //endregion Resources


    public static void browseLink(Context context,@StringRes int linkRes) {
        browseLink(context, getString(context, linkRes));
    }
    public static void browseLink(Context context, String link){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(browserIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.error_browse, Toast.LENGTH_SHORT).show();
        }
    }

    public static void syncRoverWindow(Context context, boolean isShow){
        Intent toggleIntent = new Intent(context, SyncRoverActivity.class);
        Bundle b = new Bundle();

        b.putBoolean(SyncRoverActivity.TOGGLE_IS_SHOW_KEY, isShow);

        if(!(context instanceof Activity)) {
            toggleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        toggleIntent.putExtras(b);
        context.startActivity(toggleIntent);

    }

    public static void unbindDrawables(View view) {
        if(view != null){
            try {
                view.setOnClickListener(null);//remove onClicks
            }
            catch (Exception e){}
        }
        if (view != null && view.getBackground() != null)
            view.getBackground().setCallback(null);

        if (view != null && view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            imageView.setImageBitmap(null);
        } else if (view != null && view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++)
                unbindDrawables(viewGroup.getChildAt(i));

            if (view != null && !(view instanceof AdapterView))
                viewGroup.removeAllViews();
        }
    }

    public static int[] loadColorSelectionArray(Context context) {

        int choicesResId = R.array.md_basic_colors_array;

        int[] choices = context.getResources().getIntArray(choicesResId);


        return choices;

    }

    public static List<Integer> asList(final int[] is)
    {
        return new AbstractList<Integer>() {
            public Integer get(int i) { return is[i]; }
            public int size() { return is.length; }
        };
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public static void navigateTo(Context context, @StringRes int fragTag){
            Intent intent = new Intent(context, MainActivity.class);
            Bundle b = new Bundle();

            b.putInt(MainActivity.STATE_STARTUP_FRAGMENT, fragTag);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.putExtras(b);
            context.startActivity(intent);
    }

    //region Complaints

    public static void complain(Context context, @StringRes int messageRes){
        complain(context, getString(context, messageRes));
    }

    public static void complain(Context context, String message) {
        LogUtils.LOGE(TAG, "**** Rovers Complaint: " + message);
        if (!AmazonAppstore.hasAmazonClasses()) { // Amazon moderators don't allow alert dialogs for in-apps
            alertWithTitle(context, R.string.error_default, message);
        }
    }

    public static void alertWithTitle(Context context, int titleStringId, int messageStringID) {
        alertWithTitle(context, getString(context, titleStringId), getString(context, messageStringID));
    }


    public static void alertWithTitle(Context context, int titleStringId, String messageString) {
        alertWithTitle(context, getString(context, titleStringId), messageString);
    }

    public static void alertWithTitle(Context context, String titleString, String messageString) {
        try {
            AlertDialog.Builder bld = new AlertDialog.Builder(context);
            bld.setTitle(titleString);
            bld.setMessage(messageString);
            bld.setNeutralButton("OK", null);
            Log.d(TAG, "Showing alert dialog: title" + titleString + "; message = " + messageString);
            bld.create().show();
        }
        catch (Exception e){
            Toast.makeText(context, titleString +": " + messageString,Toast.LENGTH_SHORT).show();
        }
    }




    //endregion Complaints

}
