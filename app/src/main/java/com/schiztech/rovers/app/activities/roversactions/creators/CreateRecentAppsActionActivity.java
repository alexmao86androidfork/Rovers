package com.schiztech.rovers.app.activities.roversactions.creators;

import android.content.Intent;
import android.os.Bundle;

import com.schiztech.rovers.api.RoversActionBuilder;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.base.ActivityBase;
import com.schiztech.rovers.app.activities.roversactions.RecentAppsActionActivity;

public class CreateRecentAppsActionActivity extends ActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent.ShortcutIconResource iconResource =
                Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_roveraction_recents);


        Intent launchIntent = new Intent(this, RecentAppsActionActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        Intent result = RoversActionBuilder.build()
                .setColor(getResources().getColor(R.color.md_pink_A400))
                .setIconResource(iconResource)
                .setLabel(getResources().getString(R.string.roveraction_recentapps_label))
                .setIntent(launchIntent)
                .create();

        setResult(RESULT_OK, result);

        finish();
    }
}
