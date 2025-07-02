package com.huanli233.bilizepam.ui.activity.base

import android.annotation.SuppressLint
import android.content.Context
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

    val configurationController = ConfigurationOverrideController(this)
    val originalViewContext
        get() = configurationController.originalViewContext
    val uiPaddingManager = UiPaddingManager(this)

    var topBar: TopBar? = null

    override fun attachBaseContext(newBase: Context) {
        val newContext = configurationController.overrideConfiguration(newBase)
        super.attachBaseContext(newContext)
        configurationController.attachContext(newBase)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationController.configurationChanged()
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
        uiPaddingManager.applyRootViewPadding(window.decorView.rootView)
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
