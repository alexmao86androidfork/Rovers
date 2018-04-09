package com.schiztech.rovers.app.fragments.selectors;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.adapters.RoverTypeAdapter;
import com.schiztech.rovers.app.fragments.base.FragmentBase;
import com.schiztech.rovers.app.utils.Utils;

/**
 * Created by schiz_000 on 9/5/2014.
 */
public class SelectTypeFragment extends FragmentBase {

    @Override
    protected String getFragmentTag() {
        return Utils.getString(getActivity().getApplicationContext(), R.string.fragment_select_rovertype);

    }

    public interface OnTypeSelectedListener {
        void onTypeSelected(Utils.RoverType type);
    }
    private OnTypeSelectedListener mListener;
    private GridView mGridView;
    private RoverTypeAdapter mAdapter;

    public static SelectTypeFragment newInstance() {
        return new SelectTypeFragment();
    }

    //region Fragment

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnTypeSelectedListener) activity;
    }

    @Override
    public void onDetach(){
        mListener = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_select_type, container, false);
        mGridView = (GridView) v.findViewById(R.id.gridview);
        mAdapter = new RoverTypeAdapter(getActivity());
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(mGridItemClick);
        return v;
    }


    @Override
    public void onDestroyView(){
        mListener = null;
        mGridView.setOnItemClickListener(null);
        //mGridView.setAdapter(null);
        mAdapter = null;
        mGridView = null;
        mGridItemClick = null;

        super.onDestroyView();
    }

    //endregion Fragment


    AdapterView.OnItemClickListener mGridItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            if(mListener!=null){
                mListener.onTypeSelected(Utils.RoverType.values()[position]);
            }
        }
    };


}