package com.huanli233.biliterminal2.ui.widget.components

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.TextView
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
            updateBackIconVisibility(showIcon)

            recycle()
        }

        if (id == -1) {
            id = R.id.top_bar
        }
    }

    private fun updateBackIconVisibility(visible: Boolean) {
        val drawable = if (visible) {
            ContextCompat.getDrawable(context, R.drawable.icon_arrow_back)
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
        }
    }

    fun setTitle(text: CharSequence) {
        titleTextView.crossFadeSetText(text)
    }

    fun setBackIconVisible(visible: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            titleTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                if (visible) R.drawable.icon_arrow_back else 0, 0, 0, 0
            )
        }
    }
}