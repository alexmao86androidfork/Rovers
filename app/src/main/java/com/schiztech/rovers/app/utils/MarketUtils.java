package com.schiztech.rovers.app.utils;

/**
 * Created by schiz_000 on 3/1/2015.
 */
public class MarketUtils {
    static final String PlayStorePackageWebLink = "http://play.google.com/store/apps/details?id=%s";
    static final String PlayStorePackageAndroidLink ="market://details?id=%s";

    static final String PlayStorePublisherWebLink = "http://play.google.com/store/apps/details?id=%s";
    static final String PlayStorePublisherAndroidLink ="market://search?q=pub:%s";

    static final String PlayStoreSearchWebLink = "http://play.google.com/store/search?q=%s";
    static final String PlayStoreSearchAndroidLink ="market://search?q=%s&c=apps";



    public static String getPackagePlayStoreLink(String packageName, boolean isWeb){
        return String.format(isWeb ? PlayStorePackageWebLink : PlayStorePackageAndroidLink, packageName);
    }

    public static String getPublisherPlayStoreLink(String publisher, boolean isWeb){
        return String.format(isWeb ? PlayStorePublisherWebLink : PlayStorePublisherAndroidLink, publisher);
    }

    public static String getSearchPlayStoreLink(String query, boolean isWeb){
        return String.format(isWeb ? PlayStoreSearchWebLink : PlayStoreSearchAndroidLink, query);
    }

}
