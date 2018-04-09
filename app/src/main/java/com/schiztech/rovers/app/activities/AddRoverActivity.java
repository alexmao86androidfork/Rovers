package com.schiztech.rovers.app.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;


import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.schiztech.rovers.api.RoversConstants;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.base.ActivityBase;
import com.schiztech.rovers.app.fragments.selectors.SelectColorFragment;
import com.schiztech.rovers.app.fragments.selectors.SelectIconFragment;
import com.schiztech.rovers.app.fragments.selectors.IntentSelectorFactory;
import com.schiztech.rovers.app.fragments.selectors.SelectIntentFragment;
import com.schiztech.rovers.app.fragments.selectors.SelectDesignFragment;
import com.schiztech.rovers.app.fragments.selectors.SelectTypeFragment;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.roveritems.ActionRover;
import com.schiztech.rovers.app.roveritems.ApplicationRover;
import com.schiztech.rovers.app.roveritems.ExecutableRover;
import com.schiztech.rovers.app.roveritems.FolderRover;
import com.schiztech.rovers.app.roveritems.IRover;
import com.schiztech.rovers.app.roveritems.InteractiveActionRover;
import com.schiztech.rovers.app.roveritems.RoversManager;
import com.schiztech.rovers.app.roveritems.ShortcutRover;
import com.schiztech.rovers.app.ui.LockedContentLayout;
import com.schiztech.rovers.app.utils.ExtensionsUtils;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.RoverlyticsUtils;
import com.schiztech.rovers.app.utils.RoversUtils;
import com.schiztech.rovers.app.utils.Utils;
import com.schiztech.rovers.app.windows.FloatingWindowsManager;
import com.schiztech.rovers.app.windows.helpers.RoverWindowHelper;

import wei.mark.standout.StandOutWindow;

