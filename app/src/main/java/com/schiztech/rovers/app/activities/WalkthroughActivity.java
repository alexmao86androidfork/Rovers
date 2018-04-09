package com.schiztech.rovers.app.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.base.ActionBarActivityBase;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.managers.BatchManager;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.PrefUtils;
import com.schiztech.rovers.app.utils.Utils;

import me.relex.circleindicator.CircleIndicator;

public class WalkthroughActivity extends ActionBarActivityBase implements View.OnClickListener{
    private static final String TAG = LogUtils.makeLogTag("WalkthroughActivity");

    private static final int STEPS_COUNT = 5;

    //region Steps Resources
    static int[] mHeaders = new int[]{
            R.string.walkthrough_step1_title,
            R.string.walkthrough_step2_title,
            R.string.walkthrough_step3_title,
            R.string.walkthrough_step4_title
    };

    static int[] mDescriptions = new int[]{
            R.string.walkthrough_step1_desc,
            R.string.walkthrough_step2_desc,
            R.string.walkthrough_step3_desc,
            R.string.walkthrough_step4_desc,
    };

    static int[] mImagesFirst = new int[]{
            R.drawable.walkthrough_step_1,
            R.drawable.walkthrough_step_2,
            R.drawable.walkthrough_step_3,
            R.drawable.walkthrough_step_4
    };
    static int[] mImagesSecond = new int[]{
            R.drawable.walkthrough_step_1_lines,
            R.drawable.walkthrough_step_2_lines,
            R.drawable.walkthrough_step_3_lines,
            R.drawable.walkthrough_step_4_lines
    };
    //endregion Steps Resources

    private View mNextButton;
    private View mSkipButton;
    private View mDoneButton;
    private View mBottomLayout;
    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough);

        mNextButton = findViewById(R.id.walkthrough_nextButton);
        mNextButton.setOnClickListener(this);
        mSkipButton = findViewById(R.id.walkthrough_skipButton);
        mSkipButton.setOnClickListener(this);
        mDoneButton = findViewById(R.id.walkthrough_doneButton);
        mDoneButton.setOnClickListener(this);
        mBottomLayout = findViewById(R.id.walkthrough_bottomLayout);

        ScreenSlidePagerAdapter adapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.walkthrough_viewPager);
        mViewPager.setAdapter(adapter);

        final CircleIndicator circleIndicator = (CircleIndicator)findViewById(R.id.walkthrough_circleIndicator);
        circleIndicator.setViewPager(mViewPager);

        circleIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    handleButtons(position);

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        //report analytics if fist time doing Walkthrough:
        boolean isFirstTimer = PrefUtils.getWalkthroughIsFinishedValue(getApplicationContext());
        LogUtils.LOGD(TAG, "starting Walkthrough firstTimer = " + isFirstTimer);

        AnalyticsManager.getInstance(getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                AnalyticsManager.Action.Walkthrough,
                "Started", isFirstTimer ? 1L : 0L);

    }

    @Override
    public void onDestroy(){
        if(mViewPager !=null) {
            mViewPager.setOnPageChangeListener(null);
            mViewPager = null;
        }

        if(mNextButton != null){
            mNextButton.setOnClickListener(null);
            mNextButton = null;
        }
        if(mDoneButton != null){
            mDoneButton.setOnClickListener(null);
            mDoneButton = null;
        }
        if(mSkipButton != null){
            mSkipButton.setOnClickListener(null);
            mSkipButton = null;
        }

        mBottomLayout =null;

        super.onDestroy();
    }

    private void handleButtons(int currentStep){
        if(currentStep == STEPS_COUNT - 1) {//last page
            if(mDoneButton != null)
                mDoneButton.setVisibility(View.VISIBLE);
            if(mBottomLayout != null)
                mBottomLayout.setVisibility(View.GONE);
        }
        else{//not last page
            if(mDoneButton != null)
                mDoneButton.setVisibility(View.GONE);
            if(mBottomLayout != null)
                mBottomLayout.setVisibility(View.VISIBLE);
        }
    }

    private void finishWalkthrough(){

        boolean isFirstTimer = false;
        //if finished Walkthrough for the first time - launch Rovers
        if(!PrefUtils.getWalkthroughIsFinishedValue(getApplicationContext())){
            boolean isRoversActivated = PrefUtils.getRoversIsActivatedValue(getApplicationContext());
            Utils.syncRoverWindow(this, isRoversActivated);

            isFirstTimer = true;
        }
        LogUtils.LOGD(TAG, "finished Walkthrough firstTimer = " + isFirstTimer);

        //mark Walkthrough as Finished
        PrefUtils.setWalkthroughIsFinishedValue(getApplicationContext(), true);


        AnalyticsManager.getInstance(getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                AnalyticsManager.Action.Walkthrough,
                "Finished", isFirstTimer ? 1L:0L);

        //close Walkthrough
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.walkthrough_nextButton:
                if(mViewPager != null) {
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
                }
                break;
            case R.id.walkthrough_doneButton:
                finishWalkthrough();
                break;
            case R.id.walkthrough_skipButton:
                finishWalkthrough();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finishWalkthrough();
    }


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override public Fragment getItem(int position) {
            return WalkthroughStepFragment.getInstance(position);
        }

        @Override public int getCount() {
            return STEPS_COUNT;
        }
    }

    public static class WalkthroughStepFragment
            extends Fragment {

        public static final String ARGUMENT_POSITION = "ARGUMENT_POSITION";

        public static WalkthroughStepFragment getInstance(int position){
            WalkthroughStepFragment fragment = new WalkthroughStepFragment();

            Bundle args = new Bundle();
            args.putInt(WalkthroughStepFragment.ARGUMENT_POSITION, position);
            fragment.setArguments(args);

            return fragment;
        }

        public WalkthroughStepFragment() {
        }


        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            int position = 0;
            if (getArguments() != null) {
                position = getArguments().getInt(ARGUMENT_POSITION, 0);
            }

            ViewGroup rootView;
            if(position < STEPS_COUNT -1) {
                rootView = (ViewGroup) inflater.inflate(
                        R.layout.fragment_walkthrough_step, container, false);

                if (mImagesFirst[position] == 0) {
                    rootView.findViewById(R.id.walkthroughStep_image1).setVisibility(View.GONE);
                } else {
                    rootView.findViewById(R.id.walkthroughStep_image1).setVisibility(View.VISIBLE);
                    ((ImageView) rootView.findViewById(R.id.walkthroughStep_image1)).setImageResource(mImagesFirst[position]);
                }
                if (mImagesSecond[position] == 0) {
                    rootView.findViewById(R.id.walkthroughStep_image2).setVisibility(View.GONE);
                } else {
                    rootView.findViewById(R.id.walkthroughStep_image2).setVisibility(View.VISIBLE);
                    ((ImageView) rootView.findViewById(R.id.walkthroughStep_image2)).setImageResource(mImagesSecond[position]);
                }

                ((TextView) rootView.findViewById(R.id.walkthroughStep_headline)).setText(mHeaders[position]);
                ((TextView) rootView.findViewById(R.id.walkthroughStep_description)).setText(mDescriptions[position]);
            }
            else{
                rootView = (ViewGroup) inflater.inflate(
                        R.layout.fragment_walkthrough_step_last, container, false);
            }

            return rootView;
        }
    }

}
