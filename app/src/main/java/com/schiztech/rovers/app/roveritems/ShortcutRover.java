package com.schiztech.rovers.app.roveritems;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.utils.BitmapUtils;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.PrefUtils;
import com.schiztech.rovers.app.utils.Utils;

import java.lang.reflect.Type;
import java.net.URISyntaxException;

/**
 * Created by schiz_000 on 5/11/2014.
 */
public class ShortcutRover extends ExecutableRover {

    private static final String TAG = LogUtils.makeLogTag("ShortcutRover");

    //private Bitmap mShortcutIcon;
    private String mShortcutName;
//    private String mShortcutUri;
    private long mShortcutBitmapID;
    //todo get package and set it as well.
   // private String mShortcutPackage;


    //this c'tor is for an new made shortcut
    public ShortcutRover(Intent unflattenedIntent, Context context) {
        super(flattenShortcutIntent(unflattenedIntent),context);

        Bitmap icon = flattenShortcutIcon(unflattenedIntent, context);
        mShortcutName = flattenShortcutName(unflattenedIntent);
        //mShortcutUri = flattenShortcutUri(unflattenedIntent);
        mShortcutBitmapID = BitmapUtils.saveBitmapToStorage(icon, context);
     //   mShortcutPackage = getIntentPackageName(unflattenedIntent);
    }

//    private c'tor - made for GsonAdapter use ONLY!!!
//    private ShortcutRover( String name, String pkg, long bitmapID, int backgroundColor) {
//        super(flattenShortcutIntent(uri, pkg));
//
////        mShortcutUri = uri;
//        //mShortcutPackage = pkg;
//        mShortcutBitmapID = bitmapID;
//        mShortcutName = name;
//        mBackgroundColor = backgroundColor;
//
//    }

    //region ExecutableRover Methods

    @Override
    public Intent getLaunchIntent() {
        return mIntent;
    }



    //endregion

    //region IRover Methods

    @Override
    public Drawable getDefaultIcon(Context context) throws DefaultIconNotAvailableException{
        Bitmap icon = BitmapUtils.readBitmapFromStorage(context, mShortcutBitmapID);
        if(icon != null)
            return new BitmapDrawable(context.getResources(), icon);

        throw new DefaultIconNotAvailableException("Couldn't get icon for action " + getLabel(context));
    }

    @Override
    public String getDefaultLabel(Context context) {
        return mShortcutName;
    }

    @Override
    public int getDefaultColor(Context context) {
        return PrefUtils.getDefaultShortcutColorValue(context);
    }

    @Override
    public void onDelete() {
//        if(mShortcutIcon != null && !mShortcutIcon.isRecycled()){
//            mShortcutIcon.recycle();
//        }
    }


    //endregion

    //region flatten Shortcut Properties

    private static Intent flattenShortcutIntent(Intent unflattenedIntent) {
        try {
            return Intent.parseUri(flattenShortcutUri(unflattenedIntent), 0);
        } catch (URISyntaxException e) {
            LogUtils.LOGE(TAG, "Couldn't get intent  for package \"" + getIntentPackageName(unflattenedIntent) + "\"");
        }

        return null;
    }

    private static Intent flattenShortcutIntent(String uri, String pkg) {
        try {
            return Intent.parseUri(uri, 0);
        } catch (URISyntaxException e) {
            LogUtils.LOGE(TAG, "Couldn't get intent  for package \"" + pkg + "\"");
        }

        return null;
    }

    private static String flattenShortcutUri(Intent unflattenedIntent) {
        try {
            Intent tmpIntent = (Intent) unflattenedIntent.getExtras().get(Intent.EXTRA_SHORTCUT_INTENT);

            return tmpIntent.toUri(0);
        } catch (NullPointerException e) {
            LogUtils.LOGE(TAG, "Couldn't get uri for package \"" + unflattenedIntent.getPackage() + "\"");
        }

        return null;
    }

    private static Bitmap flattenShortcutIcon(Intent unflattenedIntent, Context context) {
        Bundle extras = unflattenedIntent.getExtras();

        Bitmap icon = (Bitmap) extras.get(Intent.EXTRA_SHORTCUT_ICON);

        if (icon != null)
            return icon;


        Drawable shortcutIconDrawable = null;
        Parcelable iconResourceParcelable = unflattenedIntent.getExtras().getParcelable(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
        if (iconResourceParcelable != null && iconResourceParcelable
                instanceof Intent.ShortcutIconResource) {
            Intent.ShortcutIconResource iconResource =
                    (Intent.ShortcutIconResource) iconResourceParcelable;


            Resources resources;
            try {
                resources = context.getPackageManager().getResourcesForApplication(iconResource.packageName);
                if (resources != null) {
                    int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    shortcutIconDrawable = resources.getDrawable(id);
                    icon = ((BitmapDrawable) shortcutIconDrawable).getBitmap();
                }

            } catch (PackageManager.NameNotFoundException e) {
                LogUtils.LOGE(TAG, "Couldn't get icon for package \"" + unflattenedIntent.getPackage() + "\"");
            }
        }


        if (icon != null)
            return icon;

        return BitmapUtils.drawableToBitmap(RoversManager.getErrorRoverIcon(context));
    }

    private static String flattenShortcutName(Intent unflattenedIntent) {

        try {
            return unflattenedIntent.getExtras().getString(Intent.EXTRA_SHORTCUT_NAME);
        } catch (NullPointerException e) {
            LogUtils.LOGE(TAG, "Couldn't get name for package \"" + unflattenedIntent.getPackage() + "\"");
        }

        return null;
    }

    private static String flattenShortcutPackage(Intent unflattenedIntent) {
        return getIntentPackageName(unflattenedIntent);
    }

    //endregion

    //region Gson Adapter ShortcutRover

//    public static class ShortcutRoverGsonAdapter implements JsonSerializer<ShortcutRover>, JsonDeserializer<ShortcutRover> {
//
//        public static ShortcutRoverGsonAdapter getInstance(){
//            return new ShortcutRoverGsonAdapter();
//        }
//
//        @Override
//        public JsonElement serialize(ShortcutRover src, Type typeOfSrc, JsonSerializationContext context) {
//            final JsonObject wrapper = new JsonObject();
//
//            JsonObject jo = new JsonObject();
//
//            jo.addProperty("name", src.mShortcutName);
//            //jo.addProperty("package", src.mShortcutPackage);
//            //jo.addProperty("uri", src.mShortcutUri);
//            jo.addProperty("bitmapID", src.mShortcutBitmapID);
//            jo.addProperty("backgroundColor", src.mBackgroundColor);
//
//            wrapper.addProperty("type", src.getClass().getName());
//            wrapper.add("data", jo);
//
//            return wrapper;
//
//        }
//
//        @Override
//        public ShortcutRover deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//            JsonObject jo = json.getAsJsonObject();
//
//            String name = jo.getAsJsonPrimitive("name").getAsString();
//            //String pkg = jo.getAsJsonPrimitive("package").getAsString();
//            String uri = jo.getAsJsonPrimitive("uri").getAsString();
//            long bitmapID = jo.getAsJsonPrimitive("bitmapID").getAsLong();
//            int backgroundColor = jo.getAsJsonPrimitive("backgroundColor").getAsInt();
//            ShortcutRover shortcutRover = new ShortcutRover(uri,name,"",bitmapID, backgroundColor);
//
//            return shortcutRover;
//
//        }
//
//    }

    //endregion

}