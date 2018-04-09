package com.schiztech.rovers.app.fragments.selectors;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.adapters.ColorAdapter;
import com.schiztech.rovers.app.fragments.base.FragmentBase;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.ui.LockedContentLayout;
import com.schiztech.rovers.app.utils.ExtensionsUtils;
import com.schiztech.rovers.app.utils.Utils;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

/**
 * Created by schiz_000 on 9/11/2014.
 */
public class SelectColorFragment extends FragmentBase implements AdapterView.OnItemClickListener {
    ColorAdapter mAdapter;
    GridView mGridView;
    View mRootView;
    LockedContentLayout mLockedContentLayout;

    @Override
    protected String getFragmentTag() {
        return Utils.getString(getActivity().getApplicationContext(), R.string.fragment_select_color);
    }


    //region Color Selected Listener

    public interface OnColorSelectedListener {
        void onColorSelected(int selectedColor);
    }
    private OnColorSelectedListener mListener;
    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        mListener = listener;
    }

    //endregion

    public SelectColorFragment() {
    }

    public static SelectColorFragment newInstance() {
        return new SelectColorFragment();
    }

    //region Fragment

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        mRootView = layoutInflater.inflate(R.layout.gridview_coloricon, null);

        mLockedContentLayout = (LockedContentLayout)mRootView.findViewById(R.id.addRover_lockedContentLayout);
        initLockedContentLayout();

        mGridView = (GridView) mRootView.findViewById(R.id.coloricon_grid);
        initGridView();

        return mRootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setOnColorSelectedListener((OnColorSelectedListener)activity);
    }

    @Override
    public void onDetach(){
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onDestroyView(){
        if(mLockedContentLayout !=null){
            mLockedContentLayout.setOnGetExtensiosnClickedListener(null);
            mLockedContentLayout = null;
        }
        if(mGridView != null){
            Utils.unbindDrawables(mGridView);
            mGridView.setOnItemClickListener(null);
            mGridView = null;
        }

        mRootView = null;

        mAdapter = null;

        super.onDestroyView();
    }

    //endregion

    //region GridView

    private void initGridView(){
        if(mGridView == null) return;

        int selectedColor = Integer.MIN_VALUE;
        Integer defaultColor = null;

        if(getArguments() != null && getArguments().containsKey("currentColor")){
            selectedColor = getArguments().getInt("currentColor",selectedColor);
        }
        if(getArguments() != null && getArguments().containsKey("defaultColor")){
            defaultColor = getArguments().getInt("defaultColor",Integer.MAX_VALUE);
            if(defaultColor == Integer.MAX_VALUE)
                defaultColor = null;
        }
        ((StickyGridHeadersGridView)mGridView).setAreHeadersSticky(false);
        mAdapter = new ColorAdapter(getActivity(), selectedColor,defaultColor);
        mGridView.setAdapter(mAdapter);

        mGridView.setOnItemClickListener(this);

    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Integer selectedColor = (Integer)mAdapter.getItem(i);

        if (mListener != null)
            mListener.onColorSelected(selectedColor);

        mAdapter.setSelectedColor(selectedColor);
//        mGridView.setEnabled(false);//prevent multiple selections

        AnalyticsManager.getInstance(getActivity().getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                AnalyticsManager.Action.Colors,
                selectedColor.toString());
    }


    //endregion GridView

    //region Locked Content

    private void initLockedContentLayout(){
        if(mLockedContentLayout == null) return;

        //check if "more colors" purchased to determine if showing the warning.
        if(ExtensionsUtils.isGotMoreColors(getActivity().getApplicationContext())){
           mLockedContentLayout.hide();
        }

        else {
            mLockedContentLayout.setExtensionType(ExtensionsUtils.ExtensionType.MoreColors);
            mLockedContentLayout.setOnGetExtensiosnClickedListener(new LockedContentLayout.OnGetExtensionsClickedListener() {
                @Override
                public void onGetExtensionsClicked() {
                    if(getActivity() != null){
                        getActivity().finish();//close this activity
                    }
                }
            });
            mLockedContentLayout.show();
        }
    }

    //endregion Locked Content


}


