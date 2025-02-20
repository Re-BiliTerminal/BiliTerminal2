package com.huanli233.biliterminal2.util

import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.util.Consumer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @author silent碎月
 */
object CenterThreadPool {
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
     * 在后台运行, 用于网络请求等耗时操作, 有返回值,
     * 在fragment, activity等位置使用LiveData.observe()获取返回值, 会自动切到主线程,不需要再runOnUiThread().
     *
     * @param supplier 要运行的任务
     * @param <T>      返回值类型
     * @return LiveData包装的返回值
    </T> */
    @JvmStatic
    fun <T> supplyAsyncWithLiveData(supplier: Callable<T>): LiveData<Result<T>> {
        val retval = MutableLiveData<Result<T>>()
        run {
            try {
                val res = supplier.call()
                retval.postValue(Result.success(res))
            } catch (e: Exception) {
                retval.postValue(Result.failure(e))
                MsgUtil.err(e)
            }
        }
        return retval
    }

    /**
     * 在后台运行， 有返回值
     * 使用 CenterThreadPool.observe方法对返回值进行观察
     *
     * @param supplier 一个带返回值的lambda表达式或Supplier的实现类
     * @param <T>      返回值类型
     * @return 返回一个可供CenterThreadPool观察的Future对象
    </T> */
    @JvmStatic
    fun <T> supplyAsyncWithFuture(supplier: Callable<T>?): Future<T> {
        val ftask = FutureTask(supplier)
        run(ftask)
        return ftask
    }

    /**
     * 对Deferred 对象进行观察， 无需切换线程， 自动在ui线程进行观察
     *
     * @param deferred 一个将要在未来返回一个 T 类型对象的对象
     * @param consumer 对T进行观察的lambda表达式或者类
     * @param <T>      要观察的类型
    </T> */
    @JvmStatic
    fun <T> observe(deferred: Future<T>, consumer: Consumer<T>) {
        run {
            try {
                val value = deferred.get()
                runOnUiThread { consumer.accept(value) }
            } catch (ignored: Throwable) {
            }
        }
    }


    @JvmStatic
    fun <T> observe(future: Future<T>, consumer: Consumer<T>, onFailure: Consumer<Throwable?>) {
        run {
            try {
                val value = future.get()
                runOnUiThread { consumer.accept(value) }
            } catch (e: Exception) {
                onFailure.accept(e)
            }
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