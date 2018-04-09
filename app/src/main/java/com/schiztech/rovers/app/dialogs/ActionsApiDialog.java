package com.schiztech.rovers.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import com.facebook.rebound.ui.Util;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.utils.Utils;

/**
 * Created by schiz_000 on 4/15/2015.
 */
public class ActionsApiDialog extends DialogBase {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity().getApplicationContext();
        final SpannableString s =
                new SpannableString(Utils.getString(context,R.string.actions_api_desc_opening) +
                        "\n" +"\n" +
                        Utils.getString(context,R.string.actions_api_desc_middle) +
                        "\n" +"\n" +
                        Utils.getString(context,R.string.actions_api_desc_end));
        Linkify.addLinks(s, Linkify.WEB_URLS);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.actions_api_title)
                .setMessage(s)
                .setPositiveButton(R.string.actions_api_learn_more, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AnalyticsManager.getInstance(getActivity().getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                                AnalyticsManager.Action.Button_Click,
                                "API_Yes");

                        Utils.browseLink(getActivity(), R.string.link_api);
                    }
                })
                .setNegativeButton(R.string.actions_api_nevermind, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AnalyticsManager.getInstance(getActivity().getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                                AnalyticsManager.Action.Button_Click,
                                "API_No");
                    }
                })
                .setIcon(R.drawable.ri_misc_graduation)
                .show();
    }


    @Override
    protected int getDialogTagRes() {
        return R.string.dialog_actionsapi;
    }


}
