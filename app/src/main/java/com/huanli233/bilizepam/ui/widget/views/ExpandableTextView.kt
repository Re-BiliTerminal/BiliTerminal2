package com.huanli233.bilizepam.ui.widget.views

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import com.huanli233.bilizepam.R
import androidx.core.content.withStyledAttributes

class ExpandableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val textView: TextView
    private val toggleArea: FrameLayout
    private val arrowImageView: ImageView

    private var isExpanded = false
    private var collapsedLines: Int = 3
    private var animationDuration: Int = 300
    private var originalText: CharSequence = ""

    private var collapsedHeight: Int = -1
    private var expandedHeight: Int = -1

    private var currentAnimator: ValueAnimator? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_expandable_text, this, true)
        orientation = VERTICAL

        textView = findViewById(R.id.tv_content)
        toggleArea = findViewById(R.id.fl_toggle_area)
        arrowImageView = findViewById(R.id.iv_arrow)

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.ExpandableTextView, 0, 0) {
                collapsedLines = getInt(R.styleable.ExpandableTextView_e_collapsedLines, 3)
                animationDuration = getInt(R.styleable.ExpandableTextView_e_animationDuration, 300)
                originalText = getString(R.styleable.ExpandableTextView_e_text) ?: ""
            }
        }

        if (originalText.isNotEmpty()) {
            setText(originalText)
        }

        setOnClickListener {
            toggle()
        }
    }

    fun setText(text: CharSequence) {
        originalText = text
        textView.text = text
        currentAnimator?.cancel()

        post {
            if (textView.lineCount <= collapsedLines) {
                toggleArea.isVisible = false
                isClickable = false
                textView.maxLines = Int.MAX_VALUE
            } else {
                toggleArea.isVisible = true
                isClickable = true
                collapse(false)
            }
        }
    }

    private fun toggle() {
        currentAnimator?.cancel()
        arrowImageView.animate().cancel()

        if (isExpanded) {
            collapse(true)
        } else {
            expand()
        }
    }

    private fun expand() {
        isExpanded = true

        if (expandedHeight == -1) {
            textView.maxLines = Int.MAX_VALUE
            textView.measure(
                MeasureSpec.makeMeasureSpec(textView.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            expandedHeight = textView.measuredHeight
        }

        val startHeight = textView.height
        val endHeight = expandedHeight

        textView.maxLines = Int.MAX_VALUE

        currentAnimator = ValueAnimator.ofInt(startHeight, endHeight).apply {
            duration = this@ExpandableTextView.animationDuration.toLong()
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                textView.height = animation.animatedValue as Int
            }
            doOnEnd { currentAnimator = null }
            doOnCancel { currentAnimator = null }
        }
        currentAnimator?.start()
        arrowImageView.animate().rotation(180f).setDuration(this.animationDuration.toLong()).start()
    }

    private fun collapse(withAnimation: Boolean) {
        isExpanded = false

        if (collapsedHeight == -1) {
            textView.maxLines = collapsedLines
            textView.measure(
                MeasureSpec.makeMeasureSpec(textView.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            collapsedHeight = textView.measuredHeight
        }

        if (!withAnimation) {
            textView.maxLines = collapsedLines
            arrowImageView.rotation = 0f
            return
        }

        val startHeight = textView.height
        val endHeight = collapsedHeight

        currentAnimator = ValueAnimator.ofInt(startHeight, endHeight).apply {
            duration = this@ExpandableTextView.animationDuration.toLong()
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                textView.height = animation.animatedValue as Int
            }
            doOnEnd {
                textView.maxLines = collapsedLines
                currentAnimator = null
            }
            doOnCancel { currentAnimator = null }
        }
        currentAnimator?.start()
        arrowImageView.animate().rotation(0f).setDuration(this.animationDuration.toLong()).start()
    }
}