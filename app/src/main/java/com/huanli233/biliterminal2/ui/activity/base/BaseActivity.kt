package com.huanli233.biliterminal2.ui.activity.base

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Display
import android.view.InputDevice
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
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.setting.DataStore
import com.huanli233.biliterminal2.event.SnackEvent
import com.huanli233.biliterminal2.ui.utils.crossFadeSetText
import com.huanli233.biliterminal2.ui.widget.components.TopBar
import com.huanli233.biliterminal2.utils.MsgUtil
import com.huanli233.biliterminal2.utils.Preferences
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.roundToInt

open class BaseActivity : AppCompatActivity() {
    var windowWidth: Int = 0
    var windowHeight: Int = 0
    private lateinit var _originalContext: Context
    private var _lastOriginalViewContext: Context? = null
    private var _configurationChanged = false
    val originalViewContext: Context
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            _lastOriginalViewContext?.let {
                if (!_configurationChanged) {
                    it
                } else {
                    null
                }
            } ?: let {
                _configurationChanged = false
                overrideToSystemConfiguration()
            }
        } else {
            this
        }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun overrideToSystemConfiguration(): Context {
        return object : ContextWrapper(this) {
            val mResources = baseContext.resources.run {
                val system = Resources.getSystem()
                @Suppress("DEPRECATION") Resources(
                    assets,
                    DisplayMetrics().apply {
                        setTo(displayMetrics)
                        density = system.displayMetrics.density
                        densityDpi = system.displayMetrics.densityDpi
                    },
                    Configuration(configuration).apply {
                        densityDpi = system.configuration.densityDpi
                    }
                )
            }

            override fun getResources(): Resources? {
                return mResources
            }
        }.also {
            _lastOriginalViewContext = it
        }
    }

    var topBar: TopBar? = null

    override fun attachBaseContext(context: Context) {
        val newContext = overrideConfiguration(context)
        super.attachBaseContext(newContext)
        _originalContext = context
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        _configurationChanged = true
    }

    fun overrideConfiguration(baseContext: Context): Context {
        val dpiTimes = DataStore.appSettings.uiScale
        val density = DataStore.appSettings.density
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return baseContext
        return runCatching {
            val configuration = baseContext.resources.configuration
            if (density >= 72) {
                configuration.densityDpi = density
                configuration.fontScale = 1.0f
                baseContext.createConfigurationContext(configuration)
            } else if (dpiTimes in 0.25..5.0) {
                val displayMetrics = baseContext.resources.displayMetrics
                configuration.densityDpi = (displayMetrics.densityDpi * dpiTimes).toInt()
                baseContext.createConfigurationContext(configuration)
            } else {
                baseContext
            }
        }.getOrNull() ?: baseContext
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        val paddingHPercent: Int = DataStore.appSettings.uiPaddingHorizontal
        val paddingVPercent: Int = DataStore.appSettings.uiPaddingVertical

        val rootView: View = this.window.decorView.rootView
        val windowManager: WindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val display: Display = windowManager.defaultDisplay
        val metrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= 17) display.getRealMetrics(metrics)
        else display.getMetrics(metrics)

        val screenWidth: Int = metrics.widthPixels
        val screenHeight: Int = metrics.heightPixels
        if (paddingHPercent != 0 || paddingVPercent != 0) {
            val paddingHorizontal: Int = screenWidth * paddingHPercent / 100
            val paddingTop: Int = screenHeight * paddingVPercent / 100
            val paddingBottom = if (DataStore.appSettings.roundMode) {
                (paddingTop + screenHeight * 0.03).toInt()
            } else {
                paddingTop
            }

            windowWidth = screenWidth - paddingHorizontal - paddingHorizontal
            windowHeight = screenHeight - paddingTop - (paddingBottom - paddingTop)
            rootView.setPadding(paddingHorizontal, paddingTop, paddingHorizontal, paddingBottom)
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left + paddingHorizontal, systemBars.top + paddingTop, systemBars.right + paddingHorizontal, systemBars.bottom + paddingBottom)
                insets
            }
        } else {
            windowWidth = screenWidth
            windowHeight = screenHeight
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (!DataStore.appSettings.backDisabled && Build.VERSION.SDK_INT < 33) {
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

    open fun setupTopbar() {
        val view = topBar ?: return
        view.setBackIconVisible(true)
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
        return DataStore.appSettings.snackbarEnabled
    }

    override fun isDestroyed(): Boolean {
        return lifecycle.currentState == Lifecycle.State.DESTROYED
    }

    override fun onContentChanged() {
        super.onContentChanged()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val rootView: ViewGroup = this.window.decorView as ViewGroup
            applyRotaryScroll(rootView)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun applyRotaryScroll(view: View) {
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
                            applyRotaryScroll(viewChild)
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
}
