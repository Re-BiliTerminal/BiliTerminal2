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

-keep class com.hjq.gson.factory.** {*;}
-keep class com.huanli233.biliwebapi.bean.** {*;}
-keep class * extends com.google.protobuf.GeneratedMessageLite {*;}

-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keep class com.geetest.sdk.** {*;}
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

-keepattributes SourceFile,LineNumberTable

-keep class master.flame.danmaku.danmaku.** { *; }
-keep class master.flame.danmaku.** { *; }
-keep class tv.danmaku.ijk.media.player.** { *; }

-dontwarn androidx.navigation.NavType$Companion

-keepclassmembers class * extends android.view.View {
    <init>(android.content.Context);
    <init>(android.content.Context, android.util.AttributeSet);
}
-keepclassmembers class * extends android.view.ViewGroup$LayoutParams {
    <init>(int, int);
}
-keep interface tv.danmaku.ijk.media.player.IMediaPlayer$OnPreparedListener { *; }
-keep interface tv.danmaku.ijk.media.player.IMediaPlayer$OnCompletionListener { *; }
-keep interface tv.danmaku.ijk.media.player.IMediaPlayer$OnErrorListener { *; }
-keep interface tv.danmaku.ijk.media.player.IMediaPlayer$OnInfoListener { *; }
-keep interface tv.danmaku.ijk.media.player.IMediaPlayer$OnVideoSizeChangedListener { *; }
-keep interface tv.danmaku.ijk.media.player.IMediaPlayer$OnSeekCompleteListener { *; }
-keep interface tv.danmaku.ijk.media.player.IMediaPlayer$OnBufferingUpdateListener { *; }

-keepclassmembers interface tv.danmaku.ijk.media.player.IMediaPlayer {
    public *;
}

-keepclassmembers class * {
    *** lambda$*(...);
}

-keepclassmembers class * {
    public void onPrepared(tv.danmaku.ijk.media.player.IMediaPlayer);
    public void onCompletion(tv.danmaku.ijk.media.player.IMediaPlayer);
    public boolean onError(tv.danmaku.ijk.media.player.IMediaPlayer, int, int);
    public boolean onInfo(tv.danmaku.ijk.media.player.IMediaPlayer, int, int);
    public void onVideoSizeChanged(tv.danmaku.ijk.media.player.IMediaPlayer, int, int, int, int);
    public void onSeekComplete(tv.danmaku.ijk.media.player.IMediaPlayer);
    public void onBufferingUpdate(tv.danmaku.ijk.media.player.IMediaPlayer, int);
}

-keepclassmembers class * {
    tv.danmaku.ijk.media.player.IMediaPlayer *$lambda$*$*(...);
    *** *$lambda$*$*(tv.danmaku.ijk.media.player.IMediaPlayer);
    *** *$lambda$*$*(tv.danmaku.ijk.media.player.IMediaPlayer, int, int);
    *** *$lambda$*$*(tv.danmaku.ijk.media.player.IMediaPlayer, int, int, int, int);
}

-keepclassmembers class com.huanli233.bilizepam.ui.screens.player.PlayerViewModel {
    <methods>;
}
