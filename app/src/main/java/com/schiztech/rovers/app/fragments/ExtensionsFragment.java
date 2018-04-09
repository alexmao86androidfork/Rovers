package com.schiztech.rovers.app.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.adapters.RoverExtensionRecyclerAdapter;
import com.schiztech.rovers.app.fragments.base.ContentRevealFragmentBase;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.managers.BatchManager;
import com.schiztech.rovers.app.managers.OpenIabManager;
import com.schiztech.rovers.app.utils.ExtensionsUtils;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.PrefKeys;
import com.schiztech.rovers.app.utils.PrefUtils;
import com.schiztech.rovers.app.utils.Utils;

public class ExtensionsFragment extends ContentRevealFragmentBase {
    public static final String TAG = LogUtils.makeLogTag("ExtensionsFragment");;

    View mRootView;
    ImageView mEnterPromoImage;

    View mEnterPromoLabel;
    EditText mEnterPromoEditText;
    RoverExtensionRecyclerAdapter mAdapter;
    boolean mIsEnteringPromo = false;

    //region create Instance

    public static ExtensionsFragment newInstance(boolean isTabletMode, int startX, int startY) {
        ExtensionsFragment fragment = new ExtensionsFragment();
        Bundle args = new Bundle();
        args.putInt(ARGUMENT_START_X, startX);
        args.putInt(ARGUMENT_START_Y, startY);
        args.putBoolean(ARGUMENT_TABLET_MODE, isTabletMode);
        fragment.setArguments(args);

        return fragment;
    }

    public static ExtensionsFragment newInstance(boolean isTabletMode) {
        ExtensionsFragment fragment = new ExtensionsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARGUMENT_TABLET_MODE, isTabletMode);
        fragment.setArguments(args);

