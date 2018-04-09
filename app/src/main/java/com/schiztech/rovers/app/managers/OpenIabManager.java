package com.schiztech.rovers.app.managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.PurchaseEvent;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.utils.ExtensionsUtils;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.Utils;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.googleUtils.IabHelper;
import org.onepf.oms.appstore.googleUtils.IabResult;
import org.onepf.oms.appstore.googleUtils.Inventory;
import org.onepf.oms.appstore.googleUtils.Purchase;
import org.onepf.oms.appstore.googleUtils.SkuDetails;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Map;

/**
 * Created by schiz_000 on 3/23/2015.
 */
public class OpenIabManager {
//    private final boolean AUTO_CONSUME_ALL = false; // DEBUGGING PURPOSES ONLY todo remove before publish!!

    private static final String TAG = LogUtils.makeLogTag("OpenIabManager");

    //region Singelton Fields & C'tor
    private OpenIabManager(Context applicationContext) {
        this.mApplicationContext = applicationContext;

        mAutoConsumeSkus.add(InAppConfig.SKU_COFFEE);
    }

    private static OpenIabManager sInstance;
    public static OpenIabManager getInstance(Context applicationContext){
        if(sInstance == null) {
            sInstance = new OpenIabManager(applicationContext);
        }

        return sInstance;
    }

    //endregion

    private Context mApplicationContext;
    private  OpenIabHelper mHelper;
    private  Boolean mIsOpenIabSetupDone = null;
    static final int RC_REQUEST = 10001;

    List<String> mAutoConsumeSkus = new ArrayList<>();
    Map<String, SkuDetails> mAvailableExtensions;

    //region ExtensionType conversions

    private String extensionTypeToSku(ExtensionsUtils.ExtensionType extensionType){
        switch (extensionType) {
            case MoreColors:
                return InAppConfig.SKU_COLORS;
            case MoreSettings:
                return InAppConfig.SKU_SETTINGS;
            case MoreRovers:
                return InAppConfig.SKU_ROVERS;
            case CompletePackage:
                return InAppConfig.SKU_COMPLETE;
            case Coffee:
                return InAppConfig.SKU_COFFEE;
        }

        LogUtils.LOGE(TAG, "Tried to get extension SKU with unknown type: " + extensionType.toString());
        return null;

    }

    private ExtensionsUtils.ExtensionType skuToExtensionType(String sku){
        switch (sku){
            case InAppConfig.SKU_COFFEE:
                return ExtensionsUtils.ExtensionType.Coffee;
            case InAppConfig.SKU_COLORS:
                return ExtensionsUtils.ExtensionType.MoreColors;
            case InAppConfig.SKU_COMPLETE:
                return ExtensionsUtils.ExtensionType.CompletePackage;
            case InAppConfig.SKU_ROVERS:
                return ExtensionsUtils.ExtensionType.MoreRovers;
            case InAppConfig.SKU_SETTINGS:
                return ExtensionsUtils.ExtensionType.MoreSettings;
        }

        LogUtils.LOGE(TAG, "Tried to get extension Type with unknown SKU: " + sku);

        return null;

    }

    //endregion ExtensionType conversions

    //region OpenIAB States

    public void buildOpenIabHelper(Context context){
        disposeOpenIabHelper();//make sure the helper is disposed before creating it again.

        // Create the helper, passing it our context and the public key to verify signatures with
        LogUtils.LOGD(TAG, "Creating IAB helper.");
        //Only map SKUs for stores that using purchase with SKUs different from described in store console.
        OpenIabHelper.Options.Builder builder = new OpenIabHelper.Options.Builder()
                .setStoreSearchStrategy(OpenIabHelper.Options.SEARCH_STRATEGY_INSTALLER_THEN_BEST_FIT)
                .setVerifyMode(OpenIabHelper.Options.VERIFY_SKIP)
                .addStoreKeys(InAppConfig.STORE_KEYS_MAP);
        mHelper = new OpenIabHelper(context, builder.build());
    }

