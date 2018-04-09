package com.schiztech.rovers.app.managers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.batch.android.Batch;
import com.batch.android.BatchCodeListener;
import com.batch.android.BatchRestoreListener;
import com.batch.android.BatchURLListener;
import com.batch.android.BatchUnlockListener;
import com.batch.android.CodeErrorInfo;
import com.batch.android.Config;
import com.batch.android.FailReason;
import com.batch.android.Feature;
import com.batch.android.Offer;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.utils.ExtensionsUtils;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.MarketUtils;
import com.schiztech.rovers.app.utils.Utils;

import java.util.List;
import java.util.Map;

/**
 * Created by schiz_000 on 4/1/2015.
 */
public class BatchManager implements BatchUnlockListener, BatchCodeListener, BatchURLListener,BatchRestoreListener {
    private static final String TAG = LogUtils.makeLogTag("BatchManager");

    private Context mApplicationContext;
    private Context mActivityContext;
    //region Features

    private static final String FEATURE_SETTINGS = "MORE_SETTINGS";
    private static final String FEATURE_ROVERS = "MORE_ROVERS";
    private static final String FEATURE_COLORS = "MORE_COLORS";
    private static final String FEATURE_COMPLETE = "WHOLE_PACKAGE";

    //region ExtensionType conversions
    private ExtensionsUtils.ExtensionType featureToExtensionType(String feature){
        LogUtils.LOGD(TAG, "Matching feature to extensionsType: " + feature);

        switch (feature){
            case FEATURE_COLORS:
                return ExtensionsUtils.ExtensionType.MoreColors;
            case FEATURE_ROVERS:
                return ExtensionsUtils.ExtensionType.MoreRovers;
            case FEATURE_SETTINGS:
                return ExtensionsUtils.ExtensionType.MoreSettings;
            case FEATURE_COMPLETE:
                return ExtensionsUtils.ExtensionType.CompletePackage;
        }

        LogUtils.LOGE(TAG, "Tried to get extension Type with unknown Feature: " + feature);
        return null;

    }

    private String extensionTypeToFeature(ExtensionsUtils.ExtensionType extensionType){
        switch (extensionType) {
            case MoreColors:
                return FEATURE_COLORS;
            case MoreSettings:
                return FEATURE_SETTINGS;
            case MoreRovers:
                return FEATURE_ROVERS;
            case CompletePackage:
                return FEATURE_COMPLETE;
        }

        LogUtils.LOGE(TAG, "Tried to get extension SKU with unknown type: " + extensionType.toString());

        return null;

    }

    //endregion ExtensionType conversions

    //endregion Features

    //region Singelton Fields & C'tor
    private BatchManager(Context applicationContext) {
        this.mApplicationContext = applicationContext;
    }

    private static BatchManager sInstance;
    public static BatchManager getInstance(Context applicationContext){
        if(sInstance == null) {
            sInstance = new BatchManager(applicationContext);
        }

        return sInstance;
    }

    //endregion

    //region Api Keys
    private boolean mIsDebugging = false;
    public void setDebugging(boolean isDebugging){
        LogUtils.LOGD(TAG, "setDebugging = " + isDebugging);
        mIsDebugging = isDebugging;
    }

    private String getApiKey(){
        LogUtils.LOGD(TAG, "getApiKey | debugging = " + mIsDebugging);

        return mIsDebugging ?
                "DEBUGGING_KEY" : //dev key
                "RELEASE_KEY"; //live key
    }

    private String getGCMKey(){
        LogUtils.LOGV(TAG, "getGCMKey");

        return "GCM_KEY";
    }


    //endregion Api Keys

    //region Activity States

    public void init(){
        LogUtils.LOGV(TAG, "init");

        Batch.setConfig(new Config(getApiKey()));
        Batch.Push.setGCMSenderId(getGCMKey());
        Batch.Push.setSmallIconResourceId(R.drawable.ic_notification_logo);
        Batch.Push.setLargeIcon(BitmapFactory.decodeResource(mApplicationContext.getResources(),
                R.drawable.ic_launcher));
    }

