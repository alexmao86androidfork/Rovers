package com.schiztech.rovers.app.fragments.selectors;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.fragments.base.FragmentBase;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.roveritems.IRover;
import com.schiztech.rovers.app.ui.CircleButton;
import com.schiztech.rovers.app.utils.BitmapUtils;
import com.schiztech.rovers.app.utils.RoversUtils;
import com.schiztech.rovers.app.utils.Utils;

/**
 * Created by schiz_000 on 9/8/2014.
 */
public class SelectDesignFragment extends FragmentBase{

    IRover mRover;
    View mLayout;
    Button mColorButton;
    Button mIconButton;
    Button mDefaultButton;
    CircleButton mRoverCircle;

    public static SelectDesignFragment newInstance() {
        return new SelectDesignFragment();
    }

    //region Rover
    public void setRover(IRover rover){
        mRover = rover;
    }

    public IRover getRover(){
        return mRover;
    }

    public void setColor(int color) {
        if(mRover == null) return;
        mRover.setColor(color);

        UpdateRoverView();
    }

    public void setIcon(RoversUtils.RoverIcon icon){
        if(mRover == null) return;
        mRover.setIcon(icon);

        UpdateRoverView();
    }

    private void UpdateRoverView(){
        if(mRoverCircle == null || mRover == null)
            return;

        if(mRover.isRoverIcon(getActivity().getApplicationContext()))
            mRoverCircle.setImageDrawable(mRover.getIcon(getActivity().getApplicationContext()));
        else {
            Drawable icon = mRover.getIcon(getActivity().getApplicationContext());
            Bitmap bitmap = BitmapUtils.drawableToBitmap(icon);
            mRoverCircle.setImageBitmap(BitmapUtils.getCroppedBitmap(bitmap, mRoverCircle.getInnerWidth()));

            if(!(icon instanceof BitmapDrawable)){//check if icon is actually == the bitmap, if so crash might occur when adding app and launching the host.
                bitmap.recycle();
            }
        }
        mRoverCircle.setCircleColor(mRover.getColor(getActivity().getApplicationContext()));

        mRoverCircle.setOnClickListener(mRoverCircleClick);
    }

    //endregion Rover

