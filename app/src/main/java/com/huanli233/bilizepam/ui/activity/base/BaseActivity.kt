package com.huanli233.bilizepam.ui.activity.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.event.SnackEvent
import com.huanli233.bilizepam.ui.activity.base.material.ThemedAppCompatActivity
import com.huanli233.bilizepam.ui.utils.playAnimation
import com.huanli233.bilizepam.ui.widget.components.TopBar
import com.huanli233.bilizepam.utils.MsgUtil
import com.huanli233.bilizepam.utils.ThemeUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

open class BaseActivity : ThemedAppCompatActivity() {

    open val rootViewPaddingEnabled = true
    open val transitionEnabled = false

    var contentTransitionName
        get() = ViewCompat.getTransitionName(findViewById(android.R.id.content))
        set(value) = ViewCompat.setTransitionName(findViewById(android.R.id.content), value)

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

    override fun attachBaseContext(newBase: Context) {
        val newContext = overrideConfiguration(newBase)
        super.attachBaseContext(newContext)
        _originalContext = newBase
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
        if (transitionEnabled) {
            configBaseTransition()
        }

        enableEdgeToEdge()
        if (rootViewPaddingEnabled) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        super.onCreate(savedInstanceState)

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
        } else {
            windowWidth = screenWidth
            windowHeight = screenHeight
        }
    }

    fun configBaseTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            playAnimation {
                window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
                configTransition()
            }
        }
    }

    @RequiresApi(21)
    open fun configTransition() = Unit

    fun setupSharedElementTransitionExit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
            window.sharedElementsUseOverlay = false
        }
    }

    fun setupSharedElementTransitionEnter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
            window.sharedElementEnterTransition = MaterialContainerTransform().apply {
                addTarget(android.R.id.content)
                duration = 300L
                setAllContainerColors(
                    MaterialColors.getColor(findViewById(android.R.id.content), com.google.android.material.R.attr.colorSurface))
            }
            window.sharedElementReturnTransition = MaterialContainerTransform().apply {
                addTarget(android.R.id.content)
                duration = 250L
                setAllContainerColors(
                    MaterialColors.getColor(findViewById(android.R.id.content), com.google.android.material.R.attr.colorSurface))
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

    @SuppressLint("GestureBackNavigation")
    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (!LocalData.settings.preferences.backDisabled && Build.VERSION.SDK_INT < 33) {
            super.onBackPressed()
        }
    }

    open var pageName: String? = null
        set(value) {
            field = value
            value?.let { setTopbarTitle(it) }
        }

    private fun setTopbarTitle(
        name: String
    ) {
        topBar?.setTitle(name)
    }

    open fun setupTopbar() {
        val view = topBar ?: return
        view.setIcon(true)
        if (Build.VERSION.SDK_INT > 17 && view.hasOnClickListeners()) return
        view.setOnClickListener {
            onTopbarClicked()
        }
    }

    open fun onTopbarClicked() {
        if (!isDestroyed) {
            onBackPressedDispatcher.onBackPressed()
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
            pageName?.let { setTopbarTitle(it) }
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
