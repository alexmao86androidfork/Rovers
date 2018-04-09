# TStore
-dontwarn android.webkit.WebView

# AMAZON
-dontwarn com.amazon.**
-keep class com.amazon.** {*;}
-keepattributes *Annotation*
-dontoptimize

# GOOGLE
-keep class com.android.vending.billing.**

# SAMSUNG
-keep class com.sec.android.iap.**

# Dont warn about non-existing jars of stores
-dontwarn org.onepf.oms.appstore.**