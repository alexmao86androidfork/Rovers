package com.schiztech.rovers.app.fragments.base;

import android.support.v4.app.Fragment;

import com.schiztech.rovers.app.managers.AnalyticsManager;

/**
 * Created by schiz_000 on 4/8/2015.
 */
public abstract class SupportFragmentBase extends Fragment {
    @Override
    public void onResume() {
        super.onResume();

        AnalyticsManager.getInstance(getActivity().getApplicationContext()).reportScreen(getFragmentTag());
    }

    protected abstract String getFragmentTag();

}
