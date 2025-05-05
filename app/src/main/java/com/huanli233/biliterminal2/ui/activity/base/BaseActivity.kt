package com.huanli233.biliterminal2.ui.activity.base

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ListView
import android.widget.ScrollView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.ViewConfigurationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.BiliTerminal
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.UserPreferences
import com.huanli233.biliterminal2.event.SnackEvent
import com.huanli233.biliterminal2.ui.widget.TopBar
import com.huanli233.biliterminal2.ui.widget.recyclerView.CustomGridManager
import com.huanli233.biliterminal2.ui.widget.recyclerView.CustomLinearManager
import com.huanli233.biliterminal2.utils.view.AsyncLayoutInflaterX
import com.huanli233.biliterminal2.utils.MsgUtil
import com.huanli233.biliterminal2.utils.Preferences
import com.huanli233.biliterminal2.utils.extensions.crossFadeSetText
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.roundToInt

open class BaseActivity : AppCompatActivity() {
    var windowWidth: Int = 0
    @JvmField
    var windowHeight: Int = 0
    var oldContext: Context? = null
    var forceSingleColumn: Boolean = false

    private var topBar: TopBar? = null
    private var topBarChanged = false

    override fun attachBaseContext(newBase: Context) {
        oldContext = newBase
        super.attachBaseContext(BiliTerminal.getFitDisplayContext(newBase))
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = if (Preferences.getBoolean("ui_landscape", false))
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        else
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        val paddingHPercent: Int = UserPreferences.uiPaddingHorizontal.get()
        val paddingVPercent: Int = UserPreferences.uiPaddingVertical.get()

        val rootView: View = this.window.decorView.rootView
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val windowManager: WindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val display: Display = windowManager.defaultDisplay
        val metrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= 17) display.getRealMetrics(metrics)
        else display.getMetrics(metrics)

        val scrW: Int = metrics.widthPixels
        val scrH: Int = metrics.heightPixels
        if (paddingHPercent != 0 || paddingVPercent != 0) {
            val paddingH: Int = scrW * paddingHPercent / 100
            val paddingV: Int = scrH * paddingVPercent / 100
            windowWidth = scrW - paddingH
            windowHeight = scrH - paddingV
            rootView.setPadding(paddingH, paddingV, paddingH, paddingV)
        } else {
            windowWidth = scrW
            windowHeight = scrH
        }

