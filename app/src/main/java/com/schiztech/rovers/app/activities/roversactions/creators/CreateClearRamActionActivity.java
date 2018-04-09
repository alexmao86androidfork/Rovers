package com.schiztech.rovers.app.activities.roversactions.creators;

import android.content.Intent;
import android.os.Bundle;

import com.schiztech.rovers.api.RoversActionBuilder;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.base.ActivityBase;
import com.schiztech.rovers.app.activities.roversactions.ClearRamActionActivity;

public class CreateClearRamActionActivity extends ActivityBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent.ShortcutIconResource iconResource =
                Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_roveraction_clearram);


        Intent launchIntent = new Intent(this, ClearRamActionActivity.class);

        Intent result = RoversActionBuilder.build()
                .setColor(getResources().getColor(R.color.md_cyan_A700))
                .setIconResource(iconResource)
                .setLabel(getResources().getString(R.string.roveraction_clearram_label))
                .setIntent(launchIntent)
                .create();

        setResult(RESULT_OK, result);

        finish();
    }
}