public class AddRoverActivity extends ActivityBase implements
        //region Listeners
        SelectColorFragment.OnColorSelectedListener,
        SelectIconFragment.OnIconSelectedListener,
        SelectDesignFragment.OnChangeColorRequestListener,
        SelectDesignFragment.OnChangeIconRequestListener,
        SelectDesignFragment.OnBackToDefaultListener,
        SelectTypeFragment.OnTypeSelectedListener,
        SelectIntentFragment.OnIntentSelectedListener
        //endregion Listeners
{

    private static final String TAG = LogUtils.makeLogTag("AddRoverActivity");
    public static final String ROVERS_COUNT_KEY = "rovers_count_key";

    final int MAX_ROVER_ITEMS = 10;
    final int WARNING_ROVER_ITEMS = 8;

    boolean mIsSecondaryFragmentShowing = false;
    Fragment mDisplayedSecondaryFragment = null;
    boolean mIsSecondaryIsColor = false;
    boolean mIsSecondaryIsIcon = false;

    Utils.RoverType mRoverType;
    SelectDesignFragment mDesignFragment;

    Button mCancelButton;
    Button mAddButton;

    View mAddedRoversLayout;
    Button mAddedRoversGetUnlimitedButton;
    TextView mAddedRoversDescription;

    LockedContentLayout mLockedContentLayout;

    int mRoversCount = 0;

    //region Layout//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rover);

        if(getIntent() != null) {
            mRoversCount = getIntent().getIntExtra(ROVERS_COUNT_KEY, mRoversCount);
        }


        mAddedRoversLayout = findViewById(R.id.addRover_warningLayout);
        mAddedRoversGetUnlimitedButton = (Button)findViewById(R.id.addRover_getExtensionBtn);
        mAddedRoversDescription = (TextView)findViewById(R.id.addRover_warningDescription);
        initAddedRoverItemsLayout();

        mCancelButton = (Button)findViewById(R.id.addRover_cancelButton);
        mAddButton = (Button)findViewById(R.id.addRover_addButton);
        mCancelButton.setOnClickListener(mCancelButtonClick);
        mAddButton.setVisibility(View.GONE);
        mAddButton.setOnClickListener(mAddButtonClick);


        mLockedContentLayout = (LockedContentLayout)findViewById(R.id.addRover_lockedContentLayout);
        initLockedContentLayout();

        showSelectTypeFragment();

        registerCloseReceiver();
    }



    @Override
    protected void onDestroy(){
        unregisterCloseReceiver();

        mDesignFragment = null;
        mAddedRoversDescription = null;
        mAddedRoversGetUnlimitedButton.setOnClickListener(null);
        mAddedRoversGetUnlimitedButton = null;
        mAddButtonClick = null;
        mAddButton.setOnClickListener(null);
        mAddButton = null;
        mCancelButtonClick = null;
        mCancelButton.setOnClickListener(null);
        mCancelButton = null;

        if(mLockedContentLayout !=null){
            mLockedContentLayout.setOnGetExtensiosnClickedListener(null);
            mLockedContentLayout = null;
        }

        Utils.unbindDrawables(findViewById(R.id.addRover_mainLayout));

        super.onDestroy();
    }

    private void switchMainFragment(Fragment fragment){
        hideSecondaryFragment();
        if(fragment == null) {
            LogUtils.LOGW(TAG, "can't switch main fragment to NULL object");
            return;
        }

        LogUtils.LOGV(TAG, "switching main to: " + fragment.getTag());

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.addRover_fragmentPrimary, fragment);
        fragmentTransaction.commit();
    }

    private void showSecondaryFragment(Fragment fragment){
        if(fragment == null) {
            LogUtils.LOGW(TAG, "can't show secondary fragment with NULL object");
            return;
        }

        LogUtils.LOGV(TAG, "showing secondary fragment: " + fragment.getTag());

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.addRover_fragmentSecondary, fragment);
        fragmentTransaction.commit();

        mDisplayedSecondaryFragment = fragment;

        View v = findViewById(R.id.addRover_fragmentSecondary);
        v.setVisibility(View.VISIBLE);

        mIsSecondaryFragmentShowing = true;
    }

    private void hideSecondaryFragment(){
        final View v = findViewById(R.id.addRover_fragmentSecondary);
        v.setVisibility(View.GONE);

        if(mDisplayedSecondaryFragment != null){
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.remove(mDisplayedSecondaryFragment);
            fragmentTransaction.commit();

            mDisplayedSecondaryFragment = null;
        }

        mIsSecondaryFragmentShowing = false;
        mIsSecondaryIsColor = false;
        mIsSecondaryIsIcon = false;

        if(mDesignFragment != null)
            mDesignFragment.onSecondaryFragmentDismissed();
    }


    //endregion Layout

    private IRover getRoverItem(Intent intent, IRover.RoverType type){
        if (type == IRover.RoverType.Shortcut) {
            LogUtils.LOGV(TAG, "Rover is Shortcut");
            return new ShortcutRover(intent, getApplicationContext());
        }
        if (type == IRover.RoverType.Application) {
            LogUtils.LOGV(TAG, "Rover is Application");
            return new ApplicationRover(intent,getApplicationContext());
        }

        if(type == IRover.RoverType.Folder){
            LogUtils.LOGV(TAG, "Rover is Folder");
            FolderRover newFolder = new FolderRover(getApplicationContext());

            newFolder.setLabel("Folder #" + newFolder.getFolderID());

            return newFolder;
        }

        if(type == IRover.RoverType.BasicAction){
            LogUtils.LOGV(TAG, "Rover is BasicAction");
            return new ActionRover(intent, getApplicationContext());
        }

        if(type == IRover.RoverType.InteractiveAction){
            LogUtils.LOGV(TAG, "Rover is InteractiveAction");
            return new InteractiveActionRover(intent, getApplicationContext());
        }

        LogUtils.LOGW(TAG, "Failed to get Rover type");
        return null;
    }

    //region OnClick Listeners

    private View.OnClickListener mAddButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LogUtils.LOGD(TAG, "Add Button Clicked");

            AnalyticsManager.getInstance(getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                    AnalyticsManager.Action.Button_Click,
                    "AddRover_Add");

            if(mDesignFragment != null){
                IRover newRover = mDesignFragment.getRover();
                if(newRover != null){
                    //send new rover to the service
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(RoverWindowHelper.DATA_KEY_ADD_ROVER_TYPE, newRover.getClass());
                    bundle.putString(RoverWindowHelper.DATA_KEY_ADD_ROVER_DATA, RoversManager.roverToGson(mDesignFragment.getRover()));
                    StandOutWindow.sendData(getApplicationContext(),
                            FloatingWindowsManager.class,
                            RoverWindowHelper.WINDOW_ID_ROVER,
                            RoverWindowHelper.DATA_REQ_ADD_ROVER,
                            bundle,
                            FloatingWindowsManager.class,
                            FloatingWindowsManager.DISREGARD_ID
                    );

                    if(mRoverType != null) {
                        AnalyticsManager.getInstance(getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                                AnalyticsManager.Action.Rovers,
                                "Added", (long)mRoverType.ordinal());

                        LogUtils.LOGI(TAG, "Sent added Rover of type " + mRoverType +" to service.");
                    }


                    clearAndExit();
                }
                else{
                    LogUtils.LOGW(TAG, "Added Rover seems to be a NULL object");
                }
            }
            else{
                LogUtils.LOGW(TAG, "Design fragment seems to be a NULL object");
            }
        }
    };
    private View.OnClickListener mCancelButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LogUtils.LOGV(TAG, "Cancel Button Clicked");

            AnalyticsManager.getInstance(getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                    AnalyticsManager.Action.Button_Click,
                    "AddRover_Cancel");

            clearAndExit();
        }
    };


    private void clearAndExit(){
        try {
            //empty fragments
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            finish();
            System.gc();
        }
        catch (Exception e){
            LogUtils.LOGE(TAG, "ERROR while clearAndExit: " + e.getMessage());
            finish();
        }
    }

    //endregion OnClick Listeners

    //region Choose Type Fragment

    private void showSelectTypeFragment(){
        SelectTypeFragment typeFragment = SelectTypeFragment.newInstance();
        switchMainFragment(typeFragment);
    }

    @Override
    public void onTypeSelected(Utils.RoverType type) {
        LogUtils.LOGD(TAG, "Selected Rover type: " + type);

        mRoverType = type;
        if(type != Utils.RoverType.Folder)
            showChooseIntentFragment(type);
        else
            showChooseDesignFragment(null, IRover.RoverType.Folder);
    }

    //endregion

    //region Choose Intent Fragment
    private void showChooseIntentFragment(Utils.RoverType type){
        SelectIntentFragment intentFragment = IntentSelectorFactory.getIntentSelector(type);
        switchMainFragment(intentFragment);
    }

    @Override
    public void onIntentSelected(Intent selectedIntent, IRover.RoverType roverItemType) {
        LogUtils.LOGD(TAG, "Selected Rover intent: " + selectedIntent.toString());
        showChooseDesignFragment(selectedIntent, roverItemType);
    }

    //endregion

    //region Choose Design Fragment

    private void showChooseDesignFragment(Intent intent, IRover.RoverType  type){
        IRover rover = getRoverItem(intent, type);

        //only move on if the rover has valid permissions
        if(validateRoverPermissions(rover)) {
            if (rover != null) {
                mDesignFragment = SelectDesignFragment.newInstance();
                mDesignFragment.setRover(rover);
                switchMainFragment(mDesignFragment);
                mAddButton.setVisibility(View.VISIBLE);
            }
            else{
                LogUtils.LOGW(TAG, "Rover item seems to be a NULL object. Staying at the choose Rover fragment.");
            }
        }

    }


    @Override
    public void OnChangeColorRequest(int currentColor, int defaultColor) {
        LogUtils.LOGD(TAG, "change color request. current color: " + currentColor + ", default: " + defaultColor);

        if(!mIsSecondaryIsColor) {
            showChooseColorFragment(currentColor,defaultColor);
        }
        else
            hideSecondaryFragment();
    }

    @Override
    public void OnChangeIconRequest() {
        if(!mIsSecondaryIsIcon) {
            showChooseIconFragment();
        }
        else
            hideSecondaryFragment();
    }

    @Override
    public void OnBackToDefault() {
        hideSecondaryFragment();
    }
    //endregion

    //region Choose Color Fragment

    @Override
    public void onColorSelected(int selectedColor) {
        LogUtils.LOGD(TAG, "selected color: " + selectedColor);

        mDesignFragment.setColor(selectedColor);
//        hideSecondaryFragment();
    }

    private void showChooseColorFragment(int currentColor, int defaultColor) {
        Fragment fragment = SelectColorFragment.newInstance();
        Bundle arguments = new Bundle();
        arguments.putInt("currentColor", currentColor);
        arguments.putInt("defaultColor", defaultColor);

        fragment.setArguments(arguments);

        showSecondaryFragment(fragment);

        mIsSecondaryIsIcon = false;
        mIsSecondaryIsColor = true;

    }

    //endregion Choose Color

    //region Choose Icon Fragment

    @Override
    public void onIconSelected(RoversUtils.RoverIcon selectedIcon) {
        LogUtils.LOGD(TAG, "selected icon: " + selectedIcon);

        mDesignFragment.setIcon(selectedIcon);
//        hideSecondaryFragment();
    }

    private void showChooseIconFragment() {
        Fragment fragment = SelectIconFragment.newInstance();
        showSecondaryFragment(fragment);

        mIsSecondaryIsIcon = true;
        mIsSecondaryIsColor = false;
    }

    //endregion

    private void initAddedRoverItemsLayout(){
        if(mAddedRoversLayout == null || mAddedRoversGetUnlimitedButton == null || mAddedRoversDescription == null)
            return;

        //show warning only if above or equal to warning level or if has more rovers extension
        if(WARNING_ROVER_ITEMS > mRoversCount || ExtensionsUtils.isGotMoreRovers(getApplicationContext())){
            mAddedRoversLayout.setVisibility(View.GONE);
        }
        else {
            mAddedRoversLayout.setVisibility(View.VISIBLE);
            String descriptionFormat;
            if(mRoversCount <= MAX_ROVER_ITEMS) {
                descriptionFormat = "%d " + Utils.getString(getApplicationContext(), R.string.addrover_rovers_added)
                        + " (%d " + Utils.getString(getApplicationContext(), R.string.addrover_remaining) + ")";
                mAddedRoversDescription.setText(String.format(descriptionFormat, mRoversCount, MAX_ROVER_ITEMS - mRoversCount));
            }
            else{//due to a bug, we need to take care of people who added more than the max...
                descriptionFormat = "%d " + Utils.getString(getApplicationContext(), R.string.addrover_rovers_added)
                        + " (" + Utils.getString(getApplicationContext(), R.string.addrover_limit) + " %d)";

                mAddedRoversDescription.setText(String.format(descriptionFormat, mRoversCount, MAX_ROVER_ITEMS));
            }

            if (MAX_ROVER_ITEMS <= mRoversCount) {
                //the locked content will be visible anyways.. no need for double link to extensions screen.
                mAddedRoversGetUnlimitedButton.setVisibility(View.GONE);
            } else {
                mAddedRoversGetUnlimitedButton.setVisibility(View.VISIBLE);
                mAddedRoversGetUnlimitedButton.setOnClickListener(mOnGetUnlimitedRoversClicked);
            }
        }//
    }

    private View.OnClickListener mOnGetUnlimitedRoversClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ExtensionsUtils.navigateToExtensionsScreen(getApplicationContext());

            AnalyticsManager.getInstance(getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                    AnalyticsManager.Action.Button_Click,
                    "AddRover_GetUnlimitedRovers");

            finish();

        }
    };

    private void initLockedContentLayout(){
        if(mLockedContentLayout == null) return;

        //check if "more rovers" purchased to determine if showing the warning.
        if(ExtensionsUtils.isGotMoreRovers(getApplicationContext()) || mRoversCount < MAX_ROVER_ITEMS){
            mLockedContentLayout.hide();
        }

        else {
            mLockedContentLayout.setExtensionType(ExtensionsUtils.ExtensionType.MoreRovers);
            mLockedContentLayout.setOnGetExtensiosnClickedListener(new LockedContentLayout.OnGetExtensionsClickedListener() {
                @Override
                public void onGetExtensionsClicked() {
                    finish();//close this activity
                }
            });
            mLockedContentLayout.show();
        }
    }




    @Override
    public void onBackPressed() {

        if(mIsSecondaryFragmentShowing)
            hideSecondaryFragment();

        else
            super.onBackPressed();
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

    private boolean validateRoverPermissions(IRover rover) {

        if(rover instanceof ExecutableRover){
            Intent launchIntent = ((ExecutableRover)rover).getLaunchIntent();
            if(launchIntent != null){
                String action = launchIntent.getAction();
                if(action != null){
                    if(action.equals(Intent.ACTION_CALL) || action.equals(Intent.ACTION_DIAL)) {
                        showDialogCallActionError();
                        return false;
                    }
                }
            }
        }

        return true;
    }


    private void showDialogCallActionError(){
        int showTime = 8 * 1000; // 8 seconds
        int intervalTime = 1000;
        final Toast toast = Toast.makeText(getApplicationContext(), Utils.getString(getApplicationContext(), R.string.addrover_dialcall_action_error), Toast.LENGTH_SHORT);
        toast.show();
        new CountDownTimer(showTime, intervalTime)
        {
            public void onTick(long millisUntilFinished) {toast.show();}
            public void onFinish() {toast.cancel();}
        }.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.LOGD(TAG, "onActivityResult request: " + requestCode + ", result: " + resultCode);

        super.onActivityResult(requestCode, resultCode, data);

    }


}
