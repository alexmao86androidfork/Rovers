package com.schiztech.rovers.app.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.fragments.base.ActionBarFragmentBase;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.ui.ArcTextView;
import com.schiztech.rovers.app.ui.CircleButton;
import com.schiztech.rovers.app.utils.BitmapUtils;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.PrefUtils;
import com.schiztech.rovers.app.utils.Utils;

public class NavigationFragment extends ActionBarFragmentBase {
    private static final String TAG = LogUtils.makeLogTag("NavigationFragment");
    boolean mIsTabletMode = false;
    CircleButton mRoverStateCircleButton;
    CircleButton mRoverMenuSettingsCircleButton;
    CircleButton mRoverMenuActionsCircleButton;
    CircleButton mRoverIndicatorCircleButton;
    View mRoverMenuExtensionsButton;
    ArcTextView mRoverIndicatorTextView;

    Toolbar mToolbar;
    View mRootView;
    OnNavigationRequestListener mNavigationListener;

    Handler mHandler;


    public static NavigationFragment newInstance() {
        NavigationFragment fragment = new NavigationFragment();
        return fragment;
    }

    //region Fragment Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_navigation, container, false);
        mIsTabletMode = mRootView.findViewById(R.id.navigation_tablet_layout) != null;
        initViews();

        initRoverState(savedInstanceState == null);

        if (savedInstanceState == null) {
            animateViews();
        }