    //region Fragment

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.fragment_select_design, container, false);

        mColorButton = (Button) mLayout.findViewById(R.id.design_customizeColorBtn);
        mColorButton.setOnClickListener(mColorButtonClick);

        mIconButton = (Button)mLayout.findViewById(R.id.design_customizeIconBtn);
        mIconButton.setOnClickListener(mIconButtonClick);

        mDefaultButton = (Button)mLayout.findViewById(R.id.design_defaultIconBtn);
        mDefaultButton.setOnClickListener(mDefaultButtonClick);

        mRoverCircle = (CircleButton) mLayout.findViewById(R.id.rover_icon);
        mRoverCircle.setOnClickListener(mRoverCircleClick);

        UpdateRoverView();

        return mLayout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mIconRequestListener = (OnChangeIconRequestListener)activity;
        mColorRequestListener = (OnChangeColorRequestListener)activity;
        mBackToDefaultListener = (OnBackToDefaultListener)activity;
    }

    @Override
    public void onDetach() {
        mColorRequestListener = null;
        mIconRequestListener = null;
        mBackToDefaultListener = null;
        super.onDetach();
    }

    @Override
    public void onDestroyView(){
        if(mRoverCircle != null){
            mRoverCircle.setOnClickListener(null);
            mRoverCircleClick = null;

            mColorButton.setOnClickListener(null);
            mColorButtonClick = null;

            mIconButton.setOnClickListener(null);
            mIconButtonClick = null;

            mDefaultButton.setOnClickListener(null);
            mDefaultButton = null;
        }

        if(mLayout != null){
            Utils.unbindDrawables(mLayout);
        }

        mLayout = null;
        mRover = null;

        mColorRequestListener = null;
        mIconRequestListener = null;
        mBackToDefaultListener = null;
        super.onDestroyView();
    }

    public void onSecondaryFragmentDismissed(){
        if(mColorButton != null) {
            mColorButton.setTextColor(getResources().getColor(R.color.change_iconcolor_text));
            mColorButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_design_changecolor, 0, 0, 0);
        }
        if(mIconButton != null) {
            mIconButton.setTextColor(getResources().getColor(R.color.change_iconcolor_text));
            mIconButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_design_changeicon, 0, 0, 0);
        }
    }

    //endregion

    //region OnClick Button

    View.OnClickListener mRoverCircleClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AnalyticsManager.getInstance(getActivity().getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                    AnalyticsManager.Action.Button_Click,
                    "Design_RoverCircle");

            if(mRover != null)
                Toast.makeText(getActivity().getApplicationContext(), mRover.getLabel(getActivity().getApplicationContext()), Toast.LENGTH_SHORT).show();
        }
    };

    View.OnClickListener mColorButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AnalyticsManager.getInstance(getActivity().getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                    AnalyticsManager.Action.Button_Click,
                    "Design_ChangeColor", mRover.isColorChangeable() ? 1L:0L);

            if(mRover.isColorChangeable()) {
                mColorButton.setTextColor(getResources().getColor(R.color.change_iconcolor_text_pressed));
                mColorButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_design_changecolor_blue, 0, 0, 0);
                mIconButton.setTextColor(getResources().getColor(R.color.change_iconcolor_text));
                mIconButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_design_changeicon, 0, 0, 0);

                int currentColor = mRover.isDefaultColor() ? IRover.ROVER_DEFAULT_COLOR_VALUE : mRover.getColor(getActivity().getApplicationContext());
                int defaultColor = mRover.getDefaultColor(getActivity().getApplicationContext());
                mColorRequestListener.OnChangeColorRequest(currentColor, defaultColor);
            }
            else{
                Toast.makeText(getActivity().getApplicationContext(), R.string.design_error_change_color,Toast.LENGTH_SHORT).show();
            }
        }
    };
    View.OnClickListener mIconButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AnalyticsManager.getInstance(getActivity().getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                    AnalyticsManager.Action.Button_Click,
                    "Design_ChangeIcon", mRover.isIconChangeable() ? 1L:0L);

            if(mRover.isIconChangeable()) {
                mColorButton.setTextColor(getResources().getColor(R.color.change_iconcolor_text));
                mColorButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_design_changecolor,0,0,0);
                mIconButton.setTextColor(getResources().getColor(R.color.change_iconcolor_text_pressed));
                mIconButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_design_changeicon_blue,0,0,0);
                mIconRequestListener.OnChangeIconRequest();
            }
            else{
                Toast.makeText(getActivity().getApplicationContext(), R.string.design_error_change_icon,Toast.LENGTH_SHORT).show();
            }
        }
    };


    View.OnClickListener mDefaultButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AnalyticsManager.getInstance(getActivity().getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                    AnalyticsManager.Action.Button_Click,
                    "Design_DefaultIcon", mRover.isIconChangeable() ? 1L:0L);

            setColor(IRover.ROVER_DEFAULT_COLOR_VALUE);
            setIcon(IRover.ROVER_DEFAULT_ICON_VALUE);

            mBackToDefaultListener.OnBackToDefault();
        }
    };

    @Override
    protected String getFragmentTag() {
        return Utils.getString(getActivity().getApplicationContext(), R.string.fragment_select_design);

    }


    //endregion OnClick Button

    //region Listeners

    public interface OnChangeColorRequestListener {
        void OnChangeColorRequest(int currentColor, int defaultColor);
    }
    private OnChangeColorRequestListener mColorRequestListener;

    public interface OnChangeIconRequestListener {
        void OnChangeIconRequest();
    }

    private OnChangeIconRequestListener mIconRequestListener;

    public interface OnBackToDefaultListener {
        void OnBackToDefault();
    }
    private OnBackToDefaultListener mBackToDefaultListener;



    //endregion

}
