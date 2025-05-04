package com.huanli233.biliterminal2.utils

import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.EmptyCoroutineContext

object ThreadManager {
    private val MAIN_THREAD_HANDLER = Handler(Looper.getMainLooper())
    private var COROUTINE_SCOPE: CoroutineScope? = null
    private var THREAD_POOL: AtomicReference<ExecutorService?> = AtomicReference()

    private val threadPoolInstance: ExecutorService?
        get() {
            val bestThreadPoolSize =
                Runtime.getRuntime().availableProcessors()
            while (THREAD_POOL.get() == null) {
                THREAD_POOL.compareAndSet(
                    null, ThreadPoolExecutor(
                        bestThreadPoolSize / 2,
                        bestThreadPoolSize * 2,
                        60,
                        TimeUnit.SECONDS,
                        ArrayBlockingQueue(20)
                    )
                )
            }
            return THREAD_POOL.get()
        }

    init {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            COROUTINE_SCOPE = null
            THREAD_POOL = AtomicReference()
        } else {
            COROUTINE_SCOPE = CoroutineScope(Dispatchers.IO)
        }
    }

    /**
     * 在后台运行, 用于网络请求等耗时操作
     *
     * @param runnable 要运行的任务
     */
    @JvmStatic
    fun run(runnable: Runnable) {
        kotlin.runCatching {
            COROUTINE_SCOPE?.launch(
                EmptyCoroutineContext,
                CoroutineStart.DEFAULT
            ) {
                runnable.run()
            } ?: if (threadPoolInstance != null) {
                threadPoolInstance!!.submit(runnable)
            } else {
                Thread(runnable).start()
            }
        }.onFailure {
            Thread(runnable).start()
        }
    }

    /**
     * 在主线程运行, 用于更新UI, 例如Toast, Snackbar等
     *
     * @param runnable 要运行的任务
     */
    @JvmStatic
    fun runOnUiThread(runnable: Runnable) {
        MAIN_THREAD_HANDLER.post(runnable)
    }

    @JvmStatic
    fun runOnUIThreadAfter(time: Long, unit: TimeUnit?, runnable: Runnable) {
        val millis = TimeUnit.MILLISECONDS.convert(time, unit)
        MAIN_THREAD_HANDLER.postDelayed(runnable, millis)
    }

    @JvmStatic
    fun runOnUIThreadAfter(time: Long, runnable: Runnable) {
        MAIN_THREAD_HANDLER.postDelayed(runnable, time)
    }
}

fun runOnBackground(task: () -> Unit) =
    ThreadManager.run(task)

fun runOnUi(task: () -> Unit) =
    ThreadManager.runOnUiThread(task)