package com.schiztech.rovers.app.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.utils.ExtensionsUtils;

/**
 * Created by schiz_000 on 3/20/2015.
 */
public class LockedContentLayout extends LinearLayout {

    private TextView mDescription;
    private ExtensionsUtils.ExtensionType mExtensionType;
    private View mGetExtensionsButton;
    private View mLayout;

    //region C'tors
    public LockedContentLayout(Context context) {
        super(context);
        init();
    }

    public LockedContentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LockedContentLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    //endregion C'tors

    //region OnGetExtensionsClickedListener
    public interface OnGetExtensionsClickedListener{
        void onGetExtensionsClicked();
    }

    private OnGetExtensionsClickedListener mListener;

    public void setOnGetExtensiosnClickedListener(OnGetExtensionsClickedListener listener){
        mListener = listener;
    }

    //endregion OnGetExtensionsClickedListener

    private void init() {
        inflate(getContext(), R.layout.layout_locked_content, this);
        mDescription = (TextView)findViewById(R.id.lockedContent_description);
        mGetExtensionsButton = findViewById(R.id.lockedContent_btn);
        mGetExtensionsButton.setOnClickListener(mGetExtensionsButtonClick);
        mLayout = findViewById(R.id.lockedContent_layout);
        hide();//defaulted hidden
    }

    public void show(){
        if(mLayout != null)
            mLayout.setVisibility(VISIBLE);
    }
    public void hide(){
        if(mLayout != null)
            mLayout.setVisibility(GONE);
    }

    public void setExtensionType(ExtensionsUtils.ExtensionType extensionType){
        mExtensionType = extensionType;
        if(mDescription != null) {
            mDescription.setText(ExtensionsUtils.getExtensionContentWarning(getContext(), extensionType));
        }
    }

    public void hideGetExtensionsButton(){
        if(mGetExtensionsButton != null){
            mGetExtensionsButton.setVisibility(View.GONE);
        }
    }

    public void showGetExtensionsButton(){
        if(mGetExtensionsButton != null){
            mGetExtensionsButton.setVisibility(View.VISIBLE);
        }
    }

    OnClickListener mGetExtensionsButtonClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            ExtensionsUtils.navigateToExtensionsScreen(getContext());

            if(mExtensionType != null) {
                AnalyticsManager.getInstance(getContext()).reportEvent(AnalyticsManager.Category.UX,
                        AnalyticsManager.Action.Button_Click,
                        "LockedContext_GetExtension", (long)mExtensionType.ordinal());
            }

            if(mListener != null){
                mListener.onGetExtensionsClicked();
            }

        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mGetExtensionsButton != null) {
            mGetExtensionsButton.setOnClickListener(null);
        }
        mGetExtensionsButtonClick = null;
        mGetExtensionsButton = null;
        mDescription = null;
        mListener = null;
    }
}
