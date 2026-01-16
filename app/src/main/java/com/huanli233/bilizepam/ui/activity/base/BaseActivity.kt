package com.huanli233.bilizepam.ui.activity.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.huanli233.bilizepam.data.proto.AppSettings
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.ui.activity.base.material.ThemedAppCompatActivity
import com.huanli233.bilizepam.ui.animations.playAnimation
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

open class BaseActivity : ThemedAppCompatActivity() {

    open val rootViewPaddingEnabled = false
    open val transitionEnabled = false

    var contentTransitionName
        get() = ViewCompat.getTransitionName(findViewById(android.R.id.content))
        set(value) = ViewCompat.setTransitionName(findViewById(android.R.id.content), value)

    val configurationController = ConfigurationOverrideController(this)
    val originalViewContext
        get() = configurationController.originalViewContext
    val uiPaddingManager = UiPaddingManager(this)

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
            findViewById<View>(android.R.id.content).setOnApplyWindowInsetsListener { v, insets ->
                val systemBars = WindowInsetsCompat.toWindowInsetsCompat(insets).getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        super.onCreate(savedInstanceState)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        uiPaddingManager.applyRootViewPadding(window.decorView.rootView)

        // Add a listener for settings changes
        observeSettingsChanges()
    }

    var lastSettings = LocalData.settingsStateFlow.value

    private fun observeSettingsChanges() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                LocalData.settingsStateFlow.filterNotNull().collect { newSettings ->
                    lastSettings?.let {
                        handleSettingsChange(it, newSettings)
                    }
                    lastSettings = newSettings
                }
            }
        }
    }

    private fun handleSettingsChange(oldSettings: AppSettings, newSettings: AppSettings) {
        // Check for changes that require the activity to be recreated
        val needsRecreate = oldSettings.uiSettings.uiScale != newSettings.uiSettings.uiScale ||
                oldSettings.uiSettings.density != newSettings.uiSettings.density || oldSettings.theme.nightMode != newSettings.theme.nightMode ||
                oldSettings.theme.colorTheme != newSettings.theme.colorTheme || oldSettings.theme.followSystemAccent != newSettings.theme.followSystemAccent ||
                oldSettings.language != newSettings.language

        if (needsRecreate) {
            recreate()
            return // No need to process other changes if we are recreating
        }

        // Check for padding changes that can be applied live
        val paddingChanged = oldSettings.uiSettings.uiPaddingHorizontal != newSettings.uiSettings.uiPaddingHorizontal ||
                oldSettings.uiSettings.uiPaddingVertical != newSettings.uiSettings.uiPaddingVertical ||
                oldSettings.uiSettings.roundMode != newSettings.uiSettings.roundMode

        if (paddingChanged) {
            uiPaddingManager.applyRootViewPadding(window.decorView.rootView)
        }
    }

    fun configBaseTransition() {
        playAnimation {
            window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            configTransition()
        }
    }

    open fun configTransition() = Unit

    fun setupSharedElementTransitionExit() {
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementsUseOverlay = false
    }

    fun setupSharedElementTransitionEnter() {
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

    override fun computeUserThemeKey(): String? {
        return ""
    }

    @SuppressLint("GestureBackNavigation")
    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (!LocalData.settings.preferences.backDisabled && Build.VERSION.SDK_INT < 33) {
            super.onBackPressed()
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

    override fun onDestroy() {
        super.onDestroy()
        if (eventBusInit) {
            EventBus.getDefault().unregister(this)
            eventBusInit = false
        }
    }

    protected open fun eventBusEnabled(): Boolean {
        return false
    }

    override fun isDestroyed(): Boolean {
        return lifecycle.currentState == Lifecycle.State.DESTROYED
    }

}