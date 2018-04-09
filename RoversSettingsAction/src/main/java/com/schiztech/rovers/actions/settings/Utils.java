package com.schiztech.rovers.actions.settings;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Created by schiz_000 on 11/13/2014.
 */
public class Utils {
    public enum SettingsTypes {
        Wifi,
        Bluetooth,
        Brightness,
        AutoRotation,
        RingerMode
    }


    public static boolean isAndroidVersionEqualOrAbove(int version) {
        return android.os.Build.VERSION.SDK_INT >= version;
    }

    //region WiFi
    public static boolean isWifiOn(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public static boolean toggleWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean isEnabled = wifiManager.isWifiEnabled();
        wifiManager.setWifiEnabled(!isEnabled);

        Toast.makeText(context, "Turned Wi-Fi " + (isEnabled ? "off": "on") ,Toast.LENGTH_SHORT).show();
        return !isEnabled;
    }

    //endregion WiFi

    //region Bluetooth
    public static boolean isBluetoothOn() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
    }

    public static boolean toggleBluetooth(Context context) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (isEnabled)
            bluetoothAdapter.disable();
        else
            bluetoothAdapter.enable();

        Toast.makeText(context, "Turned Bluetooth " + (isEnabled ? "off": "on") ,Toast.LENGTH_SHORT).show();
        return !isEnabled;
    }

    //endregion Bluetooth

    //region Auto Rotation

    public static boolean isAutoRotateOn(Context context) {
        return android.provider.Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
    }

    public static boolean toggleAutoRotate(Context context) {
        boolean isEnabled = android.provider.Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;

        android.provider.Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, isEnabled ? 0 : 1);

        Toast.makeText(context, "Turned auto rotation " + (isEnabled ? "off": "on") ,Toast.LENGTH_SHORT).show();
        return !isEnabled;

    }

    //endregion Auto Rotation

    //region Brightness Methods
    public static final int BRIGHTNESS_LEVEL_ID_LOW = 0;
    public static final int BRIGHTNESS_LEVEL_ID_MEDIUM = 1;
    public static final int BRIGHTNESS_LEVEL_ID_HIGH = 2;
    public static final int BRIGHTNESS_LEVEL_ID_AUTO = 3;

    public static void toggleAutoBrightness(Context context) {
        boolean isAutoBrightnessOn = isAutoBrightness(context);
        if (isAutoBrightnessOn) {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        } else {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        }
    }

    public static boolean isAutoBrightness(Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    public static void setBrightnessLevel(Context context, int newLevel) {
        newLevel = Math.max(1, newLevel);//make sure no 0 values
        android.provider.Settings.System.putInt(context.getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS,
                newLevel);
    }

    public static int getBrightnessLevel(Context context) {
        float curBrightnessValue = 0;
        try {
            curBrightnessValue = android.provider.Settings.System.getInt(
                    context.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return (int) curBrightnessValue;
    }

    public static int getMaxBrightnessLevel(){
        return 255;
    }

    public static int getBrightnessLevelID(Context context){
        if(isAutoBrightness(context))
            return BRIGHTNESS_LEVEL_ID_AUTO;

        float brightnessPercentage =  (float)((float)getBrightnessLevel(context) / (float)getMaxBrightnessLevel());

        if(brightnessPercentage <= 0.3f)
            return BRIGHTNESS_LEVEL_ID_LOW;

        if(brightnessPercentage <= 0.7f)
            return BRIGHTNESS_LEVEL_ID_MEDIUM;

        return BRIGHTNESS_LEVEL_ID_HIGH;
    }

    //endregion Brightness Methods

    //region Ringer Mode Methods
    public static int getRingerMode(Context context){
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getRingerMode();
    }

    public static void setRingerMode(Context context, int ringerMode){
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(ringerMode);
    }

    public static void toggleRingerMode(Context context){
        int ringerMode = getRingerMode(context);
        if(ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            setRingerMode(context, AudioManager.RINGER_MODE_VIBRATE);
            Toast.makeText(context, "Ringer mode is vibrate" ,Toast.LENGTH_SHORT).show();
        }
        else if(ringerMode == AudioManager.RINGER_MODE_VIBRATE){

            if(isAndroidVersionEqualOrAbove(Build.VERSION_CODES.LOLLIPOP)){//lollipop doesn't play cool with silent mode.
                setRingerMode(context, AudioManager.RINGER_MODE_NORMAL);
                Toast.makeText(context, "Ringer mode is normal" ,Toast.LENGTH_SHORT).show();
            }
            else {
                setRingerMode(context, AudioManager.RINGER_MODE_SILENT);
                Toast.makeText(context, "Ringer mode is silent" ,Toast.LENGTH_SHORT).show();
            }
        }
        else if(ringerMode == AudioManager.RINGER_MODE_SILENT){
            setRingerMode(context, AudioManager.RINGER_MODE_NORMAL);
            Toast.makeText(context, "Ringer mode is normal" ,Toast.LENGTH_SHORT).show();
        }
    }

    //endregion Ringer Mode Methods

}