        return mRootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnNavigationRequestListener) {
            setOnNavigationRequestListener((OnNavigationRequestListener) activity);
        } else {
            LogUtils.LOGE(TAG, "Activity isn't implementing OnNavigationRequestListener!");
            throw new IllegalArgumentException("Activity isn't implementing OnNavigationRequestListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mNavigationListener = null;
    }

    @Override
    public void onDestroyView() {

        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        mOnRoverStateClickListener = null;

        mRoverStateCircleButton.setOnClickListener(null);
        mRoverStateCircleButton.setImageResource(0);

        mRoverMenuSettingsCircleButton.setOnClickListener(null);
        mRoverMenuSettingsCircleButton.setImageResource(0);

        mRoverMenuActionsCircleButton.setOnClickListener(null);
        mRoverMenuActionsCircleButton.setImageResource(0);

        mRoverIndicatorCircleButton.setOnClickListener(null);
        mRoverIndicatorCircleButton.setImageResource(0);

        mRoverMenuExtensionsButton.setOnClickListener(null);

        mToolbar = null;
        mRoverStateCircleButton = null;
        mRootView = null;

        mRoverIndicatorTextView = null;

        Utils.unbindDrawables(mRootView);
        System.gc();
        super.onDestroyView();
    }


    @Override
    protected String getFragmentTag() {
        return Utils.getString(getContext(), R.string.fragment_navigation);
    }

    //endregion Fragment Methods

    private void initViews() {
        //set toolbar as actionbar
        mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);

        mToolbar.setBackgroundColor(Utils.getColor(getContext(), R.color.color_primary_navigation_fragment));

        mToolbar.setTitleTextColor(Utils.getColor(getContext(), R.color.text_color_secondary));


        ViewCompat.setElevation(mToolbar, getResources().getDimensionPixelSize(R.dimen.toolbar_elevation));
        updateActionBar();

        mRoverIndicatorTextView = (ArcTextView) mRootView.findViewById(R.id.rover_textview_indicator);

        mRoverIndicatorCircleButton = (CircleButton) mRootView.findViewById(R.id.rover_circlebutton_indicator);

        mRoverStateCircleButton = (CircleButton) mRootView.findViewById(R.id.rover_circlebutton_trigger);
        mRoverStateCircleButton.setOnClickListener(mOnRoverStateClickListener);

        mRoverMenuSettingsCircleButton = (CircleButton) mRootView.findViewById(R.id.roverMenu_settingsBtn);
        mRoverMenuSettingsCircleButton.setOnClickListener(mOnRoverMenuSettingsClickListener);

        mRoverMenuActionsCircleButton = (CircleButton) mRootView.findViewById(R.id.roverMenu_actionsBtn);
        mRoverMenuActionsCircleButton.setOnClickListener(mOnRoverMenuActionsClickListener);

        mRoverMenuExtensionsButton = mRootView.findViewById(R.id.navigation_extensionsBtn);
        mRoverMenuExtensionsButton.setOnClickListener(mOnRoverMenuExtensionsClickListener);


        mRootView.findViewById(R.id.rover_circlebutton_indicator).setEnabled(false);
    }

    //region Rovers Settings
    View.OnClickListener mOnRoverMenuSettingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AnalyticsManager.getInstance(getContext()).reportEvent(AnalyticsManager.Category.UX,
                    AnalyticsManager.Action.Button_Click,
                    "Navigation_Settings");
            setNavigationRequest(NavigationType.Settings, getLocation(mRoverMenuSettingsCircleButton));
        }
    };
    //endregion Rovers Settings

    //region Rovers Actions
    View.OnClickListener mOnRoverMenuActionsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AnalyticsManager.getInstance(getContext()).reportEvent(AnalyticsManager.Category.UX,
                    AnalyticsManager.Action.Button_Click,
                    "Navigation_Actions");
            setNavigationRequest(NavigationType.Actions, getLocation(mRoverMenuActionsCircleButton));
        }
    };
    //endregion Rovers Actions

    //region Rovers Extensions
    View.OnClickListener mOnRoverMenuExtensionsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AnalyticsManager.getInstance(getContext()).reportEvent(AnalyticsManager.Category.UX,
                    AnalyticsManager.Action.Button_Click,
                    "NavigateExtensions");
            setNavigationRequest(NavigationType.Extensions, getLocation(mRoverMenuExtensionsButton));
        }
    };
    //endregion Rovers Extensions

    //region Rover State Methods

    private void initRoverState(boolean isToLaunch) {
        boolean isActivated = PrefUtils.getRoversIsActivatedValue(getContext());

        //check if tutorial was finished yet, if no - no need to start Rovers
        if (PrefUtils.getWalkthroughIsFinishedValue(getContext())) {
            if (isToLaunch) {
                syncRoversServiceDelayed(isActivated);
            }
        }

        syncIndicatorViews(isActivated);
    }


    View.OnClickListener mOnRoverStateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean isActivated = PrefUtils.getRoversIsActivatedValue(getContext());

            PrefUtils.setRoversIsActivatedValue(getContext(), !isActivated);

            tempDisableRoverStateChange();

            syncRoversService(!isActivated);
            syncIndicatorViews(!isActivated);

            AnalyticsManager.getInstance(getContext()).reportEvent(AnalyticsManager.Category.UX,
                                                                    AnalyticsManager.Action.Button_Click,
                                                                    "Navigation_RoverSwitch",
                                                                    isActivated ? 1L : 0L);
        }
    };


    private static final int SYNC_ROVERS_DELAY = 1000;

    private void syncRoversServiceDelayed(final boolean isOn) {
        if(mHandler == null){
            mHandler = new Handler();
            if(mRoverStateCircleButton != null) {
                mRoverStateCircleButton.setClickable(false);
            }
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mRoverStateCircleButton != null) {
                    mRoverStateCircleButton.setClickable(true);
                }
                syncRoversService(isOn);
            }
        }, SYNC_ROVERS_DELAY);
    }

    private void syncRoversService(final boolean isOn) {
        if (!isOn) {
            Utils.syncRoverWindow(getActivity(), false);

        } else {
            Utils.syncRoverWindow(getActivity(), true);
        }

    }

    private void syncIndicatorViews(boolean isOn) {
        mRoverIndicatorTextView.setTextColor(Utils.getColor(getContext(), isOn ? R.color.navigation_fragment_indicator_on : R.color.navigation_fragment_indicator_off));
        int indicatorMessageRes = isOn ? R.string.navigation_activated : R.string.navigation_disabled;
        String indicatorMessage = Utils.getString(getContext(), indicatorMessageRes).toUpperCase();
        mRoverIndicatorTextView.setText(indicatorMessage);

        int logoRes = isOn ? R.drawable.ic_logo_big : R.drawable.ic_logo_big_off;
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), logoRes);
        mRoverIndicatorCircleButton.setImageBitmap(BitmapUtils.getCroppedBitmap(bitmap, mRoverIndicatorCircleButton.getInnerWidth()));
        bitmap.recycle();

    }

    private static final int DISABLE_STATE_CHANGE_DELAY = 1000;

    private void tempDisableRoverStateChange() {
        mRoverStateCircleButton.setPressed(false);
        mRoverStateCircleButton.setClickable(false);
        if(mHandler == null){
            mHandler = new Handler();
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mRoverStateCircleButton != null) {
                    mRoverStateCircleButton.setPressed(true);
                    mRoverStateCircleButton.setClickable(true);
                }
            }
        }, DISABLE_STATE_CHANGE_DELAY);

    }

    //endregion Rover State Methods

    //region Animations

    private void animateViews() {
        if (!mIsTabletMode) {
            AnimatorSet settingsCircleAnimator = getCircleAnimator(mRoverMenuSettingsCircleButton);
            AnimatorSet actionsCircleAnimator = getCircleAnimator(mRoverMenuActionsCircleButton);
            AnimatorSet settingsLabelAnimator = getLabelAnimator(mRootView.findViewById(R.id.roverMenu_settingsLabel), false);
            AnimatorSet actionsLabelAnimator = getLabelAnimator(mRootView.findViewById(R.id.roverMenu_actionsLabel), true);
            AnimatorSet settingsBubbleAnimator = getBubbleAnimator(mRootView.findViewById(R.id.roverMenu_settingsBubbleBig),
                    mRootView.findViewById(R.id.roverMenu_settingsBubbleSmall));
            AnimatorSet actionsBubbleAnimator = getBubbleAnimator(mRootView.findViewById(R.id.roverMenu_actionsBubbleBig),
                    mRootView.findViewById(R.id.roverMenu_actionsBubbleSmall));

            final AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(actionsCircleAnimator).after(settingsCircleAnimator).
                    before(actionsLabelAnimator).before(settingsLabelAnimator).before(settingsBubbleAnimator).before(actionsBubbleAnimator);
            animatorSet.setStartDelay(Utils.getInteger(getContext(), android.R.integer.config_mediumAnimTime));

            animatorSet.start();
        }
    }

    private AnimatorSet getCircleAnimator(View circleView) {
        circleView.setScaleX(0.33f);
        circleView.setScaleY(0.33f);
        circleView.setAlpha(0f);
        ObjectAnimator circleAnimator = ObjectAnimator.ofPropertyValuesHolder(circleView,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1));
        circleAnimator.setInterpolator(new OvershootInterpolator());

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(circleView, View.ALPHA, 1);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(circleAnimator, alphaAnimator);
        animatorSet.setDuration(Utils.getInteger(getContext(), android.R.integer.config_shortAnimTime));


        return animatorSet;
    }

    private AnimatorSet getLabelAnimator(View labelView, boolean isToLeft) {
        int gap = 100 * (isToLeft ? 1 : -1);
        labelView.setTranslationX(gap);
        labelView.setAlpha(0f);
        ObjectAnimator labelAnimator = ObjectAnimator.ofFloat(labelView, View.TRANSLATION_X, 0);
        labelAnimator.setInterpolator(new OvershootInterpolator());

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(labelView, View.ALPHA, 1);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(labelAnimator, alphaAnimator);
        animatorSet.setDuration(Utils.getInteger(getContext(), android.R.integer.config_mediumAnimTime));


        return animatorSet;
    }

    private AnimatorSet getBubbleAnimator(View bigBubble, View smallBubble) {
        bigBubble.setScaleX(0.33f);
        bigBubble.setScaleY(0.33f);
        bigBubble.setAlpha(0f);
        smallBubble.setScaleX(0.33f);
        smallBubble.setScaleY(0.33f);
        smallBubble.setAlpha(0f);

        AnimatorSet bigBubbleAnimator = getCircleAnimator(bigBubble);
        AnimatorSet smallBubbleAnimator = getCircleAnimator(smallBubble);
        smallBubbleAnimator.setStartDelay(bigBubbleAnimator.getDuration() / 2);
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(bigBubbleAnimator, smallBubbleAnimator);

        return animatorSet;
    }

    //endregion Animations

    //region NavigationRequest Interface

    public enum NavigationType {
        Settings,
        Actions,
        Extensions,
        About
    }

    public interface OnNavigationRequestListener {
        void onNavigationRequest(NavigationType type, Point location);
    }

    protected void setOnNavigationRequestListener(OnNavigationRequestListener listener) {
        mNavigationListener = listener;
    }

    protected void setNavigationRequest(NavigationType type, Point location) {
        if (mNavigationListener != null)
            mNavigationListener.onNavigationRequest(type, location);
    }

    //endregion

    public void updateActionBar() {
        setActionBar(mToolbar);
        setSystemColor(R.color.color_primary_dark_navigation_fragment);
    }

    private Point getLocation(View view) {
        return new Point(getRelativeLeft(view) + view.getWidth() / 2, getRelativeTop(view));
    }

    private int getRelativeLeft(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getLeft();
        else
            return myView.getLeft() + getRelativeLeft((View) myView.getParent());
    }

    private int getRelativeTop(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View) myView.getParent());
    }

}
