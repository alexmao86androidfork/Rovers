package com.schiztech.rovers.actions.flashlight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.schiztech.roverflashlightaction.R;
import com.schiztech.rovers.api.RoversActionBuilder;


public class CreateFlashlightActionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent.ShortcutIconResource iconResource =
                Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_flashlight_orange);


        Intent launchIntent = new Intent(this, ToggleFlashlightActivity.class);

        Intent result = RoversActionBuilder.build()
                .setColor(getResources().getColor(R.color.flashlight_default_background))
                .setIconResource(iconResource)
                .setLabel(getResources().getString(R.string.roveraction_flashlight_label))
                .setIntent(launchIntent)
                .create();

        setResult(RESULT_OK, result);

        finish();
    }
}
