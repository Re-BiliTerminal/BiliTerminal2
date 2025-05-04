package com.huanli233.biliterminal2.ui.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.huanli233.biliterminal2.R

class TopBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.widget_top_bar, this)

        context.obtainStyledAttributes(attrs, R.styleable.TopBar).apply {
            getString(R.styleable.TopBar_titleText)?.let {
                findViewById<TextView>(R.id.page_name).text = it
            }
            
            val showIcon = getBoolean(R.styleable.TopBar_showBackIcon, true)
            updateBackIconVisibility(showIcon)

            recycle()
        }
    }

    private fun updateBackIconVisibility(visible: Boolean) {
        val textView = findViewById<TextView>(R.id.page_name)
        val drawable = if (visible) {
            ContextCompat.getDrawable(context, R.drawable.arrow_back)
        } else {
            null
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val drawables = textView.compoundDrawablesRelative
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                drawable,
                drawables[1],
                drawables[2],
                drawables[3]
            )
        }
    }

    fun setTitle(text: CharSequence) {
        findViewById<TextView>(R.id.page_name).text = text
    }

    fun setBackIconVisible(visible: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            findViewById<TextView>(R.id.page_name).setCompoundDrawablesRelativeWithIntrinsicBounds(
                if (visible) R.drawable.arrow_back else 0, 0, 0, 0
            )
        }
    }
}