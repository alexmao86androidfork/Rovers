package com.schiztech.rovers.actions.battery;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.BatteryManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;

public class BatteryLevelProvider extends ContentProvider {
    private static final String TAG = "BatteryLevelProvider";

    private static final UriMatcher mUriMatcher;

    public static final String AUTHORITY = "com.schiztech.rovers.actions.battery";
    private static final String CONTENT_TYPE = ExtendedRoverActionBuilder.CONTENT_TYPE;
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/battery");

    private static final int MATCH_CURRENT = 1;
    private static final int MATCH_ID = 2;
    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, "battery", MATCH_CURRENT);
        mUriMatcher.addURI(AUTHORITY, "battery/#", MATCH_ID);
    }


    //region Content Provider Methods
    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Log.d(TAG, "query(" + uri + ")");
        MatrixCursor cursor = new MatrixCursor(ExtendedRoverActionBuilder.INTERACTIVE_ACTION_TYPE);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        switch (mUriMatcher.match(uri)) {
            case MATCH_CURRENT:
                refreshBatteryData();
                addItem(cursor, (int)mBatteryLevel);
                break;
            case MATCH_ID:
                long id = ContentUris.parseId(uri);
                addItem(cursor, id);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case MATCH_CURRENT:
                return CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

    //endregion Content Provider Methods

    //region Battery Methods
    private float mBatteryLevel = -1;
    private boolean mIsBatteryPlugged = false;
    enum BatteryLevelCategories{
        Full (95),
        High (70),
        HighMed (50),
        Medium(20),
        Low(5),
        Empty(0),
        Charging(101);
        private final int value;

        private BatteryLevelCategories(int minLevel){
            value = minLevel;
        }
    }
    private void refreshBatteryData() {
        Intent batteryIntent = getContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        mIsBatteryPlugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0;

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            mBatteryLevel = -1;
        }

        mBatteryLevel = ((float)level / (float)scale) * 100.0f;
    }

    private Bitmap getBatteryPercentageIcon(long batteryLevel){
        return BitmapFactory.decodeResource(getContext().getResources(), getResourceID("stat_sys_battery_" + batteryLevel, "drawable"));
    }

    private int getBatteryPercentageBackground(long batteryLevel){
        if(mIsBatteryPlugged)
            return getContext().getResources().getColor(R.color.battery_charging);

        if(batteryLevel >= BatteryLevelCategories.Full.value)
            return getContext().getResources().getColor(R.color.battery_full);

        if(batteryLevel >= BatteryLevelCategories.High.value)
            return getContext().getResources().getColor(R.color.battery_high);

        if(batteryLevel >= BatteryLevelCategories.HighMed.value)
            return getContext().getResources().getColor(R.color.battery_high_med);

        if(batteryLevel >= BatteryLevelCategories.Medium.value)
            return getContext().getResources().getColor(R.color.battery_medium);

        if(batteryLevel >= BatteryLevelCategories.Low.value)
            return getContext().getResources().getColor(R.color.battery_low);

        //else
            return getContext().getResources().getColor(R.color.battery_empty);

    }
    //endregion Battery Methods

    //region Helper Methods

    private void addItem(MatrixCursor cursor, long id) {
        Bitmap bitmap = getBatteryPercentageIcon(id);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapData = stream.toByteArray();

        int backgroundColor = getBatteryPercentageBackground(id);


        cursor.addRow(new Object[] { new Long(id), bitmapData, backgroundColor });
    }

    private int getResourceID(String resName, String resType){
        final int ResourceID =
                getContext().getResources().getIdentifier(resName, resType,
                        getContext().getApplicationInfo().packageName);
        if (ResourceID == 0)
        {
            Log.e(TAG,"No resource string found with name " + resName);
                return R.drawable.stat_sys_battery_unknown;
        }
        else
        {
            return ResourceID;
        }
    }

    //endregion Helper Methods

}
