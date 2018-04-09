package com.schiztech.rovers.actions.battery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;


public class CreateBatteryActionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent.ShortcutIconResource iconResource =
                Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher);


        Intent launchIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);

        ExtendedRoverActionBuilder builder =
                (ExtendedRoverActionBuilder) ExtendedRoverActionBuilder.build()
                .setColor(Color.DKGRAY)
                .setIconResource(iconResource)
                .setLabel(getResources().getString(R.string.roveraction_battery_label))
                .setIntent(launchIntent);
        Intent result =
                builder.setIsInteractive(true)
                .setContentUri(BatteryLevelProvider.CONTENT_URI.toString())
                .setIsColorInteractive(true)
                .setIsIconInteractive(true)
                .create();


        setResult(RESULT_OK, result);

        finish();

    }

}
