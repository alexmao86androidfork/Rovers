package com.schiztech.rovers.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.schiztech.rovers.app.utils.RoverlyticsUtils;

public class RoverlyticsResultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int totalLaunches = RoverlyticsUtils.getLaunches(getApplicationContext());
        float totalTime = RoverlyticsUtils.getTime(getApplicationContext());
        float totalDistance = RoverlyticsUtils.getDistance(getApplicationContext());

        Bundle bundle = new Bundle();
        bundle.putInt(RoverlyticsUtils.TOTAL_LAUNCHES, totalLaunches);
        bundle.putFloat(RoverlyticsUtils.TOTAL_TIME, totalTime);
        bundle.putFloat(RoverlyticsUtils.TOTAL_DISTANCE, totalDistance);

        Intent result = new Intent(getApplicationContext(), RoverlyticsResultActivity.class);
        result.putExtras(bundle);


        setResult(Activity.RESULT_OK, result);
        finish();


    }


}
