package com.schiztech.rovers.actions.flashlight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class ToggleFlashlightActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //start service and die.
        startService(new Intent(getApplicationContext(), FlashlightService.class));
        finish();
    }
}
