package com.schiztech.rovers.actions.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.schiztech.roversettingsaction.R;


public class CreateSettingsActionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showSelectionDialog();
    }

    private void createToggleAction(Utils.SettingsTypes type) {
        int iconResource = R.drawable.ri_settings_gear;
        ExtendedRoverActionBuilder action = null;
        switch (type) {
            case Wifi:
                action = getWifiToggleAction();
                iconResource = getWifiToggleIcon();
                break;
            case RingerMode:
                action = getRingerModeToggleAction();
                iconResource = getRingerModeToggleIcon();
                break;
            case Brightness:
                action = getBrightnessToggleAction();
                iconResource = getBrightnessToggleIcon();
                break;
            case AutoRotation:
                action = getRotationToggleAction();
                iconResource = getRotationToggleIcon();
                break;
            case Bluetooth:
                action = getBluetoothToggleAction();
                iconResource = getBluetoothToggleIcon();
                break;
            default:
                action = null;
                iconResource = R.drawable.ri_settings_gear;
                break;
        }

        if(action != null) {
            Intent.ShortcutIconResource shortcutIconResource = Intent.ShortcutIconResource.fromContext(this, iconResource);

            Intent launchIntent = new Intent(this, ToggleActivity.class);
            launchIntent.putExtra("type", type.toString());

            Intent result = action
                    .setIsInteractive(true)
                    .setIsColorInteractive(false)
                    .setIsIconInteractive(true)
                    .setColor(getResources().getColor(R.color.rover_default_background))
                    .setIconResource(shortcutIconResource)
                    .create();

            setResult(RESULT_OK, result);
        }

    }

    //region Toggles Actions & Icons

    private ExtendedRoverActionBuilder getWifiToggleAction() {
        Intent launchIntent = new Intent(this, ToggleActivity.class);
        launchIntent.putExtra("type", Utils.SettingsTypes.Wifi.toString());

        ExtendedRoverActionBuilder builder = (ExtendedRoverActionBuilder) ExtendedRoverActionBuilder.build();

        return (ExtendedRoverActionBuilder) builder
                .setContentUri(SettingsStatesProvider.CONTENT_URI_WIFI.toString())
                .setLabel(getResources().getString(R.string.roveraction_wifi_label))
                .setIntent(launchIntent);


    }

    private int getWifiToggleIcon() {
        return R.drawable.ic_wifi_on;
    }

    private ExtendedRoverActionBuilder getBluetoothToggleAction() {
        Intent launchIntent = new Intent(this, ToggleActivity.class);
        launchIntent.putExtra("type", Utils.SettingsTypes.Bluetooth.toString());

        ExtendedRoverActionBuilder builder = (ExtendedRoverActionBuilder) ExtendedRoverActionBuilder.build();

        return (ExtendedRoverActionBuilder) builder
                .setContentUri(SettingsStatesProvider.CONTENT_URI_BT.toString())
                .setLabel(getResources().getString(R.string.roveraction_bluetooth_label))
                .setIntent(launchIntent);
    }

    private int getBluetoothToggleIcon() {
        return R.drawable.ic_bluetooth_on;
    }

    private ExtendedRoverActionBuilder getRotationToggleAction() {
        Intent launchIntent = new Intent(this, ToggleActivity.class);
        launchIntent.putExtra("type", Utils.SettingsTypes.AutoRotation.toString());

        ExtendedRoverActionBuilder builder = (ExtendedRoverActionBuilder) ExtendedRoverActionBuilder.build();

        return (ExtendedRoverActionBuilder) builder
                .setContentUri(SettingsStatesProvider.CONTENT_URI_ROTATE.toString())
                .setLabel(getResources().getString(R.string.roveraction_rotation_label))
                .setIntent(launchIntent);
    }

    private int getRotationToggleIcon() {
        return R.drawable.ic_rotation_on;
    }

    private ExtendedRoverActionBuilder getRingerModeToggleAction() {
        Intent launchIntent = new Intent(this, ToggleActivity.class);
        launchIntent.putExtra("type", Utils.SettingsTypes.RingerMode.toString());

        ExtendedRoverActionBuilder builder = (ExtendedRoverActionBuilder) ExtendedRoverActionBuilder.build();

        return (ExtendedRoverActionBuilder) builder
                .setContentUri(SettingsStatesProvider.CONTENT_URI_RINGERMODE.toString())
                .setLabel(getResources().getString(R.string.roveraction_ringermode_label))
                .setIntent(launchIntent);
    }

    private int getRingerModeToggleIcon() {
        return R.drawable.ic_ringer_normal;
    }

    private ExtendedRoverActionBuilder getBrightnessToggleAction() {
        Intent launchIntent = new Intent(this, BrightnessActivity.class);
        launchIntent.putExtra("type", Utils.SettingsTypes.Brightness.toString());

        ExtendedRoverActionBuilder builder = (ExtendedRoverActionBuilder) ExtendedRoverActionBuilder.build();

        return (ExtendedRoverActionBuilder) builder
                .setContentUri(SettingsStatesProvider.CONTENT_URI_BRIGHTNESS.toString())
                .setLabel(getResources().getString(R.string.roveraction_brightness_label))
                .setIntent(launchIntent);
    }

    private int getBrightnessToggleIcon() {
        return R.drawable.ic_brightness_high;
    }

    //endregion Toggles Actions & Icons

    //region Selection Dialog

    public static class Item {
        public final String mText;
        public final int mIcon;
        Utils.SettingsTypes mType;

        public Item(String text, Integer icon, Utils.SettingsTypes type) {
            mText = text;
            mIcon = icon;
            mType = type;
        }

        @Override
        public String toString() {
            return mText;
        }
    }

    Item[] items;
    private Item[] getItems(){
        if(items == null) {
            items = new Item[]{
                    new Item(getResources().getString(R.string.roveraction_wifi_label), getWifiToggleIcon(), Utils.SettingsTypes.Wifi),
                    new Item(getResources().getString(R.string.roveraction_bluetooth_label), getBluetoothToggleIcon(), Utils.SettingsTypes.Bluetooth),
                    new Item(getResources().getString(R.string.roveraction_brightness_label), getBrightnessToggleIcon(), Utils.SettingsTypes.Brightness),
                    new Item(getResources().getString(R.string.roveraction_rotation_label), getRotationToggleIcon(), Utils.SettingsTypes.AutoRotation),
                    new Item(getResources().getString(R.string.roveraction_ringermode_label), getRingerModeToggleIcon(), Utils.SettingsTypes.RingerMode)

            };
        }

        return items;
    }

    private ListAdapter getItemAdapter() {
        ListAdapter adapter = new ArrayAdapter<Item>(
                this,
                android.R.layout.select_dialog_item,
                android.R.id.text1,
                getItems()) {
            public View getView(int position, View convertView, ViewGroup parent) {
                //User super class to create the View
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v.findViewById(android.R.id.text1);
                tv.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_DeviceDefault_Medium_Inverse);
                //Put the image on the TextView
                tv.setCompoundDrawablesWithIntrinsicBounds(getItems()[position].mIcon, 0, 0, 0);

                //Add margin between image and text (support various screen densities)
                int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                tv.setCompoundDrawablePadding(dp5);

                return v;
            }
        };

        return adapter;
    }

    @SuppressLint("NewApi")
    private void showSelectionDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth)

                .setTitle("Select Toggle")
                .setAdapter(getItemAdapter(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        createToggleAction(getItems()[item].mType);
                        dialog.dismiss();
                    }
                });
        if(Utils.isAndroidVersionEqualOrAbove(Build.VERSION_CODES.JELLY_BEAN_MR1)) {
            alertBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    finish();
                }
            });
        }

        else{
            alertBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    finish();
                }
            });
        }

        alertBuilder.show();


    }

    //endregion Selection Dialog
}
