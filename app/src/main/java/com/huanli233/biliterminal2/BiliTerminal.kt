package com.huanli233.biliterminal2

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.multidex.MultiDex
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.AndroidPrinter
import com.huanli233.biliterminal2.activity.base.InstanceActivity
import com.huanli233.biliterminal2.activity.user.info.UserInfoActivity
import com.huanli233.biliterminal2.util.SharedPreferencesUtil
import com.huanli233.biliterminal2.util.TerminalContext
import java.lang.ref.WeakReference

class BiliTerminal : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        XLog.init(
            if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.ERROR,
            AndroidPrinter()
        )
        if (context == null) {
            SharedPreferencesUtil.sharedPreferences = getSharedPreferences("default", MODE_PRIVATE)
            context = getFitDisplayContext(this)
            val errorCatch = ErrorCatch.getInstance()
            errorCatch.init(context)
        }
    }

    companion object {
        @JvmField
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null

        val contextNotNull: Context
            get() = context!!

        @JvmField
        var DPI_FORCE_CHANGE: Boolean = false

        private var instance = WeakReference<InstanceActivity?>(null)

        @JvmStatic
        fun setInstance(instanceActivity: InstanceActivity?) {
            instance = WeakReference(instanceActivity)
        }

        @JvmStatic
        val instanceActivityOnTop: InstanceActivity?
            get() = instance.get()

        /**
         * 重写attachBaseContext方法，用于调整应用内dpi
         *
         * @param old The origin context.
         */
        @JvmStatic
        fun getFitDisplayContext(old: Context): Context? {
            val dpiTimes = SharedPreferencesUtil.getFloat("dpi", 1.0f)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return old
            if (!DPI_FORCE_CHANGE && dpiTimes == 1.0f) return old
            try {
                val displayMetrics = old.resources.displayMetrics
                val configuration = old.resources.configuration
                configuration.densityDpi = (displayMetrics.densityDpi * dpiTimes).toInt()
                return old.createConfigurationContext(configuration)
            } catch (e: Exception) {
                //MsgUtil.err(e,old);
                return old
            }
        }

        @get:Throws(PackageManager.NameNotFoundException::class)
        @Suppress("DEPRECATION")
        val version: Int
            get() = context!!.packageManager
                .getPackageInfo(context!!.packageName, 0).versionCode

        @JvmStatic
        fun jumpToVideo(context: Context, aid: Long) {
            TerminalContext.getInstance().enterVideoDetailPage(context, aid)
        }

        @JvmStatic
        fun jumpToVideo(context: Context, bvid: String?) {
            TerminalContext.getInstance().enterVideoDetailPage(context, bvid)
        }

        @JvmStatic
        fun jumpToArticle(context: Context, cvid: Long) {
            TerminalContext.getInstance().enterArticleDetailPage(context, cvid)
        }

        @JvmStatic
        fun jumpToUser(context: Context, mid: Long) {
            val intent = Intent()
            intent.setClass(context, UserInfoActivity::class.java)
            intent.putExtra("mid", mid)
            context.startActivity(intent)
        }
    }
}
