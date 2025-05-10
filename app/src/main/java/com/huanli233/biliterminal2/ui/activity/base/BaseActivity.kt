package com.huanli233.biliterminal2.ui.activity.base

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.setting.LocalData
import com.huanli233.biliterminal2.event.SnackEvent
import com.huanli233.biliterminal2.ui.activity.base.material.ThemedAppCompatActivity
import com.huanli233.biliterminal2.ui.utils.crossFadeSetText
import com.huanli233.biliterminal2.ui.widget.components.TopBar
import com.huanli233.biliterminal2.utils.MsgUtil
import com.huanli233.biliterminal2.utils.ThemeUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

open class BaseActivity : ThemedAppCompatActivity() {
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
                        setTo(system.displayMetrics)
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
        val dpiTimes = LocalData.settings.uiSettings.uiScale
        val density = LocalData.settings.uiSettings.density
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

        val paddingHPercent: Int = LocalData.settings.uiSettings.uiPaddingHorizontal
        val paddingVPercent: Int = LocalData.settings.uiSettings.uiPaddingVertical

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
            val paddingBottom = if (LocalData.settings.uiSettings.roundMode) {
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

    override fun computeUserThemeKey(): String? {
        return ThemeUtil.getColorTheme()
    }

    override fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        if (!ThemeUtil.isSystemAccent()) {
            theme.applyStyle(ThemeUtil.getColorThemeStyleRes(), true)
        }
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (!LocalData.settings.preferences.backDisabled && Build.VERSION.SDK_INT < 33) {
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
        return LocalData.settings.uiSettings.snackbarEnabled
    }

    override fun isDestroyed(): Boolean {
        return lifecycle.currentState == Lifecycle.State.DESTROYED
    }

}