    public void onStart(Activity activity){
        LogUtils.LOGV(TAG, "onStart");

        mActivityContext = activity;
        Batch.Unlock.setUnlockListener(this);//listens to redeems
        Batch.onStart(activity);

        //restore once. if did it before, don't try again...
        if(getPrefRestoredSuccessfully() == false){
            restoreBatchFeatures();
        }
    }

    public void onStop(Activity activity){
        LogUtils.LOGV(TAG, "onStop");

        Batch.onStop(activity);
    }

    public void onDestroy(Activity activity){
        LogUtils.LOGV(TAG, "onDestroy");

        Batch.onDestroy(activity);

        mApplicationContext = null;
        mActivityContext = null;
        sInstance = null;
    }

    public void onNewIntent(Activity activity, Intent intent){
        LogUtils.LOGV(TAG, "onNewIntent");

        Batch.onNewIntent(activity, intent);
    }


    //endregion Activity States

    //region BatchUnlockListener

    @Override
    public void onRedeemAutomaticOffer(Offer offer) {
        if(offer != null) {
            LogUtils.LOGV(TAG, "onRedeemAutomaticOffer | offer: " + offer.getOfferReference());
        }

        handleOffer(offer);
        AnalyticsManager.getInstance(mApplicationContext).reportEvent(AnalyticsManager.Category.Batch,
                AnalyticsManager.Action.Automatic,
                offer.getOfferReference());

    }

    //endregion BatchUnlockListener

    //region BatchCodeListener

    public void onCodeEntered(String code){
        LogUtils.LOGI(TAG, "Checking Batch code | code = " + code);

        Toast.makeText(mApplicationContext, Utils.getString(mActivityContext, R.string.batch_checking_promotion_code) +" ("+code+")", Toast.LENGTH_SHORT).show();
        Batch.Unlock.redeemCode(code, this);
        AnalyticsManager.getInstance(mApplicationContext).reportEvent(AnalyticsManager.Category.Batch,
                AnalyticsManager.Action.Promo_Submit,
                code);

    }

    @Override
    public void onRedeemCodeSuccess(String s, Offer offer) {
        LogUtils.LOGI(TAG, "Batch Redeem code success! code = " + s);

        AnalyticsManager.getInstance(mApplicationContext).reportEvent(AnalyticsManager.Category.Batch,
                AnalyticsManager.Action.Promo_Result,
                s, -1L);

        handleOffer(offer);
    }

    @Override
    public void onRedeemCodeFailed(String s, FailReason reason, CodeErrorInfo infos) {
        LogUtils.LOGI(TAG, "Batch Redeem code has failed, reason: " + reason.toString() + ", Code: " + s);

        AnalyticsManager.getInstance(mApplicationContext).reportEvent(AnalyticsManager.Category.Batch,
                AnalyticsManager.Action.Promo_Result,
                s, (long)reason.ordinal());

        handleErrorAlert(reason, infos);
    }

    //endregion BatchCodeListener

    //region BatchURLListener

    @Override
    public void onURLWithCodeFound(String s) {
        LogUtils.LOGI(TAG, "Checking Url code: " + s);

        Toast.makeText(mApplicationContext, R.string.batch_found_promotion_code, Toast.LENGTH_SHORT);
        AnalyticsManager.getInstance(mApplicationContext).reportEvent(AnalyticsManager.Category.Batch,
                AnalyticsManager.Action.Url_Submit,
                s);
    }

    @Override
    public void onURLCodeSuccess(String s, Offer offer) {
        LogUtils.LOGI(TAG, "Batch URL code success! code = " + s);

        handleOffer(offer);
        AnalyticsManager.getInstance(mApplicationContext).reportEvent(AnalyticsManager.Category.Batch,
                AnalyticsManager.Action.Url_Result,
                s, -1L);
    }

    @Override
    public void onURLCodeFailed(String s, FailReason reason, CodeErrorInfo infos) {
        LogUtils.LOGI(TAG, "Batch URL code has failed, reason: " + reason.toString() + ", Code: " + s);
        handleErrorAlert(reason, infos);

        AnalyticsManager.getInstance(mApplicationContext).reportEvent(AnalyticsManager.Category.Batch,
                AnalyticsManager.Action.Url_Result,
                s, (long)reason.ordinal());
    }

