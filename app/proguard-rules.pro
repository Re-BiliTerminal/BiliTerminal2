# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keeppackagenames org.jsoup.nodes
-keep class tv.danmaku.ijk.media.** {*;}
-keep class com.netease.hearttouch.brotlij.** {*;}

-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-dontwarn com.geetest.sdk.**
-keep class com.geetest.sdk.**{*;}
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

-keepattributes SourceFile,LineNumberTable

-dontwarn master.flame.danmaku.controller.DrawHandler$Callback
-dontwarn master.flame.danmaku.controller.IDanmakuView
-dontwarn master.flame.danmaku.danmaku.loader.ILoader
-dontwarn master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
-dontwarn master.flame.danmaku.danmaku.model.AbsDisplayer
-dontwarn master.flame.danmaku.danmaku.model.BaseDanmaku
-dontwarn master.flame.danmaku.danmaku.model.DanmakuTimer
-dontwarn master.flame.danmaku.danmaku.model.IDanmakus
-dontwarn master.flame.danmaku.danmaku.model.android.DanmakuContext
-dontwarn master.flame.danmaku.danmaku.model.android.DanmakuFactory
-dontwarn master.flame.danmaku.danmaku.model.android.Danmakus
-dontwarn master.flame.danmaku.danmaku.parser.BaseDanmakuParser
-dontwarn master.flame.danmaku.danmaku.parser.IDataSource
-dontwarn master.flame.danmaku.danmaku.parser.android.BiliDanmukuParser
-dontwarn androidx.navigation.NavType$Companion