package com.schiztech.rovers.app.activities.roversactions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.schiztech.rovers.api.RoversConstants;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.base.ActivityBase;
import com.schiztech.rovers.app.fragments.selectors.SelectRecentFragment;
import com.schiztech.rovers.app.roveritems.IRover;
import com.schiztech.rovers.app.utils.Utils;

import java.lang.reflect.Method;

public class RecentAppsActionActivity extends ActivityBase implements SelectRecentFragment.OnIntentSelectedListener {
    boolean mIsRegisteredBroadcast = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_apps);


        if (Utils.isAndroidVersionEqualOrAbove(Build.VERSION_CODES.LOLLIPOP)) {
            Toast.makeText(getApplicationContext(), R.string.roveraction_notsupported, Toast.LENGTH_SHORT).show();
            finish();
            return;

            //recents hack for lollipop, needs solving of not able to hide this activity from the list.
//            try {
//                Class serviceManagerClass = Class.forName("android.os.ServiceManager");
//                Method getService = serviceManagerClass.getMethod("getService", String.class);
//                IBinder retbinder = (IBinder) getService.invoke(serviceManagerClass, "statusbar");
//                Class statusBarClass = Class.forName(retbinder.getInterfaceDescriptor());
//                Object statusBarObject = statusBarClass.getClasses()[0].getMethod("asInterface", IBinder.class).invoke(null, new Object[] { retbinder });
//                Method clearAll = statusBarClass.getMethod("toggleRecentApps");
//                clearAll.setAccessible(true);
//                clearAll.invoke(statusBarObject);
//            }
//            catch (Exception e){
//                ACRA.getErrorReporter().handleException(e);
//                Toast.makeText(getApplicationContext(), R.string.roveraction_notsupported, Toast.LENGTH_SHORT).show();
//            }
        }
        else {
            SelectRecentFragment fragment = new SelectRecentFragment();

            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .add(R.id.container, fragment)
                        .commit();
            }
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
    protected void onDestroy() {
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

    private void registerCloseReceiver() {
        registerReceiver(mCloseDialogReceiver, new IntentFilter(RoversConstants.ROVERS_EXPANDED_RECEIVER));
        mIsRegisteredBroadcast = true;
    }

    private void unregisterCloseReceiver() {
        if(mIsRegisteredBroadcast) {
            unregisterReceiver(mCloseDialogReceiver);
            mCloseDialogReceiver = null;
            mIsRegisteredBroadcast = false;
        }

    }


    //endregion
}
