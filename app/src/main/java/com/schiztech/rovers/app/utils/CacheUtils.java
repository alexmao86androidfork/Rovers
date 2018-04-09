package com.schiztech.rovers.app.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.util.LruCache;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by schiz_000 on 10/7/2014.
 */
public class CacheUtils {
    private static final String TAG = LogUtils.makeLogTag("CacheUtils");

    public static void clearIconsCache(){
        clearDrawableCache();
        clearBitmapCache();
    }

    //region BitmapCache

    private static Set<SoftReference<Bitmap>> mReusableBitmaps;
    private static LruCache<String, Bitmap> sBitmapCache;

    private static LruCache<String, Bitmap> getBitmapCache(){
        if(sBitmapCache == null){
            // Get max available VM memory, exceeding this amount will throw an
            // OutOfMemory exception. Stored in kilobytes as LruCache takes an
            // int in its constructor.
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/10th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 20;
            sBitmapCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }

                // Notify the removed entry that is no longer being cached.
                @Override
                protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                    //add the bitmap
                    // to a SoftReference set for possible use with inBitmap later.
                    getReusableBitmaps().add(new SoftReference<Bitmap>(oldValue));
                    LogUtils.LOGD(TAG," new bitmap added to soft cache");
                }
            };
        }

        return sBitmapCache;
    }

    public static void addToBitmapCache(String key, Bitmap bitmap){
        getBitmapCache().put(key, bitmap);
    }

    public static Bitmap getFromBitmapCache(String key){
        return getBitmapCache().get(key);
    }

    public static Set<SoftReference<Bitmap>> getReusableBitmaps(){
        if(mReusableBitmaps == null){
            mReusableBitmaps = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
        }

        return mReusableBitmaps;
    }

    // This method iterates through the reusable bitmaps, looking for one
    // to use for inBitmap:
    protected static Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
        Bitmap bitmap = null;
        Set<SoftReference<Bitmap>> reusableBitmaps = CacheUtils.getReusableBitmaps();
        if (reusableBitmaps != null && !reusableBitmaps.isEmpty()) {
            synchronized (reusableBitmaps) {
                final Iterator<SoftReference<Bitmap>> iterator
                        = reusableBitmaps.iterator();
                Bitmap item;

                while (iterator.hasNext()) {
                    item = iterator.next().get();

                    if (null != item && item.isMutable()) {
                        // Check to see it the item can be used for inBitmap.
                        if (canUseForInBitmap(item, options)) {
                            bitmap = item;

                            // Remove from reusable set so it can't be used again.
                            iterator.remove();
                            break;
                        }
                    } else {
                        // Remove from the set if the reference has been cleared.
                        iterator.remove();
                    }
                }
            }
        }
        return bitmap;
    }

    static boolean canUseForInBitmap(
            Bitmap candidate, BitmapFactory.Options targetOptions) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // From Android 4.4 (KitKat) onward we can re-use if the byte size of
            // the new bitmap is smaller than the reusable bitmap candidate
            // allocation byte count.
            int width = targetOptions.outWidth / targetOptions.inSampleSize;
            int height = targetOptions.outHeight / targetOptions.inSampleSize;
            int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
            return byteCount <= candidate.getAllocationByteCount();
        }

        // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
        return candidate.getWidth() == targetOptions.outWidth
                && candidate.getHeight() == targetOptions.outHeight
                && targetOptions.inSampleSize == 1;
    }

    /**
     * A helper function to return the byte usage per pixel of a bitmap based on its configuration.
     */
    static int getBytesPerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }

    public static void clearBitmapCache(){
        if(sBitmapCache != null) {
            sBitmapCache.evictAll();
        }
    }

    //endregion BitmapCache

    //region DrawableCache
    private static LruCache<String, Drawable> sDrawableCache;

    private static LruCache<String, Drawable> getDrawableCache(){
        if(sDrawableCache == null){
            // Get max available VM memory, exceeding this amount will throw an
            // OutOfMemory exception. Stored in kilobytes as LruCache takes an
            // int in its constructor.
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/20th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 20;
            sDrawableCache = new LruCache<String, Drawable>(cacheSize) {
                @Override
                protected int sizeOf(String key, Drawable drawable) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return ((BitmapDrawable)drawable).getBitmap().getByteCount() / 1024;
                }

            };
        }

        return sDrawableCache;
    }

    public static void addToDrawableCache(String key, Drawable drawable){
        getDrawableCache().put(key, drawable);
    }

    public static Drawable getFromDrawableCache(String key){
        return getDrawableCache().get(key);
    }

    public static void clearDrawableCache(){
        if(sDrawableCache != null) {
            sDrawableCache.evictAll();
        }
    }

    //endregion DrawableCache

    //region ColorCache

    private static HashMap<String, Integer> sColorCache;

    private static HashMap<String, Integer> getColorCache(){
        if(sColorCache == null){
            sColorCache = new HashMap<String, Integer>();
        }

        return sColorCache;
    }

    public static void addToColorCache(String key, int color){
        getColorCache().put(key, color);
    }

    public static Integer getFromColorCache(String key){
        if(getColorCache().containsKey(key))
            return getColorCache().get(key);

        return null;
    }

    //endregion ColorCache

}
