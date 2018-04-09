package com.schiztech.rovers.app.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.adapters.CreditsRecyclerAdapter;
import com.schiztech.rovers.app.adapters.SimpleSectionedRecyclerViewAdapter;
import com.schiztech.rovers.app.fragments.base.ContentRevealFragmentBase;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schiz_000 on 4/6/2015.
 */
public class AboutFragment extends ContentRevealFragmentBase{
    public static final String TAG = LogUtils.makeLogTag("AboutFragment");
    View mRootView;
    CreditsRecyclerAdapter mAdapter;
    SimpleSectionedRecyclerViewAdapter mSectionedAdapter;



    //region create Instance

    public static AboutFragment newInstance(boolean isTabletMode, int startX, int startY) {
        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        args.putInt(ARGUMENT_START_X, startX);
        args.putInt(ARGUMENT_START_Y, startY);
        args.putBoolean(ARGUMENT_TABLET_MODE, isTabletMode);
        fragment.setArguments(args);

        return fragment;
    }

    public static AboutFragment newInstance(boolean isTabletMode) {
        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARGUMENT_TABLET_MODE, isTabletMode);
        fragment.setArguments(args);

        return fragment;
    }

    //endregion create Instance

    //region Base Methods

    @Override
    protected void onContentRevealed() {
        setSystemColor(R.color.color_primary_dark_about_fragment);

    }

    @Override
    protected int getPrimaryDarkColor() {
        return R.color.color_primary_dark_about_fragment;
    }

    @Override
    protected int getPrimaryColor() {
        return R.color.color_primary_about_fragment;
    }

    @Override
    protected int getFragmentTitle() {
        return R.string.about_fragment_title;
    }

    @Override
    protected View getLayoutView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    protected String getFragmentTag() {
        return Utils.getString(getContext(), R.string.fragment_about);
    }

    //endregion Base Methods

    //region Init Methods

    @Override
    protected void initViews(View rootView, Bundle savedInstanceState) {
        mRootView = rootView;

        initRecyclerView();
    }

    private void initRecyclerView(){
        RecyclerView recList = (RecyclerView) mRootView.findViewById(R.id.credits_list);

        recList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(layoutManager);

        mAdapter = new CreditsRecyclerAdapter(getActivity(), new CreditsRecyclerAdapter.OnCreditsLoadedListener() {
            @Override
            public void OnCreditsLoaded(int libraries, int icons) {
                if(mAdapter == null || mSectionedAdapter == null) return;

                mAdapter.clearOnCreditsLoadedListener();
                List<SimpleSectionedRecyclerViewAdapter.Section> sections = new ArrayList<>();

                //Sections
                sections.add(new SimpleSectionedRecyclerViewAdapter.Section(1,Utils.getString(getContext(), R.string.about_libraries)));
                if(icons >0) {
                    sections.add(new SimpleSectionedRecyclerViewAdapter.Section(libraries, Utils.getString(getContext(), R.string.about_icon_packs)));
                }
                SimpleSectionedRecyclerViewAdapter.Section[] dummy = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
                mSectionedAdapter.setSections(sections.toArray(dummy));

            }
        });


        mSectionedAdapter = new
                SimpleSectionedRecyclerViewAdapter(getContext(), R.layout.section_about,R.id.section_text, mAdapter);


        recList.setAdapter(mSectionedAdapter);


    }


    //endregion init methods

    //region Destroy Methods

    @Override
    public void onDestroyView() {
        if(mAdapter != null){
            mAdapter.clear();
            mAdapter = null;
        }
        mRootView = null;
        mSectionedAdapter = null;
        super.onDestroyView();

    }


    //endregion Destroy Methods
}
