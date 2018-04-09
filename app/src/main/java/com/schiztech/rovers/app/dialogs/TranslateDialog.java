package com.schiztech.rovers.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.utils.Utils;

/**
 * Created by schiz_000 on 4/6/2015.
 */
public class TranslateDialog extends DialogBase {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity().getApplicationContext();
        final SpannableString s =
                new SpannableString(Utils.getString(context, R.string.translate_desc_start) +
                        "\n\n" +
                        Utils.getString(context, R.string.translate_desc_end)
                        +"\n\n" +
                        Utils.getString(context, R.string.link_translate));
        Linkify.addLinks(s, Linkify.WEB_URLS);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.translate_title)
                .setMessage(s)
                .setPositiveButton(R.string.translate_ill_help, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AnalyticsManager.getInstance(getActivity().getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                                AnalyticsManager.Action.Button_Click,
                                "Translate_Yes");

                        Utils.browseLink(getActivity(), R.string.link_translate);
                    }
                })
                .setNegativeButton(R.string.translate_no_thanks, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AnalyticsManager.getInstance(getActivity().getApplicationContext()).reportEvent(AnalyticsManager.Category.UX,
                                AnalyticsManager.Action.Button_Click,
                                "Translate_No");
                    }
                })
                .setIcon(R.drawable.ri_internet_globe)
                .show();
    }

    @Override
    protected int getDialogTagRes() {
        return R.string.dialog_translation;
    }

}