    public void startSetupOpenIabHelper(final Context context){
        LogUtils.LOGV(TAG, "Starting setup for IAB helper.");
        try {
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    LogUtils.LOGV(TAG, "IAB helper Setup finished.");

                    if (!result.isSuccess()) {
                        // Oh noes, there was a problem.
                        mIsOpenIabSetupDone = false;
                        //updateUi();
                        LogUtils.LOGW(TAG, "Problem setting up in-app billing: " + result);
                        return;
                    }

                    // Hooray, IAB is fully set up. Now, let's get an inventory of stuff we own.
                    LogUtils.LOGD(TAG, "Setup successful. Querying inventory.");
                    mIsOpenIabSetupDone = true;
                    //use helper class to attach context to the listener
                    queryInventoryAsync();
                }
            });
        }
        catch (Exception e){
            LogUtils.LOGD(TAG, "Setup failed: " + e.getMessage());
//            Crashlytics.getInstance().core.logException(e);
        }
    }

    private void disposeOpenIabHelper(){
        // very important:
        mIsOpenIabSetupDone = null;
        LogUtils.LOGV(TAG, "Destroying IAB helper.");
        if (mHelper != null) mHelper.dispose();
        mHelper = null;

        mAvailableExtensions = null;
    }

    public void disposeSelf(){
        disposeOpenIabHelper();
        mOnQueryInventoryFinished = null;
        mOnIabPurchaseFinished = null;

        sInstance = null;
    }

    //endregion OpenIAB States

    //region Purchase Flow

    public void launchPurchaseFlow(Activity activity, ExtensionsUtils.ExtensionType extensionType){
        if(ExtensionsUtils.isGotExtension(activity.getApplicationContext() ,extensionType)){
            Utils.complain(mApplicationContext, R.string.extensions_already_got);
            return;
        }

        if (mIsOpenIabSetupDone == null) {
            Utils.complain(mApplicationContext, R.string.billing_error_setup_incomplete);
            return;
        }

        if (!mIsOpenIabSetupDone) {
            Utils.complain(mApplicationContext, R.string.billing_error_setup_failed);
            return;
        }

        String sku = extensionTypeToSku(extensionType);
        if(sku == null){
            Utils.complain(mApplicationContext, R.string.billing_error_sku_extension_not_found);
            return;
        }

        LogUtils.LOGI(TAG, "Launching purchase flow for " + extensionType.toString());

        //todo come up with a good payload!
        String payload = createDeveloperPayload();

        mHelper.launchPurchaseFlow(activity, sku, RC_REQUEST, mOnIabPurchaseFinished,payload);
    }

    //region OnIabPurchaseFinished

    private IabHelper.OnIabPurchaseFinishedListener  mOnIabPurchaseFinished = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            LogUtils.LOGI(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            if (result.isFailure()) {
                complainBadResult(result);
            }

            else {
                //if no error, handle the purchase like it should.
                handlePurchase(purchase);
            }

            if(result != null && purchase != null) {
                try {
                    AnalyticsManager.getInstance(mApplicationContext).reportEvent(AnalyticsManager.Category.Purchases,
                            AnalyticsManager.Action.Flow_Result,
                            purchase.getSku(), (long) result.getResponse());
                    BigDecimal price = InAppConfig.SKU_COMPLETE.equals(purchase.getSku()) ? BigDecimal.valueOf(2.50) : BigDecimal.valueOf(1);
                    Answers.getInstance().logPurchase(new PurchaseEvent()
                            .putItemPrice(price)
                            .putCurrency(Currency.getInstance("USD"))
                            .putItemName(purchase.getSku())
                            .putItemType(purchase.getItemType())
                            .putItemId(purchase.getSku())
                            .putSuccess(result.isSuccess()));

                }
                catch (Exception e){
                    Crashlytics.getInstance().core.logException(e);
                }
            }
        }
    };


    //endregion OnIabPurchaseFinished

    //endregion Purchase Flow

    //region Consume Flow

    public void launchConsumeFlow(Purchase purchase){

        if(purchase == null){
            Utils.complain(mApplicationContext, R.string.billing_error_consume_empty);
            return;

        }
        LogUtils.LOGE(TAG, "Consuming item: " + purchase.getSku());

        if (mIsOpenIabSetupDone == null) {
            Utils.complain(mApplicationContext, R.string.billing_error_setup_incomplete);
            return;
        }

        if (!mIsOpenIabSetupDone) {
            Utils.complain(mApplicationContext, R.string.billing_error_setup_failed);
            return;
        }

        LogUtils.LOGI(TAG, "Launching consume flow for " + purchase.getSku());

        mHelper.consumeAsync(purchase, mConsumeFinishedListener);
    }

    //region OnConsumeFinished

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            LogUtils.LOGI(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);
            if(result.isFailure()){
                complainBadResult(result);
            }
        }
    };

    //endregion OnConsumeFinished

    //endregion Consume Flow

    //region Payload

    private String createDeveloperPayload(){
        return "";
    }

    private boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    //endregion

    //region Purchase handles

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data){
        if(mHelper != null)
            return mHelper.handleActivityResult(requestCode,resultCode,data);

        return false;
    }

    private void handlePurchase(Purchase purchase){
        //verify purchase
        if(!verifyDeveloperPayload(purchase)) {
            Utils.complain(mApplicationContext, R.string.billing_error_verification_failed_full);
            return;
        }

        //check if purchase if a consumable item
        if(mAutoConsumeSkus.contains(purchase.getSku())){
            LogUtils.LOGD(TAG, "Purchase found as an auto consume item, starting consume flow");
            launchConsumeFlow(purchase);//auto consume if needed

        }

        if(purchase.getSku().toLowerCase().equals(InAppConfig.SKU_COFFEE))
            setThanksAlert(mApplicationContext);//coffee... donation

        //if not consumable & not owned already, mark extension as owned
        else if (!isExtensionsOwned(purchase)){
            SetExtensionOwnedState(purchase, true);
        }

    }

    /**
     * Sets the extension owned state
     * @param purchase info of the purchase that was made
     * @param value either to mark extension as owned
     */

    private boolean isExtensionsOwned(Purchase purchase){
        ExtensionsUtils.ExtensionType type = skuToExtensionType(purchase.getSku());
        return ExtensionsUtils.isGotExtension(mApplicationContext, type);
    }

    private void SetExtensionOwnedState(Purchase purchase, boolean value) {
        ExtensionsUtils.ExtensionType type = skuToExtensionType(purchase.getSku());
        if (type != null) {
            LogUtils.LOGI(TAG, "Purchase bounded to extension type " + type.toString() + ", marking as owned ");
            //mark extension as owned

            ExtensionsUtils.setGotExtension(mApplicationContext, type, value);

            AnalyticsManager.getInstance(mApplicationContext).reportEvent(AnalyticsManager.Category.Purchases,
                    AnalyticsManager.Action.Got_Changed,
                    type.toString(), value ? 1L : 0L);

        }
    }

    private void setThanksAlert(Context context){
        Utils.alertWithTitle(context, R.string.billing_thank_you,
                Utils.getString(mApplicationContext, R.string.billing_coffee_thanks_start)
                +"\n"+
                Utils.getString(mApplicationContext, R.string.billing_coffee_thanks_end)
        );
    }


    private void complainBadResult(IabResult result){
        int errorDesc;
        switch (result.getResponse()){
            default:
                return;//don't show message for error like "user cancel"....
            case IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED :
                errorDesc = R.string.billing_error_already_owned;
                break;
            case IabHelper.IABHELPER_BAD_RESPONSE:
                errorDesc = R.string.billing_error_bad_response;
                break;
            case IabHelper.IABHELPER_VERIFICATION_FAILED :
                errorDesc = R.string.billing_error_verification_failed;
                break;
            case IabHelper.IABHELPER_SEND_INTENT_FAILED:
                errorDesc = R.string.billing_error_send_intent_failed;
                break;
            case IabHelper.IABHELPER_UNKNOWN_PURCHASE_RESPONSE:
                errorDesc = R.string.billing_error_unknown_purchase_response;
                break;
            case IabHelper.IABHELPER_MISSING_TOKEN:
                errorDesc = R.string.billing_error_missing_token;
                break;
            case IabHelper.IABHELPER_UNKNOWN_ERROR:
                errorDesc = R.string.billing_error_unknown_error;
                break;
            case IabHelper.IABHELPER_INVALID_CONSUMPTION:
                errorDesc = R.string.billing_error_invalid_consumption;
                break;


        }

        String message= Utils.getString(mApplicationContext, R.string.billing_purchase_error) +
                        " #" +result.getResponse() + ": " +
                        Utils.getString(mApplicationContext, errorDesc) + "." +
                        "\n" +
                        Utils.getString(mApplicationContext, R.string.billing_contact_if_error_continues);

        Utils.complain(mApplicationContext, message);
    }

    //endregion Purchase handles

    //region Inventory Checkups

    public void queryInventoryAsync(){
        if(mHelper != null) {
            mHelper.queryInventoryAsync(true, Arrays.asList(InAppConfig.SKU_ARRAY), mOnQueryInventoryFinished);
        }
    }

    //region OnQueryInventoryFinished

    private IabHelper.QueryInventoryFinishedListener mOnQueryInventoryFinished = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            LogUtils.LOGD(TAG, "Query inventory finished.");
            if(result == null){
                LogUtils.LOGE(TAG, "Query inventory failed. result is null");
                return;
            }

            if (result.isFailure()) {
                LogUtils.LOGE(TAG, "Query inventory failed: " + result.getMessage() );
                Toast.makeText(mApplicationContext, Utils.getString(mApplicationContext, R.string.billing_error_verify_previous_purchases) + ": " + result,Toast.LENGTH_SHORT).show();
                return;
            }

            if(inventory == null){
                LogUtils.LOGE(TAG, "Query inventory failed. inventory is null");
                return;
            }

            LogUtils.LOGI(TAG, "Query inventory finished successfully.");
            //list the available items
            mAvailableExtensions = inventory.getSkuMap();
            LogUtils.LOGD(TAG, mAvailableExtensions.size() + " extensions listed");
            // Check for items we own. Notice that for each purchase, we check
            List<Purchase> ownedPurchases = inventory.getAllPurchases();

            for(Purchase purchase : ownedPurchases){
//                if(AUTO_CONSUME_ALL) {
//                    launchConsumeFlow(purchase);
//                    SetExtensionOwnedState(purchase,false);
//                }
//                else {
                    handlePurchase(purchase);
//                }
            }


        }
    };

    //endregion OnQueryInventoryFinished

    public String getExtensionPrice(ExtensionsUtils.ExtensionType extensionType){
        String sku = extensionTypeToSku(extensionType);
        if(sku == null) {
            return null;
        }

        if(mAvailableExtensions == null || !mAvailableExtensions.containsKey(sku)){
            return null;
        }

        return mAvailableExtensions.get(sku).getPrice();
    }

    //endregion Inventory Checkups


}
