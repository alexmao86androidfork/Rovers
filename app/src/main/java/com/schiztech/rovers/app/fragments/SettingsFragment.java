package com.schiztech.rovers.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.fragments.base.ContentRevealFragmentBase;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.utils.ExtensionsUtils;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.PrefKeys;
import com.schiztech.rovers.app.utils.PrefUtils;
import com.schiztech.rovers.app.utils.Utils;

import java.security.spec.ECField;

public class SettingsFragment extends ContentRevealFragmentBase {
    public static final String TAG = LogUtils.makeLogTag("SettingsFragment");
    ;

    public static SettingsFragment newInstance(boolean isTabletMode, int startX, int startY) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putInt(ARGUMENT_START_X, startX);
        args.putInt(ARGUMENT_START_Y, startY);
        args.putBoolean(ARGUMENT_TABLET_MODE, isTabletMode);
        fragment.setArguments(args);

        return fragment;
    }

    public static SettingsFragment newInstance(boolean isTabletMode) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARGUMENT_TABLET_MODE, isTabletMode);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    protected void onContentRevealed() {
        setSystemColor(R.color.color_primary_dark_settings_fragment);
    }

    @Override
    protected int getPrimaryDarkColor() {
        return R.color.color_primary_dark_settings_fragment;
    }

    @Override
    protected int getPrimaryColor() {
        return R.color.color_primary_settings_fragment;
    }

    @Override
    protected int getFragmentTitle() {
        return R.string.settings_fragment_title;
    }

    @Override
    protected View getLayoutView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    protected void initViews(View rootView, Bundle savedInstanceState) {
        getChildFragmentManager().beginTransaction().replace(R.id.settings_prefsContainer,
                new MainPreferenceFragment()).commit();
    }

    @Override
    protected String getFragmentTag() {
        return Utils.getString(getContext(), R.string.fragment_settings);
    }


    //region Main Prefs

    public static class MainPreferenceFragment extends PreferenceFragment {
        public static final String TAG = LogUtils.makeLogTag("MainPreferenceFragment");
        ;

        public MainPreferenceFragment() {
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesName(Utils.getString(getActivity().getApplicationContext(), R.string.prefs_main));
            getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.main_prefs);

            SharedPreferences prefs = PrefUtils.getMainSharedPreferences(getActivity().getApplicationContext());
            if (prefs != null) {
                prefs.registerOnSharedPreferenceChangeListener(mPrefsListener);
            }
        }

        @Override
        public void onDestroy() {
            SharedPreferences prefs = PrefUtils.getMainSharedPreferences(getActivity().getApplicationContext());
            if (prefs != null) {
                prefs.unregisterOnSharedPreferenceChangeListener(mPrefsListener);
            }

            mPrefsListener = null;

            super.onDestroy();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            LinearLayout v = (LinearLayout) super.onCreateView(inflater, container, savedInstanceState);
            final ListView lv = (ListView) v.findViewById(android.R.id.list);
            if (lv != null) {
                LinearLayout header = (LinearLayout) inflater.inflate(R.layout.header_item_settings, null);

                lv.addHeaderView(header, null, false);


            }

            if (!ExtensionsUtils.isGotMoreSettings(getActivity().getApplicationContext()))
                initLockedContent();

            return v;
        }

        private void initLockedContent() {
            String[] lockedPrefs = new String[]{
                    PrefKeys.TRIGGER_BACKGROUND_COLOR,
                    PrefKeys.TRIGGER_ICON_COLOR,
                    PrefKeys.TRIGGER_REST_OFFSET,
                    PrefKeys.TRIGGER_REST_ALPHA,
                    PrefKeys.TRIGGER_INDEPENDENT_SIZE,
                    PrefKeys.TRIGGER_ITEM_SIZE
            };


            for (String key : lockedPrefs) {
                Preference p = findPreference(key);
                p.setEnabled(false);
                p.setSummary(ExtensionsUtils.getExtensionContentWarning(getActivity(), ExtensionsUtils.ExtensionType.MoreSettings));
            }
        }


        SharedPreferences.OnSharedPreferenceChangeListener mPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key == null) return;
                if (getActivity() == null) return;

                Context context = getActivity().getApplicationContext();

                //manage settings changed on some prefs:
                switch (key) {

                    case PrefKeys.MISC_SEND_ANONYMOUS_DATA://turn on/off analytics
                        boolean isAnalyticsOptIn = PrefUtils.getMiscSendAnonymousDataValue(context);
                        AnalyticsManager.getInstance(context).setOptOut(!isAnalyticsOptIn);
                        break;

                    case PrefKeys.TRIGGER_BACKGROUND_COLOR://notify new background color
                        context.sendBroadcast(new Intent(PrefUtils.ACTION_TRIGGER_BACKGROUND_COLOR_CHANGED));
                        break;

                    case PrefKeys.TRIGGER_ICON_COLOR://notify new icon color
                        context.sendBroadcast(new Intent(PrefUtils.ACTION_TRIGGER_ICON_COLOR_CHANGED));
                        break;

                    case PrefKeys.TRIGGER_REST_ALPHA://notify new alpha level
                        context.sendBroadcast(new Intent(PrefUtils.ACTION_TRIGGER_REST_ALPHA_CHANGED));
                        break;

                    case PrefKeys.TRIGGER_REST_OFFSET://notify new rest offset
                        context.sendBroadcast(new Intent(PrefUtils.ACTION_TRIGGER_REST_OFFSET_CHANGED));
                        break;

                    case PrefKeys.ITEMS_DEFAULT_APPLICATION_COLOR://notify new item default color
                        context.sendBroadcast(new Intent(PrefUtils.ACTION_ITEMS_DEFAULT_COLOR));
                        break;

                    case PrefKeys.ITEMS_DEFAULT_SHORTCUT_COLOR://notify new item default color
                        context.sendBroadcast(new Intent(PrefUtils.ACTION_ITEMS_DEFAULT_COLOR));
                        break;

                    case PrefKeys.ITEMS_DEFAULT_FOLDER_COLOR://notify new item default color
                        context.sendBroadcast(new Intent(PrefUtils.ACTION_ITEMS_DEFAULT_COLOR));
                        break;

                    case PrefKeys.ITEMS_ITEM_SIZE://restart rovers service
                        restartRoversService(context);
                        break;

                    case PrefKeys.TRIGGER_INDEPENDENT_SIZE://restart rovers service
                        restartRoversService(context);
                        break;
                    case PrefKeys.TRIGGER_ITEM_SIZE://restart rovers service
                        restartRoversService(context);
                        break;
                }
            }
        };

        final int DELAY_TIME = 750;
        Handler mHandler = null;

        private void restartRoversService(final Context context) {
            try {
                if (PrefUtils.getRoversIsActivatedValue(context)) {
                    Utils.syncRoverWindow(context, false);

                    if (mHandler == null) {
                        mHandler = new Handler();
                    } else {
                        mHandler.removeCallbacksAndMessages(null);
                    }
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (context != null) {
                                    Utils.syncRoverWindow(context, true);
                                }
                            } catch (Exception e) {
                                LogUtils.LOGE(TAG, "Error restarting handler: " + e.getMessage());
                            }
                        }
                    }, DELAY_TIME);

                }
            } catch (Exception e) {
                LogUtils.LOGE(TAG, "Error restarting Rovers: " + e.getMessage());
            }

        }

        @Override
        public void onDestroyView() {
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
                mHandler = null;
            }
            super.onDestroyView();
        }

    }

//endregion
}
