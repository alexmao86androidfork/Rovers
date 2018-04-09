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
import com.schiztech.rovers.app.adapters.RoverIconAdapter;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.utils.RoversUtils;
import com.schiztech.rovers.app.utils.Utils;

/**
 * Created by schiz_000 on 9/19/2014.
 */
public class SelectIconFragment extends Fragment implements AdapterView.OnItemClickListener{
    RoverIconAdapter mAdapter;
    GridView mGridView;

    public static SelectIconFragment newInstance() {
        return new SelectIconFragment();
    }

    //region Icon Selected Listener

    public interface OnIconSelectedListener {
        void onIconSelected(RoversUtils.RoverIcon selectedIcon);
    }
    private OnIconSelectedListener mListener;
    public void setOnIconSelectedListener(OnIconSelectedListener listener) {
        mListener = listener;
    }

    //endregion

    //region Fragment

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View rootView = layoutInflater.inflate(R.layout.gridview_coloricon, null);

        mGridView = (GridView) rootView.findViewById(R.id.coloricon_grid);
        mAdapter = new RoverIconAdapter(getActivity());
        mGridView.setAdapter(mAdapter);

        mGridView.setOnItemClickListener(this);


        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setOnIconSelectedListener((OnIconSelectedListener) activity);
    }

    @Override
    public void onDetach(){
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onDestroyView(){
        if(mGridView != null){
            Utils.unbindDrawables(mGridView);
            mGridView.setOnItemClickListener(null);
            mGridView = null;
        }
        mAdapter = null;


        super.onDestroyView();
    }

    //endregion

    //region GridView

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mListener != null)
            mListener.onIconSelected((RoversUtils.RoverIcon) mAdapter.getItem(i));

//        mGridView.setEnabled(false);//prevent multiple selections

        AnalyticsManager.getInstance(getActivity().getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                AnalyticsManager.Action.Icons,
                mAdapter.getItem(i).toString());
    }




    //endregion GridView
}