    //endregion BatchURLListener

    //region BatchRestoreListener

    private void restoreBatchFeatures() {
        LogUtils.LOGI(TAG, "Restoring previous Batch offers redeemed");

        // You need to show the user a wait UI, like a loading spinner or something.
        Batch.Unlock.restore(this);
    }

    @Override
    public void onRestoreSucceed(List<Feature> features) {
        LogUtils.LOGI(TAG, "Batch restore finished successfully");

        //mark restored success so no more restores will launch
        setPrefRestoredSuccessfully(true);

        if( !features.isEmpty() )
        {
            LogUtils.LOGD(TAG, "Restore finished, " + features.size() + " features found");
            Toast.makeText(mApplicationContext, R.string.batch_restore_found_promotions, Toast.LENGTH_SHORT).show();
            handleFeatures(features);
        }
        else
        {
            LogUtils.LOGD(TAG, "Restore finished, no features found.");
        }
    }

    @Override
    public void onRestoreFailed(FailReason failReason) {
        // Hide the wait UI.
        // Show a message error to the user using the reason.
        LogUtils.LOGI(TAG, "Batch restore has failed, reason: " + failReason.toString());
    }

    //endregion BatchRestoreListener

    //region Custom Parameters

    private static final String BATCH_REWARD_MESSAGE = "reward_message";
    private static final String BATCH_REWARD_TITLE = "reward_title";

    private void showSuccessfulMessage(Offer offer){
        Map<String, String> additionalParameters = offer.getOfferAdditionalParameters();
        String rewardMessage;
        if(additionalParameters.containsKey(BATCH_REWARD_MESSAGE))
            rewardMessage  = additionalParameters.get(BATCH_REWARD_MESSAGE);
        else{//default value
            rewardMessage = Utils.getString(mActivityContext, R.string.batch_reward_message_default);
        }

        rewardMessage += "\n\n" + Utils.getString(mActivityContext, R.string.batch_rate_if_you_like) + "\n";

        String rewardTitle;
        if(additionalParameters.containsKey(BATCH_REWARD_TITLE))
            rewardTitle  = additionalParameters.get(BATCH_REWARD_TITLE);
        else{//default value
            rewardTitle = Utils.getString(mActivityContext, R.string.batch_reward_title_default);
        }

        //show dialog, if failed show toast
        if(!showSuccessDialog(mActivityContext, rewardTitle, rewardMessage)) {
            Toast.makeText(mActivityContext, rewardTitle +": " + rewardMessage,Toast.LENGTH_LONG).show();
        }

    }

