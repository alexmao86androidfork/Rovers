package com.schiztech.rovers.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.os.Build;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.base.ActionBarActivityBase;
import com.schiztech.rovers.app.dialogs.TranslateDialog;
import com.schiztech.rovers.app.fragments.AboutFragment;
import com.schiztech.rovers.app.fragments.base.ActionBarFragmentBase;
import com.schiztech.rovers.app.fragments.ActionsFragment;
import com.schiztech.rovers.app.fragments.base.ContentRevealFragmentBase;
import com.schiztech.rovers.app.fragments.NavigationFragment;
import com.schiztech.rovers.app.fragments.RoverlyticsFragment;
import com.schiztech.rovers.app.fragments.SettingsFragment;
import com.schiztech.rovers.app.fragments.ExtensionsFragment;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.managers.BatchManager;
import com.schiztech.rovers.app.utils.BackableInterface;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.managers.OpenIabManager;
import com.schiztech.rovers.app.utils.PrefUtils;
import com.schiztech.rovers.app.utils.Utils;

import org.codechimp.apprater.AppRater;

import java.util.List;

public class MainActivity extends ActionBarActivityBase
        implements ActionBarFragmentBase.OnActionBarChangedListener,
        NavigationFragment.OnNavigationRequestListener,
        ContentRevealFragmentBase.OnFragmentStateChangedListener {
    private static final String TAG = LogUtils.makeLogTag("MainActivity");
    public static final String STATE_DISPLAYED_FRAGMENT = "displayedFragmentTag";
    public static final String STATE_STARTUP_FRAGMENT = "openingFragmentTag";


    boolean mIsTabletMode = false;
    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;
    String mDisplayedContentFragmentTag;

    //region Activity States

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isAndroidVersionEqualOrAbove(Build.VERSION_CODES.LOLLIPOP)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_main);

        //determine if tablet mode by checking for unique tablet view id
        mIsTabletMode = findViewById(R.id.tablet_layout) != null;

        if (savedInstanceState == null) {
            LogUtils.LOGV(TAG, "savedInstanceState is NULL");
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_containerPrimary, new NavigationFragment(), Utils.getString(getApplicationContext(), R.string.fragment_navigation))
                    .commit();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_navigationDrawer, new RoverlyticsFragment(), Utils.getString(getApplicationContext(), R.string.fragment_roverlytics))
                    .commit();
        }

        //setting default values if unset to pref values.
        PreferenceManager.setDefaultValues(getApplicationContext(), Utils.getString(getApplicationContext(), R.string.prefs_main), Context.MODE_MULTI_PROCESS, R.xml.main_prefs, false);

        initDrawer();
        initOpenIab();
        initWalkthrough();
        initAppRater();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        syncActionBarToggle();

        //if activity relaunched (orientation change) - show last displayed fragment
        if (savedInstanceState != null) {
            LogUtils.LOGV(TAG, "savedInstanceState not NULL");
            mDisplayedContentFragmentTag = savedInstanceState.getString(STATE_DISPLAYED_FRAGMENT, null);

            if (mDisplayedContentFragmentTag != null) {//!mIsTabletMode && mDisplayedContentFragmentTag != null) {
                LogUtils.LOGV(TAG, "onPostCreate | Showing " + mDisplayedContentFragmentTag);

                Fragment fragment = getSupportFragmentManager().findFragmentByTag(mDisplayedContentFragmentTag);
                if (fragment != null && fragment instanceof ActionBarFragmentBase) {
                    ((ActionBarFragmentBase) fragment).updateActionBar();
                }
            }
        }

        //if special startup fragment requested - show it
        else if (getIntent().getExtras() != null) {
            int fragTag = getIntent().getExtras().getInt(STATE_STARTUP_FRAGMENT, 0);
            if (fragTag != 0) {
                LogUtils.LOGD(TAG, "Showing requested startup fragment" + Utils.getString(getApplicationContext(), fragTag));
                showFragment(getFragmentInstance(fragTag, null), fragTag);
            }
        }


        if (mIsTabletMode && mDisplayedContentFragmentTag == null) {
            mDisplayedContentFragmentTag = Utils.getString(getApplicationContext(), R.string.fragment_settings);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_containerSecondary, new SettingsFragment(), Utils.getString(getApplicationContext(), R.string.fragment_settings))
                    .commit();
        }

    }

    @Override
    public void onDestroy() {
        BatchManager.getInstance(getApplicationContext()).onDestroy(this);

        mDrawerToggle = null;
        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerListener(null);
            Utils.unbindDrawables(mDrawerLayout);
            mDrawerLayout = null;
        }
        destroyOpenIab();
        System.gc();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        BatchManager.getInstance(getApplicationContext()).onStart(this);
    }

    @Override
    protected void onStop() {
        BatchManager.getInstance(getApplicationContext()).onStop(this);
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        BatchManager.getInstance(getApplicationContext()).onNewIntent(this, intent);

        super.onNewIntent(intent);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    //endregion Activity States

    //region Options Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        AnalyticsManager.Category category = AnalyticsManager.Category.UX;
        AnalyticsManager.Action action = AnalyticsManager.Action.MenuItem_Click;
        //drawer trigger
        if (mDrawerToggle.isDrawerIndicatorEnabled() &&
                mDrawerToggle.onOptionsItemSelected(item)) {
            LogUtils.LOGV(TAG, "OptionsMenu home clicked");
            AnalyticsManager.getInstance(getApplicationContext()).reportEvent(category, action, "Main_Home");
            return true;
        } else if (id == R.id.action_faq) {
            LogUtils.LOGV(TAG, "OptionsMenu FAQ clicked");
            AnalyticsManager.getInstance(getApplicationContext()).reportEvent(category, action, "Main_Faq");
            Utils.browseLink(getApplicationContext(), R.string.link_faq);
            return true;
        } else if (id == R.id.action_about) {
            LogUtils.LOGV(TAG, "OptionsMenu about clicked");
            AnalyticsManager.getInstance(getApplicationContext()).reportEvent(category, action, "Main_About");
            onNavigationRequest(NavigationFragment.NavigationType.About, new Point(Utils.getDisplayDimensions(getApplicationContext()).x, 0));
            return true;
        } else if (id == R.id.action_contact) {
            LogUtils.LOGV(TAG, "OptionsMenu contact clicked");
            AnalyticsManager.getInstance(getApplicationContext()).reportEvent(category, action, "Main_Contact");
            Utils.browseLink(getApplicationContext(), R.string.link_contact);
            return true;
        } else if (id == R.id.action_translate) {
            LogUtils.LOGV(TAG, "OptionsMenu translate clicked");
            AnalyticsManager.getInstance(getApplicationContext()).reportEvent(category, action, "Main_Translate");
            showTranslateDialog();
            return true;
        } else if (id == R.id.action_tutorial) {
            LogUtils.LOGV(TAG, "OptionsMenu tutorial clicked");
            AnalyticsManager.getInstance(getApplicationContext()).reportEvent(category, action, "Main_Tutorial");
            showWalkthrough();
            return true;
        }
        //close the shown content fragment
        else if (item.getItemId() == android.R.id.home && mDisplayedContentFragmentTag != null) {
            LogUtils.LOGV(TAG, "OptionsMenu back clicked");
            AnalyticsManager.getInstance(getApplicationContext()).reportEvent(category, action, "Main_Back");
            contentFragmentHandleBack();
            return true;
        }


        //something else
        return super.onOptionsItemSelected(item);
    }

    //endregion Options Menu

    private void initDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawerLayout);

        ViewCompat.setElevation(findViewById(R.id.main_navigationDrawer), getResources().getDimensionPixelSize(R.dimen.drawer_elevation));

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    //region Fragment Factory

    private void showFragment(Fragment fragment, @StringRes int fragmentTag) {
        showFragment(fragment, Utils.getString(getApplicationContext(), fragmentTag));
    }

    private void showFragment(Fragment fragment, String fragmentTag) {
        LogUtils.LOGV(TAG, "showing fragment: " + fragmentTag);
        if (fragment != null && (mDisplayedContentFragmentTag == null || !fragmentTag.equals(mDisplayedContentFragmentTag))) {
            LogUtils.LOGV(TAG, "adding fragment on top of navigation fragment");
            //if showing main fragment, add content fragment on top
            if (mDisplayedContentFragmentTag == null || mIsTabletMode) {
                mDisplayedContentFragmentTag = fragmentTag;
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.main_containerSecondary, fragment, mDisplayedContentFragmentTag)
                        .commit();
            }

            //if currently showing another content fragment, replace if with the new one
            else {
                LogUtils.LOGV(TAG, "replacing fragment with currently shown content fragment");
                mDisplayedContentFragmentTag = fragmentTag;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_containerSecondary, fragment, mDisplayedContentFragmentTag)
                        .commit();

            }
        }
    }

    private
    @StringRes
    int getFragmentTag(NavigationFragment.NavigationType type) {
        switch (type) {
            case Settings:
                return R.string.fragment_settings;
            case Actions:
                return R.string.fragment_actions;
            case Extensions:
                return R.string.fragment_extensions;
            case About:
                return R.string.fragment_about;

        }

        return 0;
    }

    private
    @StringRes
    int getFragmentTag(Fragment fragment) {
        if (fragment instanceof SettingsFragment)
            return R.string.fragment_settings;
        if (fragment instanceof ActionsFragment)
            return R.string.fragment_actions;
        if (fragment instanceof ExtensionsFragment)
            return R.string.fragment_extensions;
        if (fragment instanceof AboutFragment)
            return R.string.fragment_about;

        return 0;
    }

    private Fragment getFragmentInstance(@StringRes int fragmentTagRes, Point location) {
        switch (fragmentTagRes) {
            case R.string.fragment_settings:
                return (mIsTabletMode || location == null) ?
                        SettingsFragment.newInstance(mIsTabletMode) : SettingsFragment.newInstance(mIsTabletMode, location.x, location.y);
            case R.string.fragment_actions:
                return (mIsTabletMode || location == null) ?
                        ActionsFragment.newInstance(mIsTabletMode) : ActionsFragment.newInstance(mIsTabletMode, location.x, location.y);
            case R.string.fragment_extensions:
                return (mIsTabletMode || location == null) ?
                        ExtensionsFragment.newInstance(mIsTabletMode) : ExtensionsFragment.newInstance(mIsTabletMode, location.x, location.y);
            case R.string.fragment_about:
                return (mIsTabletMode || location == null) ?
                        AboutFragment.newInstance(mIsTabletMode) : AboutFragment.newInstance(mIsTabletMode, location.x, location.y);


        }

        LogUtils.LOGE(TAG, "Couldn't identify fragment tag. returning NULL object");
        return null;
    }

    private void removeContentFragments(String excludedFragment) {
        //on tablet mode we're not replacing content fragment but adding them on top so the animation will look good.
        //therefore, after the animation if done we need to remove the old fragment to clean our resources.
        if (mIsTabletMode) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();

            for (Fragment fragment : fragments) {

                if (fragment != null) {
                    // To save any of the fragments, add this check.
                    // A tag can be added as a third parameter to the fragment when you commit it
                    if (fragment.getTag() == null || fragment.getTag().equals(excludedFragment) ||
                            fragment.getTag().equals(Utils.getString(getApplicationContext(), R.string.fragment_navigation)) ||
                            fragment.getTag().equals(Utils.getString(getApplicationContext(), R.string.fragment_roverlytics)))
                        continue;

                    LogUtils.LOGV(TAG, "removing content fragment: " + fragment.getTag());
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }
            }
        }
    }

    //endregion Fragment Factory

    //region ActionBarFragmentBase.OnActionBarChangedListener
    @Override
    public void onActionBarChanged(Toolbar toolbar) {
        LogUtils.LOGV(TAG, "onActionBarChanged");

        if (!mIsTabletMode || mDisplayedContentFragmentTag == null) {
            setSupportActionBar(toolbar);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            syncActionBarToggle();
        } else if (mIsTabletMode == true && mDisplayedContentFragmentTag != null) {
            //hide home arrow on top after orientation change when tablet mode
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }
    }


    @Override
    public void onSystemColorChanged(@ColorRes int colorRes) {
        LogUtils.LOGV(TAG, "onSystemColorChanged");

        if (!mIsTabletMode || mDisplayedContentFragmentTag == null) {

            if (mDrawerLayout != null) {
                int systemColor = Utils.getColor(getApplicationContext(), colorRes);
                LogUtils.LOGV(TAG, "new system color: " + systemColor);

                mDrawerLayout.setStatusBarBackgroundColor(systemColor);
                mDrawerLayout.requestLayout();
            }

            syncActionBarToggle();
        }
    }

    //endregion ActionBarFragmentBase.OnActionBarChangedListener

    //region NavigationFragment.OnNavigationRequestListener

    @Override
    public void onNavigationRequest(NavigationFragment.NavigationType type, Point location) {
        LogUtils.LOGV(TAG, "onNavigationRequest type: " + type);

        int fragmentTag = getFragmentTag(type);
        Fragment fragment = getFragmentInstance(fragmentTag, location);

        showFragment(fragment, fragmentTag);
    }

    //endregion NavigationFragment.OnNavigationRequestListener

    //region ContentRevealFragmentBase.OnFragmentRemoveRequestListener

    @Override
    public void onFragmentHidden(String fragmentTag) {
        LogUtils.LOGV(TAG, "onFragmentHidden " + fragmentTag);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (fragment != null) {
            if (mDisplayedContentFragmentTag != null && mDisplayedContentFragmentTag.equals(fragmentTag)) {
                mDisplayedContentFragmentTag = null;
            }

            getSupportFragmentManager().beginTransaction()
                    .remove(fragment).commit();
            LogUtils.LOGV(TAG, "removed fragment" + fragmentTag);

            updateNavigationActionBar();
        }

    }

    @Override
    public void preFragmentHidden(String fragmentTag) {
        LogUtils.LOGV(TAG, "preFragmentHidden " + fragmentTag);

        if (mDisplayedContentFragmentTag != null && mDisplayedContentFragmentTag.equals(fragmentTag)) {
            mDisplayedContentFragmentTag = null;
        }

        updateNavigationActionBar();
    }

    @Override
    public void onFragmentShown(String fragmentTag) {
        removeContentFragments(fragmentTag);
    }

    private void updateNavigationActionBar() {
        Fragment navFragment = getSupportFragmentManager().findFragmentByTag(Utils.getString(getApplicationContext(), R.string.fragment_navigation));
        if (navFragment != null && navFragment instanceof ActionBarFragmentBase) {
            ((ActionBarFragmentBase) navFragment).updateActionBar();
        }

    }

    //endregion ContentRevealFragmentBase.OnFragmentRemoveRequestListener

    //region Back Clicks
    private boolean contentFragmentHandleBack() {
        boolean isHandled = false;
        if (mDisplayedContentFragmentTag != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(mDisplayedContentFragmentTag);
            if (fragment != null && fragment instanceof BackableInterface) {
                //let him to the back press!
                isHandled = ((BackableInterface) fragment).onBackPressed();
            }
        }

        return isHandled;
    }

    @Override
    public void onBackPressed() {
        LogUtils.LOGV(TAG, "onBackPressed ");
        try {
            if (mIsTabletMode || contentFragmentHandleBack() == false) {
                LogUtils.LOGV(TAG, "back click handled by mainActivity");
                super.onBackPressed();
            } else {
                LogUtils.LOGV(TAG, "back click handled by fragment");
            }
        }
        catch (Exception e){
            Crashlytics.getInstance().core.logException(e);
            super.onBackPressed();
        }

    }

    //endregion Back Clicks

    //region OpenIab

    private void initOpenIab() {
        OpenIabManager.getInstance(this).buildOpenIabHelper(getApplicationContext());
        OpenIabManager.getInstance(this).startSetupOpenIabHelper(getApplicationContext());
    }

    private void destroyOpenIab() {
        OpenIabManager.getInstance(this).disposeSelf();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.LOGD(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!OpenIabManager.getInstance(this).handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);

            try {
                //run checkup to sync purchases. fixes rotation on purchase buy
                OpenIabManager.getInstance(this).queryInventoryAsync();
            } catch (Exception e) {
                //some devices without Google Play don't like it
            }
        } else {
            LogUtils.LOGD(TAG, "onActivityResult handled by IABUtil.");

        }
    }


    //endregion OpenIab

    //region Walkthrough

    private boolean initWalkthrough() {
        //show tutorial if not finished before
        if (!PrefUtils.getWalkthroughIsFinishedValue(getApplicationContext())) {
            LogUtils.LOGD(TAG, "Walkthrough never shown, showing tutorial");
            showWalkthrough();
            return true;//showing tutorial
        }

        LogUtils.LOGD(TAG, "Walkthrough marked as shown, moving on.");
        return false;//not showing tutorial
    }

    private void showWalkthrough() {
        LogUtils.LOGD(TAG, "Showing Walkthrough");
        startActivity(new Intent(this, WalkthroughActivity.class));
    }

    //endregion Walkthrough

    //region AppRater

    private void initAppRater() {
        AppRater.app_launched(MainActivity.this,
                Utils.getInteger(getApplicationContext(), R.integer.apprater_days_until_prompt),
                Utils.getInteger(getApplicationContext(), R.integer.apprater_launches_until_prompt),
                Utils.getInteger(getApplicationContext(), R.integer.apprater_days_until_remind),
                Utils.getInteger(getApplicationContext(), R.integer.apprater_launches_until_remind));
    }

    //endregion AppRater


    private void showTranslateDialog() {
        LogUtils.LOGD(TAG, "Showing translate dialog");

        FragmentManager fm = getSupportFragmentManager();
        TranslateDialog translateDialog = new TranslateDialog();
        translateDialog.show(fm, Utils.getString(getApplicationContext(), R.string.dialog_translation));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LogUtils.LOGD(TAG, "onSaveInstanceState | keeping displayed content fragment" + mDisplayedContentFragmentTag);

        outState.putString(STATE_DISPLAYED_FRAGMENT, mDisplayedContentFragmentTag);

    }


    private void syncActionBarToggle() {
        mDrawerToggle.setDrawerIndicatorEnabled(mDisplayedContentFragmentTag == null);

        if (mDisplayedContentFragmentTag == null) {
            mDrawerToggle.syncState();
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

    }
}
