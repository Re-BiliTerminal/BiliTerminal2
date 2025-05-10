package com.huanli233.biliterminal2.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import com.huanli233.biliterminal2.applicationContext
import com.huanli233.biliterminal2.data.setting.LocalData
import com.huanli233.biliterminal2.event.SnackEvent
import com.huanli233.biliterminal2.ui.widget.snackbar.Snackbar
import com.huanli233.biliterminal2.ui.widget.snackbar.SnackbarContentLayout
import com.huanli233.biliterminal2.ui.widget.wearable.BoxInsetLayout
import com.huanli233.biliterminal2.utils.ThreadManager.runOnUiThread
import com.huanli233.biliterminal2.utils.extensions.dp2px
import org.greenrobot.eventbus.EventBus
import kotlin.math.roundToInt

object MsgUtil {
    private var toast: Toast? = null

    @JvmStatic
    fun showMsg(str: String) {
        if (LocalData.settings.uiSettings.snackbarEnabled) {
            runOnUiThread(Runnable { EventBus.getDefault().postSticky(SnackEvent(str)) })
        } else {
            toast(str)
        }
    }

    @JvmStatic
    fun showMsgLong(str: String) {
        if (LocalData.settings.uiSettings.snackbarEnabled) {
            runOnUiThread(Runnable { EventBus.getDefault().postSticky(SnackEvent(str)) })
        } else {
            toastLong(str)
        }
    }

    private fun toast(str: String) {
        runOnUi {
            toastInternal(str, applicationContext)
        }
    }

    private fun toastLong(str: String) {
        runOnUi {
            toastLongInternal(str, applicationContext)
        }
    }

    private fun toastInternal(str: String, context: Context?) {
        toast?.cancel()
        toast = Toast.makeText(context, str, Toast.LENGTH_SHORT).apply { show() }
    }

    private fun toastLongInternal(str: String, context: Context?) {
        toast?.cancel()
        toast = Toast.makeText(context, str, Toast.LENGTH_LONG).apply { show() }
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

    private fun snackText(view: View, text: CharSequence) {
        createSnack(view, text).show()
    }

    private fun snackTextLong(view: View, text: CharSequence) {
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

    private val screenWidth: Int by lazy { Resources.getSystem().displayMetrics.widthPixels }
    private val screenHeight: Int  by lazy { Resources.getSystem().displayMetrics.heightPixels }

    @SuppressLint("ClickableViewAccessibility", "RestrictedApi")
    fun createSnack(view: View, text: CharSequence, duration: Int, action: Action?): Snackbar {
        val snackbar: Snackbar = Snackbar.make(view, text, duration)
        snackbar.setBackgroundTint(Color.argb(0x85, 0x80, 0x80, 0x80))
        snackbar.setTextColor(Color.rgb(0xeb, 0xe0, 0xe2))
        val snackBarView = snackbar.view
        snackBarView.setOnTouchListener(View.OnTouchListener { v: View?, event: MotionEvent? -> false })
        snackBarView.setPadding(dp2px(6f), 0, 0, 0)
        val snackBarLayout = (snackBarView as Snackbar.SnackbarLayout)
        val contentLayout = (snackBarLayout.getChildAt(0) as SnackbarContentLayout)

        if (LocalData.settings.uiSettings.roundMode) {
            snackBarLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                val inset = BoxInsetLayout.calculateInset(screenWidth, screenHeight)
                updateMargins(
                    left = (inset * 0.85).roundToInt(),
                    right = (inset * 0.85).roundToInt(),
                    bottom = inset
                )
            }
        }

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
