package com.huanli233.biliterminal2.ui.widget.components

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.setting.DataStore
import com.huanli233.biliterminal2.ui.utils.crossFadeSetText

class TopBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val titleTextView: TextView by lazy {
        findViewById(R.id.page_name)
    }

    private val layoutId: Int
        get() {
            return if (isInEditMode) R.layout.widget_top_bar
            else if (DataStore.appSettings.roundMode) R.layout.widget_top_bar_round
            else R.layout.widget_top_bar
        }

    init {
        inflate(
            context,
            layoutId,
            this
        )

        context.obtainStyledAttributes(attrs, R.styleable.TopBar).apply {
            getString(R.styleable.TopBar_titleText)?.let {
                titleTextView.text = it
            }

            val showIcon = getBoolean(R.styleable.TopBar_showBackIcon, true)
            setBackIconVisible(showIcon)

            recycle()
        }

        if (id == -1) {
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