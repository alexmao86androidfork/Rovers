package com.schiztech.rovers.actions.directcontact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by schiz_000 on 11/25/2014.
 */
public abstract class DirectTypeActionActivityBase extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivityForResult(Utils.getPickContactIntent(), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            Intent result = Utils.contactDataToRoverAction(getApplicationContext(), getDirectType(), data.getData(), getIconResource());

            setResult(RESULT_OK, result);
            finish();

        } else {
            //no answer...
            setResult(RESULT_CANCELED);
            finish();
        }

    }

    protected abstract Utils.DirectType getDirectType();

    protected abstract int getIconResource();
}
