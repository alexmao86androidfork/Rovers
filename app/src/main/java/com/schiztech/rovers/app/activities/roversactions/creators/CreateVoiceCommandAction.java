package com.schiztech.rovers.app.activities.roversactions.creators;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.schiztech.rovers.api.RoversActionBuilder;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.base.ActivityBase;
import com.schiztech.rovers.app.utils.LogUtils;

public class CreateVoiceCommandAction extends ActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent.ShortcutIconResource iconResource =
                Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_roveraction_voice);


        //first option - voice search (Google Now)
        Intent launchIntent = new Intent("android.intent.action.VOICE_ASSIST").addCategory(Intent.CATEGORY_DEFAULT);
        if(getPackageManager().resolveActivity(launchIntent, PackageManager.MATCH_DEFAULT_ONLY) == null){//second option - default voice command
            launchIntent = new Intent(Intent.ACTION_VOICE_COMMAND).addCategory(Intent.CATEGORY_DEFAULT);
            LogUtils.LOGE("VOice Commands", "HHELLOOOLOLOL");
        }
        if(getPackageManager().resolveActivity(launchIntent, PackageManager.MATCH_DEFAULT_ONLY) == null){//third option - default search
            launchIntent = new Intent(Intent.ACTION_SEARCH).addCategory(Intent.CATEGORY_DEFAULT);
        }

        Intent result = RoversActionBuilder.build()
                .setColor(getResources().getColor(R.color.md_deep_orange_A400))
                .setIconResource(iconResource)
                .setLabel(getResources().getString(R.string.roveraction_voicecommands_label))
                .setIntent(launchIntent)
                .create();

        setResult(RESULT_OK, result);

        finish();
    }



}
