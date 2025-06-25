package com.huanli233.bilizepam.ui.widget.components

import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.animation.AnimationUtils
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.highcapable.betterandroid.ui.extension.view.updateCompoundDrawablesWithIntrinsicBounds
import com.highcapable.betterandroid.ui.extension.view.updateMargins
import com.highcapable.hikage.annotation.HikageView
import com.highcapable.hikage.extension.widget.bottomToParent
import com.highcapable.hikage.extension.widget.endToParent
import com.highcapable.hikage.extension.widget.startToParent
import com.highcapable.hikage.extension.widget.topToParent
import com.highcapable.hikage.widget.android.widget.TextSwitcher
import com.highcapable.hikage.widget.androidx.constraintlayout.widget.Guideline
import com.highcapable.hikage.widget.com.google.android.material.divider.MaterialDivider
import com.highcapable.hikage.widget.com.huanli233.bilizepam.ui.widget.views.AppTextClock
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.ui.utils.hikage.extension.attach
import com.huanli233.bilizepam.ui.utils.hikage.extension.boldTypeFace
import com.huanli233.bilizepam.utils.extensions.editModeText
import com.huanli233.bilizepam.utils.extensions.updateMarginsRelativeCompat
import com.huanli233.bilizepam.utils.extensions.updatePaddingRelativeCompat
import splitties.views.gravityCenterVertical
import splitties.views.gravityEnd

@HikageView
class TopBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val titleTextSwitcher: TextSwitcher by lazy {
        findViewById(R.id.page_name)
    }

    val roundMode = !isInEditMode && LocalData.settings.uiSettings.roundMode

    enum class State {
        MENU,
        PAGE
    }

    init {
        attach<LayoutParams> {
            TextSwitcher(
                lparams = LayoutParams {
                    if (roundMode) {
                        updateMargins(horizontal = 4.dp)
                        updateMargins(top = 2.dp)
                        topToBottom = R.id.text_clock
                        startToParent()
                        endToParent()
                    } else {
                        updateMarginsRelativeCompat(end = 8.dp)
                        startToParent()
                        endToEnd = viewId("guideline")
                        horizontalBias = 0f
                        topToParent()
                        bottomToParent()
                    }
                },
                init = {
                    id = R.id.page_name
                    setFactory {
                        val textView = TextView(context).apply {
                            id = R.id.page_name
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            boldTypeFace()
                            gravity = gravityCenterVertical
                            editModeText = "Page Name"
                            updateCompoundDrawablesWithIntrinsicBounds(
                                left = drawableResource(R.drawable.icon_chevron_right)
                            )

                            if (roundMode) {
                                textSize = 14f
                            } else {
                                updatePaddingRelativeCompat(top = 4.dp, start = 7.dp)
                                textSize = 12f
                            }
                        }
                        textView
                    }

                    inAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
                    outAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
                }
            )
            AppTextClock(
                lparams = LayoutParams {
                    if (roundMode) {
                        updateMargins(horizontal = 4.dp)
                        updateMargins(top = 2.dp)
                        startToParent()
                        endToParent()
                        topToParent()
                    } else {
                        updateMarginsRelativeCompat(end = 16.dp, top = 4.dp, bottom = 2.dp)
                        endToParent()
                        topToParent()
                        bottomToParent()
                    }
                }
            ) {
                id = R.id.text_clock
                boldTypeFace()
                gravity = gravityCenterVertical or gravityEnd
                textSize = 12f
                editModeText = "12:08"

                if (roundMode) {
                    alpha = 0.85f
                }
            }
            if (roundMode) {
                MaterialDivider(
                    lparams = widthMatchParent {
                        topToBottom = R.id.page_name
                        updateMargins(top = 2.dp)
                    }
                ) {
                    dividerInsetStart = 10.dp
                    dividerInsetEnd = 10.dp
                }
            } else {
                Guideline(
                    id = "guideline",
                    lparams = LayoutParams { guidePercent = 0.7f; orientation = LayoutParams.VERTICAL }
                )
            }
        }

        context.obtainStyledAttributes(attrs, R.styleable.TopBar).apply {
            getString(R.styleable.TopBar_titleText)?.let {
                setTitle(it)
            }

            val showIcon = getBoolean(R.styleable.TopBar_showBackIcon, true)
            setBackIconVisible(showIcon)

            recycle()
        }

        id = R.id.top_bar
    }

    private fun updateBackIconVisibility(
        visible: Boolean,
        @DrawableRes icon: Int
    ) {
        val drawable = if (visible) {
            ContextCompat.getDrawable(context, icon)
        } else {
            null
        }

        val currentView = titleTextSwitcher.currentView as TextView

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val drawables = currentView.compoundDrawablesRelative
            currentView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                drawable,
                drawables[1],
                drawables[2],
                drawables[3]
            )
        } else {
            val drawables = currentView.compoundDrawables
            currentView.setCompoundDrawablesWithIntrinsicBounds(
                drawable,
                drawables[1],
                drawables[2],
                drawables[3]
            )
        }
    }

    fun setTitle(text: CharSequence) {
        titleTextSwitcher.setText(text)
    }

    fun setBackIconVisible(
        visible: Boolean,
        @DrawableRes icon: Int = R.drawable.icon_chevron_left
    ) {
        updateBackIconVisibility(visible, icon)
    }

    var state: State = State.PAGE
        private set

    fun setState(newState: State, pageName: () -> String) {
        when (newState) {
            State.MENU -> {
                titleTextSwitcher.apply {
                    inAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_top)
                    outAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_bottom)
                }
                setTitle(context.getString(R.string.menu))
                setBackIconVisible(true, R.drawable.icon_keyboard_arrow_left)
            }
            State.PAGE -> {
                when (state) {
                    State.MENU -> titleTextSwitcher.apply {
                        inAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_bottom)
                        outAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_top)
                    }
                    State.PAGE -> titleTextSwitcher.apply {
                        inAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
                        outAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
                    }
                }
                setTitle(pageName())
                setBackIconVisible(true, R.drawable.icon_keyboard_arrow_down)
            }
        }
        state = newState
    }
}