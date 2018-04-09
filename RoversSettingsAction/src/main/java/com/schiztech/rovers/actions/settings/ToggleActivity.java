package com.schiztech.rovers.actions.settings;

import android.app.Activity;
import android.os.Bundle;


public class ToggleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null) {
            String stringType = getIntent().getExtras().getString("type");
            Utils.SettingsTypes type = Utils.SettingsTypes.valueOf(stringType);
            if (type != null) {
                switch (type) {
                    case Bluetooth:
                        Utils.toggleBluetooth(getApplicationContext());
                        break;
                    case Wifi:
                        Utils.toggleWifi(getApplicationContext());
                        break;
                    case AutoRotation:
                        Utils.toggleAutoRotate(getApplicationContext());
                        break;
                    case RingerMode:
                        Utils.toggleRingerMode(getApplicationContext());
                        break;
                }
            }
        }
        finish();
    }



}
