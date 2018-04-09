package com.schiztech.rovers.app.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.adapters.RoverActionRecyclerAdapter;
import com.schiztech.rovers.app.adapters.SimpleSectionedRecyclerViewAdapter;
import com.schiztech.rovers.app.dialogs.ActionsApiDialog;
import com.schiztech.rovers.app.fragments.base.ContentRevealFragmentBase;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.MarketUtils;
import com.schiztech.rovers.app.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.services.common.Crash;

public class ActionsFragment extends ContentRevealFragmentBase {
    View mRootView;
    View mMoreActionsGooglePlay;
    RoverActionRecyclerAdapter mAdapter;
    SimpleSectionedRecyclerViewAdapter mSectionedAdapter;
    private final String PLAY_SEARCH_TERM = "Rovers Action";


    //region Create Instance
    public static ActionsFragment newInstance(boolean isTabletMode, int startX, int startY) {
        ActionsFragment fragment = new ActionsFragment();
        Bundle args = new Bundle();
        args.putInt(ARGUMENT_START_X, startX);
        args.putInt(ARGUMENT_START_Y, startY);
        args.putBoolean(ARGUMENT_TABLET_MODE, isTabletMode);
        fragment.setArguments(args);

        return fragment;
    }

    public static ActionsFragment newInstance(boolean isTabletMode) {
        ActionsFragment fragment = new ActionsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARGUMENT_TABLET_MODE, isTabletMode);
        fragment.setArguments(args);

        return fragment;
    }

    //endregion Create Instance

    //region Base Methods


    @Override
    public void onDestroyView() {
        mRootView= null;
        mMoreActionsGooglePlay.setOnClickListener(null);
        mMoreActionsGooglePlay = null;
        mAdapter = null;
        mSectionedAdapter = null;
        super.onDestroyView();
    }


    @Override
    protected void onContentRevealed() {
        setSystemColor(R.color.color_primary_dark_actions_fragment);
    }

    @Override
    protected int getPrimaryDarkColor() {
        return R.color.color_primary_dark_actions_fragment;
    }

    @Override
    protected int getPrimaryColor() {
        return R.color.color_primary_actions_fragment;
    }

    @Override
    protected int getFragmentTitle() {
        return R.string.actions_fragment_title;
    }

    @Override
    protected View getLayoutView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_actions, container, false);
    }

    @Override
    protected String getFragmentTag() {
        return Utils.getString(getContext(), R.string.fragment_actions);
    }

    //endregion Base Methods

    //region Init Methods
    @Override
    protected void initViews(View rootView, Bundle savedInstanceState) {
        mRootView = rootView;
        View moreActions = mRootView.findViewById(R.id.actions_getMoreLayout);
        ViewCompat.setElevation(moreActions, getResources().getDimensionPixelSize(R.dimen.more_actions_elevation));

        mMoreActionsGooglePlay = mRootView.findViewById(R.id.actions_getMore_googlePlayBtn);
        mMoreActionsGooglePlay.setOnClickListener(mGooglePlayClickListener);

        initRecyclerView();
    }

    private void initRecyclerView(){
        RecyclerView recList = (RecyclerView) mRootView.findViewById(R.id.actions_list);

        recList.setHasFixedSize(true);
        LinearLayoutManager  layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(layoutManager);

        mAdapter = new RoverActionRecyclerAdapter(getContext(), new RoverActionRecyclerAdapter.OnActionsLoadedListener() {
            @Override
            public void OnActionsLoaded(int installedActions, int promotedActions) {
                if(mAdapter != null) {
                    mAdapter.clearOnActionsLoadedListener();
                }

                try {
                    List<SimpleSectionedRecyclerViewAdapter.Section> sections = new ArrayList<>();

                    //Sections
                    if (promotedActions > 0) {
                        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(1, Utils.getString(getContext(), R.string.actions_suggested)));
                    }

                    sections.add(new SimpleSectionedRecyclerViewAdapter.Section(promotedActions + 1, Utils.getString(getContext(), R.string.actions_installed)));

                    SimpleSectionedRecyclerViewAdapter.Section[] dummy = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
                    mSectionedAdapter.setSections(sections.toArray(dummy));
                }
                catch (Exception e){
                    LogUtils.LOGE(TAG, "Failed on got actions: " + e.getMessage());
                }
            }
        });


        mSectionedAdapter = new
                SimpleSectionedRecyclerViewAdapter(getContext(), R.layout.section_action,R.id.section_text, mAdapter);


        recList.setAdapter(mSectionedAdapter);


    }

    View.OnClickListener mGooglePlayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AnalyticsManager.getInstance(getContext()).reportEvent(AnalyticsManager.Category.UX,
                    AnalyticsManager.Action.Button_Click,
                    "Actions_GetMore");

            Utils.browseLink(getContext(),MarketUtils.getSearchPlayStoreLink(PLAY_SEARCH_TERM,false));
        }
    };


    //endregion Init Methods

    //region Options Menu
    @Override
    protected boolean isHasOptionsMenu() {
        return true;
    }

    @Override
    protected int getOptionsMenuResource() {
        return R.menu.menu_actions;
    }

    @Override
    protected boolean menuItemClickHandler(MenuItem item){
        if(item.getItemId() == R.id.action_api){
            AnalyticsManager.getInstance(getActivity().getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                    AnalyticsManager.Action.MenuItem_Click,
                    "Actions_Api");
            showActionsApiDialog();
        }

        return true;
    }

    //endregion Options Menu

    private void showActionsApiDialog() {
        FragmentManager fm =  getActivity().getSupportFragmentManager();
        ActionsApiDialog apiDialog = new ActionsApiDialog();
        apiDialog.show(fm, Utils.getString(getContext(), R.string.dialog_actionsapi));
    }

}
