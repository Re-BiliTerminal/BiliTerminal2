package com.huanli233.biliterminal2.ui.utils.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pools
import androidx.core.view.LayoutInflaterCompat
import com.huanli233.biliterminal2.data.setting.LocalData
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

typealias OnInflateFinishedListener = (view: View, resId: Int, parent: ViewGroup?) -> Unit

class AsyncLayoutInflaterX(context: Context) {
    companion object {
        private const val TAG = "AsyncLayoutInflater"
    }

    private val mRequestPool = Pools.SynchronizedPool<InflateRequest>(10)

    private val mInflater: LayoutInflater = BasicInflater(context)
    private val mHandler = Handler(Looper.myLooper()!!, HandlerCallback())
    private val mDispatcher = Dispatcher()

    @UiThread
    fun inflate(
        @LayoutRes resid: Int,
        parent: ViewGroup? = null,
        callback: OnInflateFinishedListener
    ) {
        if (LocalData.settings.preferences.asyncInflateEnabled) {
            val request = obtainRequest()
            request.inflater = this
            request.resid = resid
            request.parent = parent
            request.callback = callback
            mDispatcher.enqueue(request)
        } else {
            val request = InflateRequest()
            request.inflater = this
            request.resid = resid
            request.parent = parent
            request.callback = callback
            Message.obtain(mHandler, 0, request).sendToTarget()
        }
    }

    private inner class HandlerCallback : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            val request = msg.obj as InflateRequest
            if (request.view == null) {
                request.view = mInflater.inflate(request.resid, request.parent, false)
            }
            request.callback?.invoke(request.view!!, request.resid, request.parent)
            releaseRequest(request)
            return true
        }
    }

    class InflateRequest {
        var inflater: AsyncLayoutInflaterX? = null
        var parent: ViewGroup? = null
        var resid: Int = 0
        var view: View? = null
        var callback: OnInflateFinishedListener? = null
    }

    private class Dispatcher {
        companion object {
            private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
            private val CORE_POOL_SIZE = maxOf(2, minOf(CPU_COUNT - 1, 4))
            private val MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1
            private const val KEEP_ALIVE_SECONDS = 30

            private val sThreadFactory = object : ThreadFactory {
                private val mCount = AtomicInteger(1)

                override fun newThread(r: Runnable): Thread {
                    return Thread(r, "$TAG #${mCount.getAndIncrement()}")
                }
            }

            // LinkedBlockingQueue 默认构造器，队列容量是Integer.MAX_VALUE
            private val sPoolWorkQueue: BlockingQueue<Runnable> = LinkedBlockingQueue()

            val THREAD_POOL_EXECUTOR: ThreadPoolExecutor

            init {
                Log.i(TAG, "static initializer: CPU_COUNT = $CPU_COUNT CORE_POOL_SIZE = $CORE_POOL_SIZE MAXIMUM_POOL_SIZE = $MAXIMUM_POOL_SIZE")
                val threadPoolExecutor = ThreadPoolExecutor(
                    CORE_POOL_SIZE,
                    MAXIMUM_POOL_SIZE,
                    KEEP_ALIVE_SECONDS.toLong(),
                    TimeUnit.SECONDS,
                    sPoolWorkQueue,
                    sThreadFactory
                )
                threadPoolExecutor.allowCoreThreadTimeOut(true)
                THREAD_POOL_EXECUTOR = threadPoolExecutor
            }
        }

        fun enqueue(request: InflateRequest) {
            THREAD_POOL_EXECUTOR.execute(InflateRunnable(request))
        }
    }

    private class BasicInflater(context: Context) : LayoutInflater(context) {
        companion object {
            private val sClassPrefixList = arrayOf(
                "android.widget.",
                "android.webkit.",
                "android.app."
            )
        }

        init {
            if (context is AppCompatActivity) {
                // 手动setFactory2，兼容AppCompatTextView等控件
                val appCompatDelegate = context.delegate
                if (appCompatDelegate is Factory2) {
                    LayoutInflaterCompat.setFactory2(this, appCompatDelegate)
                }
            }
        }

        override fun cloneInContext(newContext: Context): LayoutInflater {
            return BasicInflater(newContext)
        }

        override fun onCreateView(name: String, attrs: AttributeSet): View {
            for (prefix in sClassPrefixList) {
                try {
                    val view = createView(name, prefix, attrs)
                    if (view != null) {
                        return view
                    }
                } catch (e: ClassNotFoundException) {
                    // In this case we want to let the base class take a crack at it.
                }
            }
            return super.onCreateView(name, attrs)
        }
    }

    private class InflateRunnable(private val request: InflateRequest) : Runnable {
        private var isRunning = false

        override fun run() {
            isRunning = true
            try {
                request.view = request.inflater?.mInflater?.inflate(
                    request.resid,
                    request.parent,
                    false
                )
            } catch (ex: Throwable) {
                // Probably a Looper failure, retry on the UI thread
                Log.w(TAG, "Failed to inflate resource in the background! Retrying on the UI thread", ex)
            }
            Message.obtain(request.inflater?.mHandler, 0, request).sendToTarget()
        }

        fun isRunning(): Boolean {
            return isRunning
        }
    }

    private fun obtainRequest(): InflateRequest {
        return mRequestPool.acquire() ?: InflateRequest()
    }

    private fun releaseRequest(obj: InflateRequest) {
        obj.callback = null
        obj.inflater = null
        obj.parent = null
        obj.resid = 0
        obj.view = null
        mRequestPool.release(obj)
    }

    fun cancel() {
        mHandler.removeCallbacksAndMessages(null)
    }
}