        var density: Int
        if ((UserPreferences.density.get().also { density = it }) >= 72) {
            setDensity(density)
        }
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (!UserPreferences.backDisabled.get()) {
            super.onBackPressed()
        }
    }

    open var pageName: String? = null
        set(value) {
            val oldValue = field
            field = value
            setTopbarTitle(value, oldValue != null)
        }

    private fun setTopbarTitle(
        name: String?,
        animation: Boolean = false
    ) {
        val textView = topBar?.titleTextView ?: return
        name?.let {
            if (animation) {
                textView.crossFadeSetText(it)
            } else {
                textView.text = it
            }
        }
    }

    fun setupTopbar() {
        val view = topBar ?: return
        if (Build.VERSION.SDK_INT > 17 && view.hasOnClickListeners()) return
        view.setOnClickListener {
            onTopbarClicked()
        }
    }

    open fun onTopbarClicked() {
        if (!isDestroyed) {
            finish()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (Build.VERSION.SDK_INT < 17 || !isDestroyed) {
                finish()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private var eventBusInit: Boolean = false

    override fun onStart() {
        super.onStart()
        if (eventBusEnabled() && !eventBusInit) {
            EventBus.getDefault().register(this)
            eventBusInit = true
        }
    }

    override fun onResume() {
        super.onResume()
        if (topBar == null) {
            topBar = findViewById(R.id.top_bar)
            setupTopbar()
            setTopbarTitle(pageName)
        }
        if (eventBusEnabled()) {
            var snackEvent: SnackEvent
            EventBus.getDefault().getStickyEvent(SnackEvent::class.java)?.also { snackEvent = it }?.let {
                onEvent(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (eventBusInit) {
            EventBus.getDefault().unregister(this)
            eventBusInit = false
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onEvent(event: SnackEvent) {
        if (isFinishing) return
        MsgUtil.processSnackEvent(event, window.decorView.rootView)
    }

    protected open fun eventBusEnabled(): Boolean {
        return UserPreferences.snackbarEnabled.get()
    }

    @Suppress("DEPRECATION")
    fun setDensity(targetDensityDpi: Int) {
        if (Build.VERSION.SDK_INT < 17) return
        val resources: Resources = resources

        if (resources.configuration.densityDpi == targetDensityDpi) return

        val configuration: Configuration = resources.configuration
        configuration.densityDpi = targetDensityDpi
        configuration.fontScale = 1f
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    protected fun asyncInflate(id: Int, callBack: InflateCallBack) {
        setContentView(R.layout.activity_loading)
        AsyncLayoutInflaterX(this).inflate(
            id,
            null
        ) { view: View?, layoutId: Int, parent: ViewGroup? ->
            setContentView(view)
            callBack.finishInflate(view, layoutId)
        }
    }

    override fun isDestroyed(): Boolean {
        return lifecycle.currentState == Lifecycle.State.DESTROYED
    }

    protected interface InflateCallBack {
        fun finishInflate(view: View?, id: Int)
    }

    val layoutManager: RecyclerView.LayoutManager
        get() = if (Preferences.getBoolean(
                "ui_landscape",
                false
            ) && !forceSingleColumn
        )
            CustomGridManager(this, 3)
        else
            CustomLinearManager(this)

    fun setForceSingleColumn() {
        forceSingleColumn = true
    }

    override fun onContentChanged() {
        super.onContentChanged()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val rootView: ViewGroup = this.window.decorView as ViewGroup
            setRotaryScroll(rootView)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setRotaryScroll(view: View) {
        if (view is ViewGroup) {
            val vp: ViewGroup = view
            try {
                for (i in 0 until vp.childCount) {
                    val viewChild: View = vp.getChildAt(i)

                    val multiple: Float = when (viewChild) {
                        is ListView -> Preferences.getFloat("ui_rotatory_list", 0f)
                        is RecyclerView -> Preferences.getFloat("ui_rotatory_recycler", 0f)
                        is ScrollView, is NestedScrollView -> Preferences.getFloat("ui_rotatory_scroll", 0f)
                        else -> {
                            setRotaryScroll(viewChild)
                            return
                        }
                    }
                    if (multiple <= 0) return

                    val finalMultiple: Float = multiple
                    viewChild.setOnGenericMotionListener { v: View?, ev: MotionEvent ->
                        if (ev.action == MotionEvent.ACTION_SCROLL && ev.source == InputDevice.SOURCE_ROTARY_ENCODER) {
                            val delta: Float = (-ev.getAxisValue(MotionEvent.AXIS_SCROLL)
                                    * ViewConfigurationCompat.getScaledVerticalScrollFactor(
                                ViewConfiguration.get(applicationContext),
                                applicationContext
                            ) * 2)

                            if (viewChild is ScrollView) viewChild.smoothScrollBy(
                                0,
                                (delta * finalMultiple).roundToInt()
                            )
                            else if (viewChild is NestedScrollView) viewChild.smoothScrollBy(
                                0,
                                (delta * finalMultiple).roundToInt()
                            )
                            else if (viewChild is RecyclerView) viewChild.smoothScrollBy(
                                0,
                                (delta * finalMultiple).roundToInt()
                            )
                            else (viewChild as ListView).smoothScrollBy(
                                0,
                                (delta * finalMultiple).roundToInt()
                            )

                            viewChild.requestFocus()

                            return@setOnGenericMotionListener true
                        }
                        false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val className: String
        get() {
            return javaClass.simpleName
        }
}
