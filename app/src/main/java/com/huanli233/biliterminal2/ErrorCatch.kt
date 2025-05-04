package com.huanli233.biliterminal2

import android.content.Context
import android.content.Intent
import android.os.Process
import com.huanli233.biliterminal2.ui.activity.CatchActivity
import java.lang.ref.WeakReference

class ErrorCatch : Thread.UncaughtExceptionHandler {
    private var context: Context? = null

    fun install(context: Context) {
        this.context = context
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            context?.startActivity(
                Intent(context, CatchActivity::class.java).apply {
                    putExtra("stack", throwable)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        throwable.printStackTrace()
        Process.killProcess(Process.myPid())
    }

    companion object {
        private var _instance: WeakReference<ErrorCatch> = WeakReference(null)
        val instance: ErrorCatch
            get() = _instance.get() ?: ErrorCatch().also {
                _instance = WeakReference(it)
            }
    }
}
