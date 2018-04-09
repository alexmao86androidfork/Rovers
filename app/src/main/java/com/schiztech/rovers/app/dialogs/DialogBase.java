package com.schiztech.rovers.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.Button;
import android.widget.TextView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.utils.Utils;

/**
 * Created by schiz_000 on 4/15/2015.
 */
public abstract class DialogBase  extends DialogFragment {


    @Override
    public void onStart() {
        super.onStart();

        AnalyticsManager.getInstance(getActivity().getApplicationContext()).reportScreen(getDialogTag());

        if(Utils.isAndroidVersionEqualOrAbove(Build.VERSION_CODES.LOLLIPOP)) {
            int color = Utils.getColor(getActivity(), R.color.md_blue_500);
            Button positiveButton =  ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE);
            if(positiveButton != null) {
                positiveButton.setTextColor(color);
            }
            if(negativeButton != null) {
                negativeButton.setTextColor(color);
            }
        }
        TextView messageTextView = ((TextView)getDialog().findViewById(android.R.id.message));

        if(messageTextView != null) {
            messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }

    }

    protected abstract @StringRes int getDialogTagRes();

    protected String getDialogTag() {
        if(getActivity() != null) {
            return Utils.getString(getActivity().getApplicationContext(), getDialogTagRes());
        }
        return null;
    }
}
