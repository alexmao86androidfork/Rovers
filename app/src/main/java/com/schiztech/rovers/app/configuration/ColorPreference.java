package com.schiztech.rovers.app.configuration;

/**
 * Created by schiz_000 on 5/16/2014.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.adapters.ColorAdapter;
import com.schiztech.rovers.app.utils.BitmapUtils;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.Utils;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

/**
 * A preference that allows the user to choose an application or shortcut.
 */
public class ColorPreference extends Preference {
    private static final String TAG = LogUtils.makeLogTag("ColorPreference");
    private int[] mColorChoices;
    private int mValue = 0;
    private int mItemLayoutId = R.layout.grid_item_color;
    private int mNumColumns = -1;
    private View mPreviewView;

    public ColorPreference(Context context) {
        super(context);
        initAttrs(null, 0);
    }

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs, 0);
    }

    public ColorPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(attrs, defStyle);
    }

    private void initAttrs(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.ColorPreference, defStyle, defStyle);

        try {
            mItemLayoutId = a.getResourceId(R.styleable.ColorPreference_itemLayout, mItemLayoutId);
            mNumColumns = a.getInteger(R.styleable.ColorPreference_numColumns, mNumColumns);
            int choicesResId = a.getResourceId(R.styleable.ColorPreference_choices, -1);
            if (choicesResId > 0) {
                mColorChoices = a.getResources().getIntArray(choicesResId);
            }

        } finally {
            a.recycle();
        }

        setWidgetLayoutResource(mItemLayoutId);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mPreviewView = view.findViewById(R.id.color_view);
        ColorAdapter.setColorViewWithValue(mPreviewView, mValue, false);
    }

    public void setValue(int value) {
        if (callChangeListener(value)) {
            mValue = value;
            persistInt(value);
            notifyChanged();
        }
    }

    @Override
    protected void onClick() {
        super.onClick();

        ColorDialogFragment fragment = ColorDialogFragment.newInstance();
        fragment.init(mColorChoices, mValue, mNumColumns);
        fragment.setOnColorSelectedListener(new ColorDialogFragment.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int selectedColor) {
                ColorPreference.this.setValue(selectedColor);
            }
        });

        Activity activity = (Activity) getContext();
        activity.getFragmentManager().beginTransaction()
                .add(fragment, getFragmentTag())
                .commit();
    }

    @Override
    protected void onAttachedToActivity() {
        super.onAttachedToActivity();

        Activity activity = (Activity) getContext();
        ColorDialogFragment fragment = (ColorDialogFragment) activity
                .getFragmentManager().findFragmentByTag(getFragmentTag());
        if (fragment != null) {
            // re-bind preference to fragment
            fragment.setOnColorSelectedListener(new ColorDialogFragment.OnColorSelectedListener() {
                @Override
                public void onColorSelected(int selectedColor) {
                    ColorPreference.this.setValue(selectedColor);
                }
            });
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(0) : (Integer) defaultValue);
    }

    public String getFragmentTag() {
        return "color_" + getKey();
    }

    public int getValue() {
        return mValue;
    }


    public static class ColorDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {
        private int[] mColorChoices;
        private int mSelectedColor = 0;
        private int mNumColumns = -1;
        GridView mGridView;
        ColorAdapter mAdapter;

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(mListener != null)
                mListener.onColorSelected((Integer)mAdapter.getItem(i));

            dismiss();
        }


        public interface OnColorSelectedListener {
            void onColorSelected(int selectedColor);
        }
        private OnColorSelectedListener mListener;


        public ColorDialogFragment() {
        }

        public static ColorDialogFragment newInstance() {
            return new ColorDialogFragment();
        }

        public void init(int[] colorChoices, int selectedColor, int numColumns){
            this.mNumColumns = numColumns;
            this.mSelectedColor = selectedColor;
            this.mColorChoices = colorChoices;
        }

        public void setOnColorSelectedListener(OnColorSelectedListener listener) {
            mListener = listener;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }
        @Override
        public void onDetach(){
            mListener = null;
            super.onDetach();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View rootView = layoutInflater.inflate(R.layout.gridview_coloricon, null);

            mGridView = (GridView) rootView.findViewById(R.id.coloricon_grid);
            int padding = Utils.getDimensionPixelSize(mGridView.getContext(), R.dimen.color_preference_dialog_padding);
            mGridView.setPadding(padding,padding,padding,padding);

            if(mNumColumns > 0)
                mGridView.setNumColumns(mNumColumns);


            ((StickyGridHeadersGridView)mGridView).setAreHeadersSticky(false);
            if(mColorChoices != null) {
                mAdapter = new ColorAdapter(getActivity(), mSelectedColor, mColorChoices);
            }
            else{
                mAdapter = new ColorAdapter(getActivity(), mSelectedColor,(Integer)null);
            }
            mGridView.setAdapter(mAdapter);

            mGridView.setOnItemClickListener(this);

            return new AlertDialog.Builder(getActivity())
                    .setView(rootView)
                    .create();
        }



        @Override
        public void onDestroyView (){
            if(mGridView != null){
                Utils.unbindDrawables(mGridView);
                mGridView.setOnItemClickListener(null);
                mGridView = null;
            }
            mAdapter = null;
            super.onDestroyView();
        }

    }
}

