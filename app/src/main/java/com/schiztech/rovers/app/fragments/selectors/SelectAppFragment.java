package com.schiztech.rovers.app.fragments.selectors;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.roveritems.IRover;
import com.schiztech.rovers.app.utils.ActivityInfo;
import com.schiztech.rovers.app.utils.Utils;

import java.util.List;

/**
 * Created by schiz_000 on 9/8/2014.
 */
public class SelectAppFragment extends SelectIntentFragment {


    public static SelectIntentFragment newInstance() {
        return new SelectAppFragment();
    }

    @Override
    protected Intent getIntentQuery() {
        return new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
    }

    @Override
    protected int getQueryFlags() {
        return 0;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Intent intent = mAdapter.getIntent(position);
        if (intent != null) {
            intent = Intent.makeMainActivity(intent.getComponent());

            callOnIntentSelected(intent, IRover.RoverType.Application);
        }
    }

    @Override
    protected String getFragmentTag() {
        return Utils.getString(getActivity().getApplicationContext(), R.string.fragment_select_app);

    }

    @Override
    public void onItemsLoaded(List<ActivityInfo> items) {

    }
}
