package com.schiztech.rovers.app.fragments.selectors;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.adapters.ActivityListAdapter;
import com.schiztech.rovers.app.fragments.base.FragmentBase;
import com.schiztech.rovers.app.roveritems.IRover;

/**
 * Created by schiz_000 on 9/7/2014.
 */
public abstract class SelectIntentFragment extends FragmentBase implements AdapterView.OnItemClickListener, ActivityListAdapter.OnItemsLoadedListener {

    protected ActivityListAdapter mAdapter;
    public interface OnIntentSelectedListener {
        void onIntentSelected(Intent selectedIntent, IRover.RoverType roverItemType);
    }
    private OnIntentSelectedListener mListener;
    private GridView mGridView;
    private boolean mIsLightTheme = false;


    protected void callOnIntentSelected(Intent intent, IRover.RoverType roverItemType){
        if(mListener != null)
            mListener.onIntentSelected(intent, roverItemType);
    }

    abstract protected Intent getIntentQuery();

    abstract protected int getQueryFlags();

    public void setIsLightTheme(boolean isLightTheme){
        mIsLightTheme = isLightTheme;
    }

    @Override
    public abstract void onItemClick(AdapterView<?> adapterView, View view, int position, long l);

    //region Fragment

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnIntentSelectedListener) activity;
    }

    @Override
    public void onDetach(){
        mListener = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_select_intent, container, false);
        mGridView = (GridView) v.findViewById(R.id.gridview);
        mGridView.setEmptyView(v.findViewById(R.id.progressBar));
        mAdapter = new ActivityListAdapter(getActivity(), getIntentQuery(), getQueryFlags(), mIsLightTheme, this);

        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);

        return v;
    }

    @Override
    public void onDestroyView(){
        mListener = null;
        mGridView.setOnItemClickListener(null);
        //mGridView.setAdapter(null);
        //mAdapter.clear();
        if(mAdapter != null) {
            mAdapter.setOnItemsLoadedListener(null);
            mAdapter = null;
        }
        mGridView = null;
        mListener = null;

        super.onDestroyView();
    }

    //endregion


}