        return fragment;
    }

    //endregion create Instance

    //region Base Methods


    @Override
    protected void onContentRevealed() {
        setSystemColor(R.color.color_primary_dark_extensions_fragment);

    }

    @Override
    protected int getPrimaryDarkColor() {
        return R.color.color_primary_dark_extensions_fragment;
    }

    @Override
    protected int getPrimaryColor() {
        return R.color.color_primary_extensions_fragment;
    }

    @Override
    protected int getFragmentTitle() {
        return R.string.extensions_fragment_title;
    }

    @Override
    protected View getLayoutView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_extensions, container, false);
    }

    @Override
    protected String getFragmentTag() {
        return Utils.getString(getContext(), R.string.fragment_extensions);
    }

    //endregion Base Methods

    //region Init Methods

    @Override
    protected void initViews(View rootView, Bundle savedInstanceState) {
        SharedPreferences prefs = PrefUtils.getMainSharedPreferences(getContext());
        if(prefs != null){
            LogUtils.LOGD(TAG, "register prefs listener");
            prefs.registerOnSharedPreferenceChangeListener(mPrefsListener);
        }
        mRootView = rootView;
        View promoButton = mRootView.findViewById(R.id.extensions_promotionLayout);
        ViewCompat.setElevation(promoButton, getResources().getDimensionPixelSize(R.dimen.coupon_code_elevation));

        mEnterPromoImage = (ImageView)mRootView.findViewById(R.id.extensions_promotionBtn);
        mEnterPromoImage.setOnClickListener(mEnterPromotionClick);
        mEnterPromoLabel = mRootView.findViewById(R.id.extensions_promotionLabel);
        mEnterPromoLabel.setOnClickListener(mEnterPromotionClick);

        mEnterPromoEditText = (EditText)mRootView.findViewById(R.id.extensions_promotionEditText);
        mEnterPromoEditText.getBackground().setColorFilter(getResources().getColor(R.color.md_pink_A200), PorterDuff.Mode.SRC_ATOP);

        initRecyclerView();
    }



    View.OnClickListener mEnterPromotionClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AnalyticsManager.getInstance(getContext()).reportEvent(AnalyticsManager.Category.UX,
                    AnalyticsManager.Action.Button_Click,
                    "Extensions_EnterPromotion");

            if(!mIsEnteringPromo){
                startEnterPromotionMode();
            }
            else{
                submitEnteredPromotionCode();
                endEnterPromotionMode();
            }
        }
    };

    private void initRecyclerView(){
        RecyclerView recList = (RecyclerView) mRootView.findViewById(R.id.extensions_list);

        recList.setHasFixedSize(true);
        LinearLayoutManager  layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(layoutManager);

        mAdapter = new RoverExtensionRecyclerAdapter(getContext());
        mAdapter.setBuyRequestListener(mBuyRequestListener);
        recList.setAdapter(mAdapter);
    }

    //endregion Init Methods

    //region Destroy Methods

    @Override
    public void onDestroyView() {
        SharedPreferences prefs = PrefUtils.getMainSharedPreferences(getContext());
        if(prefs != null){
            LogUtils.LOGD(TAG, "unregister prefs listener");

            prefs.unregisterOnSharedPreferenceChangeListener(mPrefsListener);
        }
        mPrefsListener = null;

        mRootView = null;
        if(mEnterPromoImage != null) {
            mEnterPromoImage.setOnClickListener(null);
            mEnterPromoImage = null;
        }
        if(mEnterPromoLabel != null) {
            mEnterPromoLabel.setOnClickListener(null);
            mEnterPromoLabel = null;
        }
        mEnterPromoEditText = null;
        if(mAdapter != null) {
            mAdapter.setBuyRequestListener(null);
        }
        mAdapter = null;
        mBuyRequestListener = null;

        super.onDestroyView();

    }

    //endregion Destroy Methods

    //region Buy Request Listener

    RoverExtensionRecyclerAdapter.OnBuyRequestListener mBuyRequestListener = new RoverExtensionRecyclerAdapter.OnBuyRequestListener() {
        @Override
        public void onBuyRequest(ExtensionsUtils.ExtensionType extensionType) {
            LogUtils.LOGD(TAG, "Purchase request: " + extensionType.toString());
            AnalyticsManager.getInstance(getActivity().getApplicationContext()).reportEvent(AnalyticsManager.Category.Extensions,
                    AnalyticsManager.Action.Buy_Request,
                    extensionType.toString());

            OpenIabManager.getInstance(getActivity()).launchPurchaseFlow(getActivity(), extensionType);
        }
    };

    //endregion Buy Request Listener

    //region Shared Prefs Update Listener

    SharedPreferences.OnSharedPreferenceChangeListener mPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if (key == null) return;
            if (key.equals(PrefKeys.EXTENSIONS_COMPLETE_PACKAGE) ||
                    key.equals(PrefKeys.EXTENSIONS_MORE_COLORS) ||
                    key.equals(PrefKeys.EXTENSIONS_MORE_ROVERS) ||
                    key.equals(PrefKeys.EXTENSIONS_MORE_SETTINGS)) {
                LogUtils.LOGD(TAG, "Purchase acknowledged, updating extensions markings");
                if (mAdapter != null)
                    mAdapter.updateInfosIsGot();

            }
        }
    };

    //endregion Shared Prefs Update Listener

    //region Promotion Code

    private void startEnterPromotionMode(){
        if(mEnterPromoLabel != null && mEnterPromoEditText != null && mEnterPromoImage != null) {
            mEnterPromoLabel.setVisibility(View.GONE);
            mEnterPromoEditText.setVisibility(View.VISIBLE);
            mEnterPromoEditText.setText(null);
            if(mEnterPromoEditText.requestFocus()) {
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mEnterPromoEditText, InputMethodManager.SHOW_IMPLICIT);
            }
            mEnterPromoImage.setImageResource(R.drawable.ic_extensions_submit);

            mIsEnteringPromo = true;
        }
    }

    private void submitEnteredPromotionCode(){
        if(mEnterPromoEditText == null || mEnterPromoEditText.getText() == null) return;

        String offerCode = mEnterPromoEditText.getText().toString();

        if(!offerCode.isEmpty()){
            BatchManager.getInstance(getContext()).onCodeEntered(offerCode);
        }
    }

    private void endEnterPromotionMode(){
        if(mEnterPromoLabel != null && mEnterPromoEditText != null && mEnterPromoImage != null) {
            mEnterPromoLabel.setVisibility(View.VISIBLE);
            mEnterPromoEditText.setVisibility(View.GONE);
            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEnterPromoEditText.getWindowToken(), 0);
            mEnterPromoImage.setImageResource(R.drawable.ic_extensions_promotion);
            mIsEnteringPromo = false;
        }
    }

    //endregion Promotion Code
}
