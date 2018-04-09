package com.schiztech.rovers.app.fragments.selectors;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.roveritems.IRover;
import com.schiztech.rovers.app.utils.ActivityInfo;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.Utils;

import java.util.List;

/**
 * Created by schiz_000 on 9/8/2014.
 */
public class SelectShortcutFragment  extends SelectIntentFragment {
    public static final String TAG = LogUtils.makeLogTag("SelectShortcutFragment");

    public static int REQUEST_CREATE_SHORTCUT = 1;

    public static SelectIntentFragment newInstance() {
        return new SelectShortcutFragment();
    }

    @Override
    protected Intent getIntentQuery() {
        return new Intent(Intent.ACTION_CREATE_SHORTCUT).addCategory(Intent.CATEGORY_DEFAULT);
    }

    @Override
    protected int getQueryFlags() {
        return 0;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Intent intent = mAdapter.getIntent(position);
        LogUtils.LOGD(TAG, "calling onActivityResult");
        startActivityForResult(intent, REQUEST_CREATE_SHORTCUT);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.LOGD(TAG, "onActivityResult request: " + requestCode + ", result: " + resultCode);
        if (requestCode == REQUEST_CREATE_SHORTCUT && resultCode == Activity.RESULT_OK) {
            LogUtils.LOGD(TAG, "intent selected");
            callOnIntentSelected(data, IRover.RoverType.Shortcut);
        }
    }

    @Override
    protected String getFragmentTag() {
        return Utils.getString(getActivity().getApplicationContext(), R.string.fragment_select_shortcut);

    }

    @Override
    public void onItemsLoaded(List<ActivityInfo> items) {

    }
}