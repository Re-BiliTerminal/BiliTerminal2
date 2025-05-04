package com.huanli233.biliterminal2.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import com.huanli233.biliterminal2.applicationContext
import com.huanli233.biliterminal2.data.UserPreferences
import com.huanli233.biliterminal2.event.SnackEvent
import com.huanli233.biliterminal2.utils.Preferences.getBoolean
import com.huanli233.biliterminal2.utils.ThreadManager.runOnUiThread
import com.huanli233.biliterminal2.utils.extensions.dp2px
import org.greenrobot.eventbus.EventBus

object MsgUtil {
    private var toast: Toast? = null

    @JvmStatic
    fun showMsg(str: String) {
        if (UserPreferences.snackbarEnabled.get()) {
            runOnUiThread(Runnable { EventBus.getDefault().postSticky(SnackEvent(str)) })
        } else {
            toast(str)
        }
    }

    @JvmStatic
    fun showMsgLong(str: String) {
        if (UserPreferences.snackbarEnabled.get()) {
            runOnUiThread(Runnable { EventBus.getDefault().postSticky(SnackEvent(str)) })
        } else {
            toastLong(str)
        }
    }

    fun toast(str: String) {
        runOnUiThread(Runnable { toastInternal(str, applicationContext) })
    }

    fun toastLong(str: String) {
        runOnUiThread(Runnable { toastLongInternal(str, applicationContext) })
    }

    private fun toastInternal(str: String, context: Context?) {
        if (toast != null) toast!!.cancel()
        toast = Toast.makeText(context, str, Toast.LENGTH_SHORT)
        toast!!.show()
    }

    private fun toastLongInternal(str: String, context: Context?) {
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
            createSnack(view, snackEvent.message.toString(), (endTime - currentTime).toInt())
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
            val actionView = contentLayout.actionView
            // actionView.setTextSize(ToolsUtil.sp2px(13));
            actionView.setMinWidth(dp2px(30f))
            actionView.setMinimumWidth(dp2px(30f))
            actionView.setMaxWidth(dp2px(48f))
            actionView.setPadding(0, 0, dp2px(4f), 0)
        }

        val msgView = contentLayout.messageView
        msgView.setMaxLines(16)
        msgView.textSize = 13f
        msgView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        msgView.setPadding(0, 0, dp2px(4f), 0)

        return snackbar
    }

    class Action(var text: String?, var onClickListener: View.OnClickListener?)
}
