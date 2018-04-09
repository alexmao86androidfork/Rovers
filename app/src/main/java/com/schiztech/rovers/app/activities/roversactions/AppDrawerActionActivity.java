package com.schiztech.rovers.app.activities.roversactions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.schiztech.rovers.api.RoversConstants;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.base.ActivityBase;
import com.schiztech.rovers.app.fragments.selectors.IntentSelectorFactory;
import com.schiztech.rovers.app.fragments.selectors.SelectIntentFragment;
import com.schiztech.rovers.app.roveritems.IRover;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.Utils;

public class AppDrawerActionActivity extends ActivityBase implements SelectIntentFragment.OnIntentSelectedListener {
    private static final String TAG = LogUtils.makeLogTag("AppDrawerActionActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_drawer);
        SelectIntentFragment fragment = IntentSelectorFactory.getIntentSelector(Utils.RoverType.App);
        fragment.setIsLightTheme(true);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }

        registerCloseReceiver();
    }

    @Override
    public void onIntentSelected(Intent selectedIntent, IRover.RoverType roverItemType) {
        selectedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(selectedIntent);

        finish();
    }
    @Override
    protected void onDestroy(){
        unregisterCloseReceiver();

        super.onDestroy();
    }

    //region Broadcast Receiver

    private BroadcastReceiver mCloseDialogReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    private void registerCloseReceiver(){
        registerReceiver(mCloseDialogReceiver, new IntentFilter(RoversConstants.ROVERS_EXPANDED_RECEIVER));
    }

    private void unregisterCloseReceiver(){
        unregisterReceiver(mCloseDialogReceiver);
        mCloseDialogReceiver = null;
    }




    //endregion
}
