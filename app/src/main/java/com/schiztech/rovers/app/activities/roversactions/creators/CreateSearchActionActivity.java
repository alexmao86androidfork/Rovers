package com.schiztech.rovers.app.activities.roversactions.creators;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.schiztech.rovers.api.RoversActionBuilder;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.base.ActivityBase;
import com.schiztech.rovers.app.utils.Utils;

public class CreateSearchActionActivity extends ActivityBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent result = createSearchActionIntent(getApplicationContext());
        setResult(RESULT_OK, result);

        finish();
    }

    public static Intent createSearchActionIntent(Context context){
        Intent.ShortcutIconResource iconResource =
                Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_roveraction_search);

        Intent launchIntent = Utils.isAndroidVersionEqualOrAbove(Build.VERSION_CODES.JELLY_BEAN) ?
                new Intent(Intent.ACTION_ASSIST).addCategory(Intent.CATEGORY_DEFAULT)
                : new Intent(Intent.ACTION_SEARCH).addCategory(Intent.CATEGORY_DEFAULT);

        Intent result = RoversActionBuilder.build()
                .setColor(context.getResources().getColor(R.color.md_red_A700))
                .setIconResource(iconResource)
                .setLabel(context.getResources().getString(R.string.roveraction_search_label))
                .setIntent(launchIntent)
                .create();

        return result;
    }
}
