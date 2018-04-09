package com.schiztech.rovers.actions.settings;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.util.Log;

import com.schiztech.roversettingsaction.R;

import java.io.ByteArrayOutputStream;

public class SettingsStatesProvider extends ContentProvider {
    private static final String TAG = "SettingsStatesProvider";

    private static final UriMatcher mUriMatcher;

    public static final String AUTHORITY = "com.schiztech.roversettingsaction";
    private static final String CONTENT_TYPE = ExtendedRoverActionBuilder.CONTENT_TYPE;

    //region Content URI
    public static final Uri CONTENT_URI_WIFI = Uri.parse("content://" + AUTHORITY
            + "/wifi");
    public static final Uri CONTENT_URI_BT = Uri.parse("content://" + AUTHORITY
            + "/bluetooth");
    public static final Uri CONTENT_URI_ROTATE = Uri.parse("content://" + AUTHORITY
            + "/autorotate");
    public static final Uri CONTENT_URI_BRIGHTNESS = Uri.parse("content://" + AUTHORITY
            + "/brightness");
    public static final Uri CONTENT_URI_RINGERMODE = Uri.parse("content://" + AUTHORITY
            + "/ringermode");

    //endregion Content URI

    private static final int MATCH_CURRENT_WIFI = 1;
    private static final int MATCH_ID_WIFI = 2;
    private static final int MATCH_CURRENT_BT = 3;
    private static final int MATCH_ID_BT = 4;
    private static final int MATCH_CURRENT_ROTATE = 5;
    private static final int MATCH_ID_ROTATE = 6;
    private static final int MATCH_CURRENT_BRIGHTNESS = 7;
    private static final int MATCH_ID_BRIGHTNESS = 8;
    private static final int MATCH_CURRENT_RINGERMODE = 9;
    private static final int MATCH_ID_RINGERMODE = 10;
    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        mUriMatcher.addURI(AUTHORITY, "wifi", MATCH_CURRENT_WIFI);
        mUriMatcher.addURI(AUTHORITY, "wifi/#", MATCH_ID_WIFI);

        mUriMatcher.addURI(AUTHORITY, "bluetooth", MATCH_CURRENT_BT);
        mUriMatcher.addURI(AUTHORITY, "bluetooth/#", MATCH_ID_BT);

        mUriMatcher.addURI(AUTHORITY, "autorotate", MATCH_CURRENT_ROTATE);
        mUriMatcher.addURI(AUTHORITY, "autorotate/#", MATCH_ID_ROTATE);

        mUriMatcher.addURI(AUTHORITY, "brightness", MATCH_CURRENT_BRIGHTNESS);
        mUriMatcher.addURI(AUTHORITY, "brightness/#", MATCH_ID_BRIGHTNESS);

        mUriMatcher.addURI(AUTHORITY, "ringermode", MATCH_CURRENT_RINGERMODE);
        mUriMatcher.addURI(AUTHORITY, "ringermode/#", MATCH_ID_RINGERMODE);
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
            case MATCH_CURRENT_BT:
                addItem(uri, cursor, -1);
                break;
            case MATCH_CURRENT_WIFI:
                addItem(uri, cursor, -1);
                break;
            case MATCH_CURRENT_ROTATE:
                addItem(uri, cursor, -1);
                break;
            case MATCH_CURRENT_BRIGHTNESS:
                addItem(uri, cursor, -1);
                break;
            case MATCH_CURRENT_RINGERMODE:
                addItem(uri, cursor, -1);
                break;
            case MATCH_ID_BT:
                addItem(uri, cursor, ContentUris.parseId(uri));
                break;
            case MATCH_ID_WIFI:
                addItem(uri, cursor, ContentUris.parseId(uri));
                break;
            case MATCH_ID_ROTATE:
                addItem(uri, cursor, ContentUris.parseId(uri));
                break;
            case MATCH_ID_BRIGHTNESS:
                addItem(uri, cursor, ContentUris.parseId(uri));
                break;
            case MATCH_ID_RINGERMODE:
                addItem(uri, cursor, ContentUris.parseId(uri));
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
            case MATCH_CURRENT_WIFI:
                return CONTENT_TYPE;
            case MATCH_CURRENT_BT:
                return CONTENT_TYPE;
            case MATCH_CURRENT_ROTATE:
                return CONTENT_TYPE;
            case MATCH_CURRENT_BRIGHTNESS:
                return CONTENT_TYPE;
            case MATCH_CURRENT_RINGERMODE:
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

