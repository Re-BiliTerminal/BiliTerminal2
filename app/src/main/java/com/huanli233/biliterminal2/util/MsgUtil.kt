package com.huanli233.biliterminal2.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.SQLException
import android.graphics.Color
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.elvishew.xlog.XLog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.DialogActivity
import com.huanli233.biliterminal2.activity.ShowTextActivity
import com.huanli233.biliterminal2.contextNotNull
import com.huanli233.biliterminal2.event.SnackEvent
import com.huanli233.biliterminal2.util.Preferences.getBoolean
import com.huanli233.biliterminal2.util.Preferences.getLong
import com.huanli233.biliterminal2.util.ThreadManager.runOnUiThread
import com.huanli233.biliterminal2.util.Utils.dp2px
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer

object MsgUtil {
    private var toast: Toast? = null

    @JvmStatic
    fun showMsg(str: String?) {
        if (getBoolean(Preferences.SNACKBAR_ENABLE, true)) {
            runOnUiThread(Runnable { EventBus.getDefault().postSticky(SnackEvent(str)) })
        } else {
            toast(str)
        }
    }

    @JvmStatic
    fun showMsgLong(str: String?) {
        if (getBoolean(Preferences.SNACKBAR_ENABLE, true)) {
            runOnUiThread(Runnable { EventBus.getDefault().postSticky(SnackEvent(str)) })
        } else {
            toastLong(str)
        }
    }

    fun toast(str: String?) {
        runOnUiThread(Runnable { toastInternal(str, contextNotNull) })
    }

    fun toastLong(str: String?) {
        runOnUiThread(Runnable { toastLongInternal(str, contextNotNull) })
    }

    private fun toastInternal(str: String?, context: Context?) {
        if (toast != null) toast!!.cancel()
        toast = Toast.makeText(context, str, Toast.LENGTH_SHORT)
        toast!!.show()
    }

    private fun toastLongInternal(str: String?, context: Context?) {
        if (toast != null) toast!!.cancel()
        toast = Toast.makeText(context, str, Toast.LENGTH_LONG)
        toast!!.show()
    }

    fun processSnackEvent(snackEvent: SnackEvent, view: View) {
        val currentTime = System.currentTimeMillis()
        var duration = 2750
        if (snackEvent.duration > 0) {
            duration = snackEvent.duration
        } else if (snackEvent.duration == Snackbar.LENGTH_SHORT) {
            duration = 1950
        } else if (snackEvent.duration == Snackbar.LENGTH_INDEFINITE) {
            duration = Int.Companion.MAX_VALUE
        }
        val endTime = snackEvent.startTime + duration
        if (currentTime >= endTime) {
            EventBus.getDefault().removeStickyEvent(snackEvent)
        } else {
            createSnack(view, snackEvent.message, (endTime - currentTime).toInt())
                .show()
        }
    }

    fun snackText(view: View, text: CharSequence) {
        createSnack(view, text).show()
    }

    fun snackTextLong(view: View, text: CharSequence) {
        createSnack(view, text, Snackbar.LENGTH_LONG).show()
    }

    @JvmOverloads
    fun createSnack(
        view: View,
        text: CharSequence,
        duration: Int = Snackbar.LENGTH_SHORT
    ): Snackbar {
        return createSnack(view, text, duration, null)
    }

    @SuppressLint("ClickableViewAccessibility", "RestrictedApi")
    fun createSnack(view: View, text: CharSequence, duration: Int, action: Action?): Snackbar {
        val snackbar: Snackbar = Snackbar.make(view, text, duration)
        snackbar.setBackgroundTint(Color.argb(0x85, 0x80, 0x80, 0x80))
        snackbar.setTextColor(Color.rgb(0xeb, 0xe0, 0xe2))
        val snackBarView = snackbar.getView()
        snackBarView.setOnTouchListener(View.OnTouchListener { v: View?, event: MotionEvent? -> false })
        snackBarView.setPadding(dp2px(6f), 0, 0, 0)
        val contentLayout = ((snackBarView as FrameLayout).getChildAt(0) as SnackbarContentLayout)

        if (action != null) snackbar.setAction(action.text, action.onClickListener)
        else if (duration == Snackbar.LENGTH_INDEFINITE || duration >= 5000) {
            snackbar.setAction("x", (View.OnClickListener { view1: View? -> snackbar.dismiss() }))
            val actionView = contentLayout.getActionView()
            //actionView.setTextSize(ToolsUtil.sp2px(13));
            actionView.setMinWidth(dp2px(30f))
            actionView.setMinimumWidth(dp2px(30f))
            actionView.setMaxWidth(dp2px(48f))
            actionView.setPadding(0, 0, dp2px(4f), 0)
        }

        val msgView = contentLayout.getMessageView()
        msgView.setMaxLines(16)
        msgView.setTextSize(13f)
        msgView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        msgView.setPadding(0, 0, dp2px(4f), 0)

        return snackbar
    }

    @JvmStatic
    fun error(e: Throwable) {
        error(null, e)
    }

    @JvmStatic
    fun error(desc: String?, e: Throwable) {
        if (desc != null) XLog.e(desc)
        e.printStackTrace()

        val output = StringBuilder(desc ?: "")

        val errorStr = e.toString()

        if (e is IOException) output.append("网络错误(＃°Д°)")
        else if (e is JSONException) {
            if (getBoolean("dev_jsonerr_detailed", false)) {
                val writer: Writer = StringWriter()
                val printWriter = PrintWriter(writer)
                e.printStackTrace(printWriter)
                showText(desc + "数据解析错误", writer.toString())
                XLog.e(writer.toString())
                return
            } else if (getLong(Preferences.MID, 0) == 0L) {
                output.append("数据解析错误\n建议登陆后再尝试")
            } else if (errorStr.contains("-352") || errorStr.contains("22015") || errorStr.contains("65056")) output.append(
                contextNotNull.getString(
                    R.string.err_rejected
                )
            )
            else {
                output.append("数据解析错误：\n")
                output.append(errorStr.replace("org.json.JSONException:", ""))
            }
        } else if (e is IndexOutOfBoundsException) {
            if (getBoolean("dev_recyclererr_detailed", false)) {
                val writer: Writer = StringWriter()
                val printWriter = PrintWriter(writer)
                e.printStackTrace(printWriter)
                showText(desc + "Adapter错误", writer.toString())
                return
            } else {
                output.append("遇到Adapter错误：\n无需上报，除非你在某个界面经常遇到")
            }
        } else if (e is SQLException) output.append("数据库读写错误\n请清理空间或清除软件数据")
        else {
            output.append("错误：")
            output.append(errorStr)
        }

        showMsgLong(output.toString())
    }

    @JvmStatic
    fun showText(title: String?, text: String?) {
        val context = contextNotNull
        val testIntent = Intent()
            .setClass(context, ShowTextActivity::class.java)
            .putExtra("title", title)
            .putExtra("content", text)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(testIntent)
    }

    fun showDialog(title: String?, content: String?) {
        val context = contextNotNull
        val intent = Intent(context, DialogActivity::class.java)
        intent.putExtra("title", title)
        intent.putExtra("content", content)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @JvmStatic
    fun showDialog(title: String?, content: String?, waitTime: Int) {
        val context = contextNotNull
        val intent = Intent(context, DialogActivity::class.java)
        intent.putExtra("title", title)
        intent.putExtra("content", content)
        intent.putExtra("wait_time", waitTime)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    class Action(var text: String?, var onClickListener: View.OnClickListener?)
}
