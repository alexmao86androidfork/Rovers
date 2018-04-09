package com.schiztech.rovers.app.activities.base;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.utils.ExtensionsUtils;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.PrefUtils;
import com.schiztech.rovers.app.utils.Reflector;

/**
 * Created by schiz_000 on 4/8/2015.
 */
public abstract class ActionBarActivityBase  extends AppCompatActivity {
    private static final String TAG = LogUtils.makeLogTag("ActionBarActivity");
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        boolean isAnalyticsOptIn = PrefUtils.getMiscSendAnonymousDataValue(getApplicationContext());
        AnalyticsManager.getInstance(getApplicationContext()).setOptOut(!isAnalyticsOptIn);

        AnalyticsManager.getInstance(getApplicationContext()).getTracker();
    }

    @Override
    protected void onStart(){
        super.onStart();
        AnalyticsManager.getInstance(getApplicationContext()).reportActivityStart(this);
    }

    @Override
    protected void onStop(){
        AnalyticsManager.getInstance(getApplicationContext()).reportActivityStop(this);
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        AnalyticsManager.getInstance(getApplicationContext()).destroy();
        fixInputMethodManager();
    }

    private void fixInputMethodManager()
    {
        try {
            final Object imm = getSystemService(Context.INPUT_METHOD_SERVICE);

            final Reflector.TypedObject windowToken
                    = new Reflector.TypedObject(getWindow().getDecorView().getWindowToken(), IBinder.class);

            Reflector.invokeMethodExceptionSafe(imm, "windowDismissed", windowToken);

            final Reflector.TypedObject view
                    = new Reflector.TypedObject(null, View.class);

            Reflector.invokeMethodExceptionSafe(imm, "startGettingWindowFocus", view);
        }
        catch (Exception e){
            LogUtils.LOGE(TAG,"Failed fixing input method manager");

            if(e != null && e.getMessage() != null)
                LogUtils.LOGE(TAG,e.getMessage());
        }
    }

}
