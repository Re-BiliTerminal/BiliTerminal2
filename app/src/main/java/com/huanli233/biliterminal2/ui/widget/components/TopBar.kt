package com.huanli233.biliterminal2.ui.widget.components

import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.highcapable.betterandroid.ui.extension.view.LayoutParamsMatchParent
import com.highcapable.betterandroid.ui.extension.view.LayoutParamsWrapContent
import com.highcapable.betterandroid.ui.extension.view.updateCompoundDrawablesWithIntrinsicBounds
import com.highcapable.betterandroid.ui.extension.view.updateMargins
import com.highcapable.hikage.annotation.HikageView
import com.highcapable.hikage.extension.widget.bottomToParent
import com.highcapable.hikage.extension.widget.endToParent
import com.highcapable.hikage.extension.widget.startToParent
import com.highcapable.hikage.extension.widget.topToParent
import com.highcapable.hikage.widget.android.widget.TextView
import com.highcapable.hikage.widget.androidx.constraintlayout.widget.Guideline
import com.highcapable.hikage.widget.com.google.android.material.divider.MaterialDivider
import com.highcapable.hikage.widget.com.huanli233.biliterminal2.ui.widget.views.AppTextClock
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.setting.LocalData
import com.huanli233.biliterminal2.ui.utils.crossFadeSetText
import com.huanli233.biliterminal2.ui.utils.hikage.extension.Hikage
import com.huanli233.biliterminal2.ui.utils.hikage.extension.boldTypeFace
import com.huanli233.biliterminal2.ui.utils.view.ViewHierarchyPrinter
import com.huanli233.biliterminal2.utils.extensions.editModeText
import com.huanli233.biliterminal2.utils.extensions.updateMarginsRelativeCompat
import com.huanli233.biliterminal2.utils.extensions.updatePaddingRelativeCompat
import splitties.views.gravityCenterVertical
import splitties.views.gravityEnd

@HikageView
class TopBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val titleTextView: TextView by lazy {
        findViewById(R.id.page_name)
    }

    override fun addView(child: View?) {
        super.addView(child)
        ViewHierarchyPrinter.printViewHierarchy(child)
    }

    val roundMode = !isInEditMode && LocalData.settings.uiSettings.roundMode

    init {

        Hikage<LayoutParams> {
            TextView(
                lparams = LayoutParams(width = if (roundMode) LayoutParamsWrapContent else 0, height = LayoutParamsWrapContent) {
                    if (roundMode) {
                        updateMargins(horizontal = 4.dp)
                        updateMargins(top = 2.dp)
                        topToBottom = R.id.text_clock
                        startToParent()
                        endToParent()
                    } else {
                        updateMarginsRelativeCompat(end = 8.dp)
                        startToParent()
                        endToStart = viewId("guideline")
                        topToParent()
                        bottomToParent()
                    }
                }
            ) {
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
                gravity = gravityCenterVertical and gravityEnd
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
                titleTextView.text = it
            }

            val showIcon = getBoolean(R.styleable.TopBar_showBackIcon, true)
            setBackIconVisible(showIcon)

            recycle()
        }

        if (id == NO_ID) {
            id = R.id.top_bar
        }
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val drawables = titleTextView.compoundDrawablesRelative
            titleTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                drawable,
                drawables[1],
                drawables[2],
                drawables[3]
            )
        } else {
            val drawables = titleTextView.compoundDrawables
            titleTextView.setCompoundDrawablesWithIntrinsicBounds(
                drawable,
                drawables[1],
                drawables[2],
                drawables[3]
            )
        }
    }

    fun setTitle(text: CharSequence) {
        titleTextView.crossFadeSetText(text)
    }

    fun setBackIconVisible(
        visible: Boolean,
        @DrawableRes icon: Int = R.drawable.icon_chevron_left
    ) {
        updateBackIconVisibility(visible, icon)
    }
}