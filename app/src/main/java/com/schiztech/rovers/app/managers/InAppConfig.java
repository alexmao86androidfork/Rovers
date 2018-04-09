package com.schiztech.rovers.app.managers;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.SkuManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by schiz_000 on 3/22/2015.
 */
public final class InAppConfig {

    //region SKUs

    //premium upgrade (non-consumable)
    public static final String SKU_SETTINGS = "sku_settings";
    //premium upgrade (non-consumable)
    public static final String SKU_ROVERS = "sku_rovers";
    //premium upgrade (non-consumable)
    public static final String SKU_COLORS = "sku_colors";
    //premium upgrade (non-consumable)
    public static final String SKU_COMPLETE = "sku_complete";
    //donation (consumable)
    public static final String SKU_COFFEE = "sku_coffee";

    public static final String[] SKU_ARRAY = new String[]{
            SKU_SETTINGS,
            SKU_ROVERS,
            SKU_COLORS,
            SKU_COMPLETE,
            SKU_COFFEE
    };


    //region Play SKUs

    //premium upgrade (non-consumable)
    public static final String PLAY_SETTINGS = "more_settings";
    //premium upgrade (non-consumable)
    public static final String PLAY_ROVERS = "more_rovers";
    //premium upgrade (non-consumable)
    public static final String PLAY_COLORS = "more_colors";
    //premium upgrade (non-consumable)
    public static final String PLAY_COMPLETE = "complete_package";
    //donation (consumable)
    public static final String PLAY_COFFEE = "buy_us_coffee";


    //endregion Play SKUs

    //endregion  SKUs

    //region Stores Keys

    //Google Play
    public static final String GOOGLE_PLAY_KEY
            = "YOUR_PUBLIC_GOOGLE_PLAY_KEY";

    //endregion Stores Keys

    public static Map<String, String> STORE_KEYS_MAP;

    public static void init(){
        STORE_KEYS_MAP = new HashMap<>();
        STORE_KEYS_MAP.put(OpenIabHelper.NAME_GOOGLE, InAppConfig.GOOGLE_PLAY_KEY);

        SkuManager.getInstance()
                .mapSku(SKU_COFFEE, OpenIabHelper.NAME_GOOGLE, PLAY_COFFEE)
                .mapSku(SKU_COLORS, OpenIabHelper.NAME_GOOGLE, PLAY_COLORS)
                .mapSku(SKU_COMPLETE, OpenIabHelper.NAME_GOOGLE, PLAY_COMPLETE)
                .mapSku(SKU_ROVERS, OpenIabHelper.NAME_GOOGLE, PLAY_ROVERS)
                .mapSku(SKU_SETTINGS, OpenIabHelper.NAME_GOOGLE, PLAY_SETTINGS);

    }

    private InAppConfig() {
    }

}
