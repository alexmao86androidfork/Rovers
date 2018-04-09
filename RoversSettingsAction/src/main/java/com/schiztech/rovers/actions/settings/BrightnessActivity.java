package com.schiztech.rovers.actions.settings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.schiztech.roversettingsaction.R;


public class BrightnessActivity extends Activity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    SeekBar mBrightnessSeekbar;
    ImageButton mBrightnessToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brightness);

        mBrightnessSeekbar = (SeekBar) findViewById(R.id.brightnessSeekbar);
        if (mBrightnessSeekbar != null) {
            mBrightnessSeekbar.setMax(Utils.getMaxBrightnessLevel());
            mBrightnessSeekbar.setOnSeekBarChangeListener(this);
        }

        mBrightnessToggle = (ImageButton) findViewById(R.id.brightnessToggle);
        if(mBrightnessToggle != null)
            mBrightnessToggle.setOnClickListener(this);

        updateViews();
        registerCloseReceiver();
    }

    @Override
    public void onDestroy() {
        if (mBrightnessSeekbar != null)
            mBrightnessSeekbar.setOnSeekBarChangeListener(null);

        mBrightnessSeekbar = null;
        mBrightnessToggle = null;
        unregisterCloseReceiver();
        super.onDestroy();
    }

    //region Update Views

    private void updateViews() {
        updateBrightnessSeekbar();
        updateBrightnessToggle();
    }

    private void updateBrightnessToggle(){
        if(mBrightnessToggle == null) return;
        boolean isAuto = Utils.isAutoBrightness(getApplicationContext());
        mBrightnessToggle.setImageResource(isAuto ? R.drawable.ic_brightness_auto : R.drawable.ic_brightness_high);

    }
    private void updateBrightnessSeekbar() {
        if (mBrightnessSeekbar == null) return;

        mBrightnessSeekbar.setEnabled(!Utils.isAutoBrightness(getApplicationContext()));
        mBrightnessSeekbar.setProgress(Utils.getBrightnessLevel(getApplicationContext()));
    }

    //endregion Update Views



    //region Seekbar Change Listener
    @Override
    public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
        Utils.setBrightnessLevel(getApplicationContext(),progressValue);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    //endregion Seekbar Change Listener

    //region On Click Listener
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.brightnessToggle) {
            Utils.toggleAutoBrightness(getApplicationContext());
            updateViews();
        }
    }

    //endregion On Click Listener



    //region Broadcast Receiver

    private BroadcastReceiver mCloseDialogReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    private void registerCloseReceiver(){
        registerReceiver(mCloseDialogReceiver, new IntentFilter("com.schiztech.rovers.roverhost.expanded"));
    }

    private void unregisterCloseReceiver(){
        unregisterReceiver(mCloseDialogReceiver);
        mCloseDialogReceiver = null;
    }




    //endregion
}
