package com.schiztech.rovers.app.fragments.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.MenuRes;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.fragments.base.ActionBarFragmentBase;
import com.schiztech.rovers.app.ui.reveallayout.RevealLayout;
import com.schiztech.rovers.app.utils.BackableInterface;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.Utils;

/**
 * Created by schiz_000 on 1/2/2015.
 */
public abstract class ContentRevealFragmentBase extends ActionBarFragmentBase implements BackableInterface {

    public static final String TAG = LogUtils.makeLogTag("ContentRevealFragmentBase");;
    public static final String ARGUMENT_TABLET_MODE = "tablet_mode";
    public static final String ARGUMENT_START_X = "start_x";
    public static final String ARGUMENT_START_Y = "start_y";


    RevealLayout mRevealLayout;
    boolean mIsRevealing = false;
    int mStartX = Integer.MIN_VALUE;
    int mStartY = Integer.MIN_VALUE;
    protected Toolbar mToolbar;
    protected boolean mIsTabletMode = false;


    //region Fragment States
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null && getArguments() != null) {
            mStartX = getArguments().getInt(ARGUMENT_START_X, Integer.MIN_VALUE);
            mStartY = getArguments().getInt(ARGUMENT_START_Y, Integer.MIN_VALUE);
            mIsTabletMode = getArguments().getBoolean(ARGUMENT_TABLET_MODE, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = getLayoutView(inflater, container);
        mRevealLayout = (RevealLayout) rootView.findViewById(R.id.reveal_layout);
        mRevealLayout.setClickable(true);

        initActionBar(rootView);
        initViews(rootView, savedInstanceState);

        if (savedInstanceState == null) {
            //animate only on first initiation - not on configuration change
            mRevealLayout.setContentShown(false);
            startupReveal(new RevealLayout.OnRevealEndedListener() {
                @Override
                public void onRevealEnded() {
                    fragmentShown();
                    onContentRevealed();
                }
            });
        }

        else{
            setHasOptionsMenu(isHasOptionsMenu());
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        mToolbar.setOnMenuItemClickListener(null);
        mToolbar= null;
        mRevealLayout = null;

        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnFragmentStateChangedListener) {
            setOnFragmentRemoveRequestListener((OnFragmentStateChangedListener) activity);
        } else {
            LogUtils.LOGE(TAG, "Activity isn't implementing OnFragmentRemoveRequestListener!");
            throw new IllegalArgumentException("Activity isn't implementing OnFragmentRemoveRequestListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentStateChanged = null;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(getOptionsMenuResource(),menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    //endregion Fragment States

    protected void initActionBar(View rootView){
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if(mToolbar == null){
            throw new IllegalArgumentException("ContentRevealFragmentBase must have toolbar view!");
        }
        mToolbar.setBackgroundColor(Utils.getColor(getContext(), getPrimaryColor()));


        mToolbar.setTitle(getFragmentTitle());
        mToolbar.setTitleTextColor(Utils.getColor(getContext(), R.color.text_color_secondary));

        ViewCompat.setElevation(mToolbar, getResources().getDimensionPixelSize(R.dimen.toolbar_elevation));

        if(isHasOptionsMenu() && mIsTabletMode){
            mToolbar.inflateMenu(getOptionsMenuResource());
            mToolbar.setOnMenuItemClickListener(
                    new Toolbar.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            return menuItemClickHandler(item);
                        }
                    });

        }

        setActionBar(mToolbar);
    }

    public void updateActionBar() {
        setSystemColor(getPrimaryDarkColor());

        if(mToolbar != null) {
            setActionBar(mToolbar);
        }
    }

    //region Abstract Methods

    protected abstract void onContentRevealed();

    protected abstract @ColorRes int getPrimaryDarkColor();
    protected abstract @ColorRes int getPrimaryColor();
    protected abstract @StringRes int getFragmentTitle();

    protected abstract View getLayoutView(LayoutInflater inflater, ViewGroup container);

    protected abstract void initViews(View rootView, Bundle savedInstanceState);

    protected abstract String getFragmentTag();
    //endregion Abstract Methods

    //region Options Menu Methods


        protected boolean isHasOptionsMenu(){
        return false;
    }

    protected  @MenuRes int getOptionsMenuResource(){
        return 0;
    }

    protected boolean menuItemClickHandler(MenuItem item) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return menuItemClickHandler(item);
    }


    //endregion

    //region ContentReveal

    protected void startupReveal(final RevealLayout.OnRevealEndedListener endedListener){
        if(Utils.isAndroidVersionEqualOrAbove(Build.VERSION_CODES.JELLY_BEAN_MR2)) {
            mRevealLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressLint("NewApi")
                @Override
                public void onGlobalLayout() {
                    if (Utils.isAndroidVersionEqualOrAbove(Build.VERSION_CODES.JELLY_BEAN_MR1)) {
                        mRevealLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    } else {
                        mRevealLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    mRevealLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            revealContent(endedListener);
                        }
                    }, 100);
                }
            });
        }

        else{//hardware acceleration not available - don't make effects
            mRevealLayout.setContentShown(true);
            if(endedListener != null)
                endedListener.onRevealEnded();
        }
    }

    protected void revealContent(RevealLayout.OnRevealEndedListener endedListener) {
        if(Utils.isAndroidVersionEqualOrAbove(Build.VERSION_CODES.JELLY_BEAN_MR2)) {
            toggleContent(true,endedListener);
        }
        else{//hardware acceleration not available - don't make effects
            mRevealLayout.setContentShown(true);
            if(endedListener != null)
                endedListener.onRevealEnded();
        }

    }

    protected void unrevealContent(RevealLayout.OnRevealEndedListener endedListener) {
        if(Utils.isAndroidVersionEqualOrAbove(Build.VERSION_CODES.JELLY_BEAN_MR2)) {
            toggleContent(false,endedListener);
        }
        else{//hardware acceleration not available - don't make effects
            mRevealLayout.setContentShown(true);
            if(endedListener != null)
                endedListener.onRevealEnded();
        }

    }

    protected void toggleContent(final boolean isShow, final RevealLayout.OnRevealEndedListener endedListener) {
        if(mRevealLayout == null) return; //there's no view to see
        if (mIsRevealing) return;//don't interrupt while animating

        mIsRevealing = true;
        mRevealLayout.setOnRevealEndedListener(new RevealLayout.OnRevealEndedListener() {
            @Override
            public void onRevealEnded() {
                if(mRevealLayout != null) {
                    mRevealLayout.setOnRevealEndedListener(null);
                }
                mIsRevealing = false;
                if(endedListener != null) {
                    endedListener.onRevealEnded();
                }
            }
        });

        final int animTime = Utils.getInteger(getContext(), android.R.integer.config_mediumAnimTime);

        if (isShow) {
            if (mStartX == Integer.MIN_VALUE || mStartY == Integer.MIN_VALUE) {
                mRevealLayout.show(animTime);
            } else {
                mRevealLayout.show(mStartX, mStartY, animTime);
            }
        } else {
            if (mStartX == Integer.MIN_VALUE || mStartY == Integer.MIN_VALUE) {
                try {
                    mRevealLayout.hide(animTime);
                }catch (Exception e){}
            }
            else{
                try {
                    mRevealLayout.hide(mStartX, mStartY, animTime);
                }catch (Exception e){}
            }

        }

    }

    //endregion ContentReveal

    //region OnFragmentRemoveRequestListener

    public interface OnFragmentStateChangedListener {
        void onFragmentHidden(String fragmentTag);
        void preFragmentHidden(String fragmentTag);
        void onFragmentShown(String fragmentTag);
    }

    private OnFragmentStateChangedListener mFragmentStateChanged;

    protected void setOnFragmentRemoveRequestListener(OnFragmentStateChangedListener listener) {
        mFragmentStateChanged = listener;
    }

    protected void preFragmentHidden() {
        if (mFragmentStateChanged != null)
            mFragmentStateChanged.preFragmentHidden(getFragmentTag());
        setHasOptionsMenu(false);
    }
    protected void fragmentHidden() {
        if (mFragmentStateChanged != null)
            mFragmentStateChanged.onFragmentHidden(getFragmentTag());
    }
    protected void fragmentShown() {
        if (mFragmentStateChanged != null)
            mFragmentStateChanged.onFragmentShown(getFragmentTag());

        if(!mIsTabletMode) {
            setHasOptionsMenu(isHasOptionsMenu());
        }
    }

    //endregion OnFragmentRemoveRequestListener

    //region BackableInterface

    @Override
    public boolean onBackPressed() {
        if (!mIsRevealing) {
            preFragmentHidden();
            unrevealContent(new RevealLayout.OnRevealEndedListener() {
                @Override
                public void onRevealEnded() {
                    fragmentHidden();
                }
            });
        }
        return true;
    }

    //endregion BackableInterface


}