    public static boolean showSuccessDialog(final Context context, String titleString, String messageString) {
        if(context == null ) return false;

        try {
            AlertDialog.Builder bld = new AlertDialog.Builder(context);
            bld.setTitle(titleString);
            bld.setMessage(messageString);
            bld.setPositiveButton(Utils.getString(context, R.string.dialog_OK), null);
            bld.setNeutralButton(Utils.getString(context, R.string.extensions_rate), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    AnalyticsManager.getInstance(context).reportEvent(AnalyticsManager.Category.UX,
                            AnalyticsManager.Action.Button_Click,
                            "AlertDialog_Rate");

                    Utils.browseLink(context, MarketUtils.getPackagePlayStoreLink(context.getPackageName(), false));

                }
            });
            Log.d(TAG, "Showing alert dialog: title" + titleString + "; message = " + messageString);
            bld.create().show();
            return true;
        }
        catch (Exception e){
            return false;
        }

    }


    //endregion Custom Parameters

    //region Success & Error handlers

    private void handleErrorAlert(FailReason reason, CodeErrorInfo infos){

        if( reason == FailReason.NETWORK_ERROR ) // 1
        {
            if(mActivityContext != null) {
                Utils.complain(mActivityContext, R.string.batch_error_network);
            }
        }
        else if( reason == FailReason.INVALID_CODE )
        {
            int toastMessage;
            switch( infos.getType() )
            {
                case UNKNOWN_CODE : // 2
                    toastMessage = R.string.batch_error_unknown_code;
                    break;
                case OFFER_EXPIRED : // 3.1
                    toastMessage = R.string.batch_error_expired_code;
                    break;
                case OFFER_NOT_STARTED : // 3.2
                    toastMessage = R.string.batch_error_not_started;
                    break;
                case OFFER_CAPPED : // 3.3
                    // Explain that the user is late because the cap has already been reached,
                    // tell them to be quicker next time!
                    toastMessage = R.string.batch_error_capped;
                    break;
                case ALREADY_CONSUMED : // 3.4
                    // Explain to the user that he already used this promocode.
                    // You can tell him to use the Restore button if you included it
                    // with the Restore function of Batch.
                    toastMessage = R.string.batch_error_already_consumed;

                    break;
                case USER_NOT_TARGETED : // 3.6
                    // Explain that the user is not eligible for this offer.
                    toastMessage = R.string.batch_error_not_targeted;

                    break;
                default :
                    // Display a generic error asking them to retry later.
                    toastMessage = R.string.batch_error_default;
                    break;
            }
            Toast.makeText(mApplicationContext, toastMessage, Toast.LENGTH_SHORT).show();
        }
        else
        {
            // Display a generic error asking them to retry later.
            Toast.makeText(mApplicationContext, R.string.batch_error_default, Toast.LENGTH_SHORT).show();
        }

    }

    private void handleOffer(Offer offer){
        if(offer == null) return;

        LogUtils.LOGD(TAG, "handling Batch offer: " + offer.getOfferReference());

        showSuccessfulMessage(offer);
        List<Feature> features = offer.getFeatures();
        if(features == null){
            return;
        }

        handleFeatures(features);
    }

    private void handleFeatures(List<Feature> features){
        LogUtils.LOGD(TAG, "handling features: " + features.size());

        for(Feature feature : features){
            if(feature != null && feature.getReference() != null)
                SetExtensionOwnedState(feature);
        }

    }

    private void SetExtensionOwnedState(Feature feature) {
        ExtensionsUtils.ExtensionType type = featureToExtensionType(feature.getReference().toUpperCase());
        if (type != null) {
            LogUtils.LOGI(TAG, "Feature bounded to extension type " + type.toString() + ", marking as owned ");
            //mark extension as owned
            if(ExtensionsUtils.isGotExtension(mApplicationContext,type)){
                Toast.makeText(mApplicationContext, Utils.getString(mActivityContext, R.string.extensions_already_got) +" (" +ExtensionsUtils.getExtensionName(mApplicationContext, type) +")",Toast.LENGTH_SHORT).show();
            }
            else {
                ExtensionsUtils.setGotExtension(mApplicationContext, type, true);

                AnalyticsManager.getInstance(mApplicationContext).reportEvent(AnalyticsManager.Category.Batch,
                        AnalyticsManager.Action.Got_Changed,
                        type.toString(), 1L);
            }
        }
    }



    //endregion Success & Error handlers

    //region Shared Prefs

    private static final String PREFS_FILE_BATCH = "batch_prefs";
    private static final String PREF_RESTORED_SUCCESSFULLY  = "restored_successfully";
    private static final boolean PREF_RESTORED_SUCCESSFULLY_DEFAULT  = false;

    private static SharedPreferences getBatchSharedPreferences(Context context){
        return context.getSharedPreferences(PREFS_FILE_BATCH, 0);
    }

    private void setPrefRestoredSuccessfully(boolean value){
        if(mApplicationContext != null) {
            SharedPreferences prefs = getBatchSharedPreferences(mApplicationContext);
            if (prefs != null)
                prefs.edit().putBoolean(PREF_RESTORED_SUCCESSFULLY, value).apply();
        }
    }

    private boolean getPrefRestoredSuccessfully(){
        SharedPreferences prefs = getBatchSharedPreferences(mApplicationContext);
        if(prefs != null)
            return prefs.getBoolean(PREF_RESTORED_SUCCESSFULLY, PREF_RESTORED_SUCCESSFULLY_DEFAULT);

        return PREF_RESTORED_SUCCESSFULLY_DEFAULT;
    }

    //endregion Shared Prefs


}
