package com.schiztech.rovers.app.activities.roversactions.creators;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.schiztech.rovers.api.RoversActionBuilder;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.base.ActivityBase;

public class CreateHomeActionActivity extends ActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent result = createHomeActionIntent(getApplicationContext());
        setResult(RESULT_OK, result);

        finish();
    }
    public static Intent createHomeActionIntent(Context context){
        Intent.ShortcutIconResource iconResource =
                Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_roveraction_home);


        Intent launchIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);

        Intent result = RoversActionBuilder.build()
                .setColor(context.getResources().getColor(R.color.md_blue_grey_800))
                .setIconResource(iconResource)
                .setLabel(context.getResources().getString(R.string.roveraction_home_label))
                .setIntent(launchIntent)
                .create();

        return result;
    }




}