    //region Helper Methods

    private void addItem(Uri uri, MatrixCursor cursor, long id) {
        int iconResource = R.drawable.ri_settings_gear;

        switch (mUriMatcher.match(uri)) {
            case MATCH_CURRENT_BT:
                iconResource = getBluetoothResource(id);
                break;
            case MATCH_CURRENT_WIFI:
                iconResource = getWifiResource(id);
                break;
            case MATCH_CURRENT_ROTATE:
                iconResource = getRotationResource(id);
                break;
            case MATCH_CURRENT_BRIGHTNESS:
                iconResource = getBrightnessResource(id);
                break;
            case MATCH_CURRENT_RINGERMODE:
                iconResource = getRingerModeResource(id);
                break;
            default:
                iconResource = R.drawable.ri_settings_gear;

        }

        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), iconResource);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapData = stream.toByteArray();

        cursor.addRow(new Object[] { new Long(id), bitmapData, Integer.MAX_VALUE });
    }

    private int getBluetoothResource(long id){
        boolean isEnabled;
        if(id != -1)
            isEnabled = id == 1 ? true: false;
        else
            isEnabled = Utils.isBluetoothOn();


        return isEnabled ? R.drawable.ic_bluetooth_on : R.drawable.ic_bluetooth_off;
    }

    private int getWifiResource(long id){
        boolean isEnabled;
        if(id != -1)
            isEnabled = id == 1 ? true: false;
        else
            isEnabled = Utils.isWifiOn(getContext());

        return isEnabled ? R.drawable.ic_wifi_on : R.drawable.ic_wifi_off;
    }

    private int getRotationResource(long id){
        boolean isEnabled;
        if(id != -1)
            isEnabled = id == 1 ? true: false;
        else
            isEnabled = Utils.isAutoRotateOn(getContext());

        return isEnabled ? R.drawable.ic_rotation_on : R.drawable.ic_rotation_off;
    }

    private int getBrightnessResource(long id){
        if(id == -1)
            id = Utils.getBrightnessLevelID(getContext());

        int resource;
        //region get Resource for id
        switch ((int)id){
            case Utils.BRIGHTNESS_LEVEL_ID_AUTO:
                resource = R.drawable.ic_brightness_auto;
                break;
            case Utils.BRIGHTNESS_LEVEL_ID_HIGH:
                resource = R.drawable.ic_brightness_high;
                break;
            case Utils.BRIGHTNESS_LEVEL_ID_MEDIUM:
                resource = R.drawable.ic_brightness_medium;
                break;
            case Utils.BRIGHTNESS_LEVEL_ID_LOW:
                resource = R.drawable.ic_brightness_low;
                break;
            default:
                resource = R.drawable.ic_brightness_high;
                break;
        }
        //endregion get Resource for id

        return resource;
    }

    private int getRingerModeResource(long id){
        if(id == -1)
            id = Utils.getRingerMode(getContext());

        int resource;
        //region get Resource for id
        switch ((int)id){
            case AudioManager.RINGER_MODE_NORMAL:
                resource = R.drawable.ic_ringer_normal;
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                resource = R.drawable.ic_ringer_vibrate;
                break;
            case AudioManager.RINGER_MODE_SILENT:
                resource = R.drawable.ic_ringer_silent;
                break;
            default:
                resource = R.drawable.ic_ringer_normal;
                break;
        }
        //endregion get Resource for id

        return resource;
    }

    //endregion Helper Methods
}
