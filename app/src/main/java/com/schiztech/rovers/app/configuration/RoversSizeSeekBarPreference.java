package com.schiztech.rovers.app.configuration;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.ui.CircleButton;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.RoversUtils;
import com.schiztech.rovers.app.utils.Utils;

/**
 * Created by schiz_000 on 7/12/2015.
 */
public class RoversSizeSeekBarPreference extends SeekBarListPreference {
    private static final String TAG = LogUtils.makeLogTag("RoversSizeSeekBarPreference");

    int mOriginalSize;
    CircleButton mNewSizeCircle;
//    CircleButton mOriginalSizeCircle;

    public RoversSizeSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mOriginalSize = RoversUtils.getRoverDefaultSize(context);
    }

    @Override
    protected View getCustomLayout() {
        View layout = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_pref_roversize, null, false);

        mNewSizeCircle = (CircleButton) layout.findViewById(R.id.roversize_newSize);
        mNewSizeCircle.setEnabled(false);
//        mOriginalSizeCircle = (CircleButton) layout.findViewById(R.id.roversize_originalSize);
//        mOriginalSizeCircle.setEnabled(false);

        return layout;

    }

    @Override
    protected void setProgressBarValue() {
        super.setProgressBarValue();
//        updateNewSizeCircle(getValue());
    }

    @Override
    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        super.onProgressChanged(seek, value, fromTouch);
        updateNewSizeCircle((String)getValueFromValue(value));
    }


    private void updateNewSizeCircle(String value) {
        try {
            float newSizeRatio = Integer.parseInt(value) / 100f;
            if (mNewSizeCircle != null) {
                mNewSizeCircle.setCircleSize((int)(mOriginalSize * newSizeRatio));
            }
        } catch (Exception e) {
            LogUtils.LOGE(TAG, "Error: " + e.getMessage());
        }
    }
}
