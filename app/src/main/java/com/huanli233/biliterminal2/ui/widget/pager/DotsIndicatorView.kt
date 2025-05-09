package com.huanli233.biliterminal2.ui.widget.pager

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import com.google.android.material.color.MaterialColors
import androidx.core.content.ContextCompat
import com.huanli233.biliterminal2.R
import kotlin.math.min
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import kotlin.math.abs

class DotsIndicatorView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var numberOfDots = 0
    internal var currentDot = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var dotRadius = 8f
    private var dotSpacing = 12f

    private var dotColor: Int
    private var selectedDotColor: Int

    private var dotShadowRadius = 2f
    private var dotShadowDx = 1f
    private var dotShadowDy = 1f
    private var dotShadowColor = 0x80000000.toInt()

    private var internalBackgroundColor: Int = 0
    private var backgroundCornerRadius: Float = 0f
    private var backgroundPadding: Float = 0f

    private var autoHideDelay = 3000
    private var hideAnimationDuration = 300
    private var showAnimationDuration = 150

    private var hideAnimator: ObjectAnimator? = null
    private var showAnimator: ObjectAnimator? = null
    private val handler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable {
        startHideAnimation()
    }

    private val previewDotCount = 5
    private val previewSelectedDot = 0

    init {
        dotColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant, ContextCompat.getColor(context, android.R.color.darker_gray))
        selectedDotColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimary, ContextCompat.getColor(context, android.R.color.holo_blue_light))

        val defaultBackgroundColor = 0x80000000.toInt()

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.DotsIndicatorView,
            0, 0
        ).apply {
            try {
                dotRadius = getDimension(R.styleable.DotsIndicatorView_dotRadius, dotRadius)
                dotSpacing = getDimension(R.styleable.DotsIndicatorView_dotSpacing, dotSpacing)

                dotColor = getColor(R.styleable.DotsIndicatorView_dotColor, dotColor)
                selectedDotColor = getColor(R.styleable.DotsIndicatorView_selectedDotColor, selectedDotColor)

                dotShadowRadius = getDimension(R.styleable.DotsIndicatorView_dotShadowRadius, dotShadowRadius)
                dotShadowDx = getDimension(R.styleable.DotsIndicatorView_dotShadowDx, dotShadowDx)
                dotShadowDy = getDimension(R.styleable.DotsIndicatorView_dotShadowDy, dotShadowDy)
                dotShadowColor = getColor(R.styleable.DotsIndicatorView_dotShadowColor, dotShadowColor)

                internalBackgroundColor = getColor(R.styleable.DotsIndicatorView_backgroundColor, defaultBackgroundColor)
                backgroundCornerRadius = getDimension(R.styleable.DotsIndicatorView_backgroundCornerRadius, 0f)
                backgroundPadding = getDimension(R.styleable.DotsIndicatorView_backgroundPadding, 0f)

                autoHideDelay = getInt(R.styleable.DotsIndicatorView_autoHideDelay, autoHideDelay)
                hideAnimationDuration = getInt(R.styleable.DotsIndicatorView_hideAnimationDuration, hideAnimationDuration)
                showAnimationDuration = getInt(R.styleable.DotsIndicatorView_showAnimationDuration, showAnimationDuration)

            } finally {
                recycle()
            }
        }

        paint.setShadowLayer(dotShadowRadius, dotShadowDx, dotShadowDy, dotShadowColor)
        backgroundPaint.color = internalBackgroundColor

        alpha = 1f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val actualNumberOfDots = if (isInEditMode) previewDotCount else numberOfDots
        val dotsContentWidth = if (actualNumberOfDots > 0) {
            actualNumberOfDots * (2 * dotRadius + dotSpacing) - dotSpacing
        } else {
            0f
        }
        val dotsContentHeight = 2 * dotRadius

        val effectiveShadowHorizontal = abs(dotShadowDx) + dotShadowRadius * 2
        val effectiveShadowVertical = abs(dotShadowDy) + dotShadowRadius * 2


        val desiredWidth = (dotsContentWidth + 2 * backgroundPadding + effectiveShadowHorizontal).toInt()
        val desiredHeight = (dotsContentHeight + 2 * backgroundPadding + effectiveShadowVertical).toInt()

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val actualNumberOfDots: Int
        val actualCurrentDot: Int

        if (isInEditMode) {
            actualNumberOfDots = previewDotCount
            actualCurrentDot = previewSelectedDot
        } else {
            actualNumberOfDots = numberOfDots
            actualCurrentDot = currentDot
        }

        if (internalBackgroundColor != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    backgroundCornerRadius,
                    backgroundCornerRadius,
                    backgroundPaint
                )
            }
        }

        if (actualNumberOfDots == 0) {
            return
        }

        val effectiveLeft = backgroundPadding + dotShadowDx.coerceAtLeast(0f) + dotShadowRadius
        val effectiveTop = backgroundPadding + dotShadowDy.coerceAtLeast(0f) + dotShadowRadius
        val effectiveRight = width.toFloat() - backgroundPadding - dotShadowDx.coerceAtMost(0f) - dotShadowRadius
        val effectiveBottom = height.toFloat() - backgroundPadding - dotShadowDy.coerceAtMost(0f) - dotShadowRadius


        val totalContentWidth = actualNumberOfDots * (2 * dotRadius + dotSpacing) - dotSpacing
        val startX = effectiveLeft + (effectiveRight - effectiveLeft - totalContentWidth) / 2f
        val centerY = effectiveTop + (effectiveBottom - effectiveTop) / 2f

        for (i in 0 until actualNumberOfDots) {
            val x = startX + i * (2 * dotRadius + dotSpacing)
            paint.color = if (i == actualCurrentDot) selectedDotColor else dotColor
            canvas.drawCircle(x + dotRadius, centerY, dotRadius, paint)
        }
    }

    fun setNumberOfDots(count: Int) {
        if (numberOfDots == count) return
        numberOfDots = count
        if (!isInEditMode) {
            invalidate()
            requestLayout()
        } else {
            invalidate()
        }
    }

    fun setCurrentDot(position: Int) {
        if (!isInEditMode) {
            if (position < 0 || position >= numberOfDots && numberOfDots > 0) {
                if (numberOfDots > 0) {
                    currentDot = 0
                } else {
                    currentDot = 0
                }
            } else {
                currentDot = position
            }
            showAndScheduleHide()
            invalidate()
        }
    }

    private fun showAndScheduleHide() {
        hideAnimator?.cancel()
        handler.removeCallbacks(hideRunnable)

        if (alpha == 1f && isVisible) {
            handler.postDelayed(hideRunnable, autoHideDelay.toLong())
            return
        }

        showAnimator?.cancel()

        visibility = View.VISIBLE

        showAnimator = ObjectAnimator.ofFloat(this, "alpha", alpha, 1f).apply {
            duration = showAnimationDuration.toLong()
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        handler.postDelayed(hideRunnable, autoHideDelay.toLong())
    }

    private fun startHideAnimation() {
        showAnimator?.cancel()

        if (isVisible && alpha > 0f) {
            hideAnimator = ObjectAnimator.ofFloat(this, "alpha", alpha, 0f).apply {
                duration = hideAnimationDuration.toLong()
                interpolator = AccelerateDecelerateInterpolator()
                doOnEnd {
                    if (this@DotsIndicatorView.alpha == 0f) {
                        visibility = INVISIBLE
                    }
                }
                start()
            }
        } else {
            visibility = INVISIBLE
            alpha = 0f
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(hideRunnable)
        hideAnimator?.cancel()
        showAnimator?.cancel()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isVisible && alpha > 0f) {
            handler.postDelayed(hideRunnable, autoHideDelay.toLong())
        }
    }

    fun getDotRadius(): Float = dotRadius
    fun setDotRadius(radius: Float) {
        dotRadius = radius
        if (!isInEditMode) { invalidate(); requestLayout() } else invalidate()
    }

    fun getDotSpacing(): Float = dotSpacing
    fun setDotSpacing(spacing: Float) {
        dotSpacing = spacing
        if (!isInEditMode) { invalidate(); requestLayout() } else invalidate()
    }

    fun getDotColor(): Int = dotColor
    fun setDotColor(color: Int) {
        dotColor = color
        invalidate()
    }

    fun getSelectedDotColor(): Int = selectedDotColor
    fun setSelectedDotColor(color: Int) {
        selectedDotColor = color
        invalidate()
    }

    fun getDotShadowRadius(): Float = dotShadowRadius
    fun setDotShadowRadius(radius: Float) {
        dotShadowRadius = radius
        if (dotShadowRadius > 0) {
            paint.setShadowLayer(dotShadowRadius, dotShadowDx, dotShadowDy, dotShadowColor)
        } else {
            paint.clearShadowLayer()
        }
        if (!isInEditMode) { invalidate(); requestLayout() } else invalidate()
    }

    fun getDotShadowDx(): Float = dotShadowDx
    fun setDotShadowDx(dx: Float) {
        dotShadowDx = dx
        if (dotShadowRadius > 0) {
            paint.setShadowLayer(dotShadowRadius, dotShadowDx, dotShadowDy, dotShadowColor)
        }
        if (!isInEditMode) { invalidate(); requestLayout() } else invalidate()
    }

    fun getDotShadowDy(): Float = dotShadowDy
    fun setDotShadowDy(dy: Float) {
        dotShadowDy = dy
        if (dotShadowRadius > 0) {
            paint.setShadowLayer(dotShadowRadius, dotShadowDx, dotShadowDy, dotShadowColor)
        }
        if (!isInEditMode) { invalidate(); requestLayout() } else invalidate()
    }

    fun getDotShadowColor(): Int = dotShadowColor
    fun setDotShadowColor(color: Int) {
        dotShadowColor = color
        if (dotShadowRadius > 0) {
            paint.setShadowLayer(dotShadowRadius, dotShadowDx, dotShadowDy, dotShadowColor)
        }
        invalidate()
    }

    override fun setBackgroundColor(color: Int) {
        internalBackgroundColor = color
        backgroundPaint.color = internalBackgroundColor
        invalidate()
    }

    fun getCustomBackgroundColor(): Int = internalBackgroundColor

    fun getBackgroundCornerRadius(): Float = backgroundCornerRadius
    fun setBackgroundCornerRadius(radius: Float) {
        backgroundCornerRadius = radius
        invalidate()
    }

    fun getBackgroundPadding(): Float = backgroundPadding
    fun setBackgroundPadding(padding: Float) {
        backgroundPadding = padding
        if (!isInEditMode) { invalidate(); requestLayout() } else invalidate()
    }

    fun getAutoHideDelay(): Int = autoHideDelay
    fun setAutoHideDelay(delay: Int) {
        autoHideDelay = delay
    }

    fun getHideAnimationDuration(): Int = hideAnimationDuration
    fun setHideAnimationDuration(duration: Int) {
        hideAnimationDuration = duration
    }

    fun getShowAnimationDuration(): Int = showAnimationDuration
    fun setShowAnimationDuration(duration: Int) {
        showAnimationDuration = duration
    }

    fun attachTo(viewPager: ViewPager2) {
        viewPager.setupWithDotsIndicator(this)
    }
}

fun ViewPager2.setupWithDotsIndicator(dotsIndicator: DotsIndicatorView) {
    registerOnPageChangeCallback(object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            dotsIndicator.setCurrentDot(position)
        }
    })

    adapter?.registerAdapterDataObserver(object : androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            dotsIndicator.setNumberOfDots(adapter?.itemCount ?: 0)
            if (dotsIndicator.currentDot >= (adapter?.itemCount ?: 0) && (adapter?.itemCount ?: 0) > 0) {
                dotsIndicator.setCurrentDot(min(dotsIndicator.currentDot, (adapter?.itemCount ?: 1) - 1))
            } else if ((adapter?.itemCount ?: 0) == 0) {
                dotsIndicator.setCurrentDot(0)
            }
            if (currentItem >= (adapter?.itemCount ?: 0) && (adapter?.itemCount ?: 0) > 0) {
                currentItem = min(currentItem, (adapter?.itemCount ?: 1) - 1)
            } else if ((adapter?.itemCount ?: 0) == 0 && currentItem != 0) {
                currentItem = 0
            }
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            dotsIndicator.setNumberOfDots(adapter?.itemCount ?: 0)
            if (dotsIndicator.currentDot >= (adapter?.itemCount ?: 0) && (adapter?.itemCount ?: 0) > 0) {
                dotsIndicator.setCurrentDot(min(dotsIndicator.currentDot, (adapter?.itemCount ?: 1) - 1))
            } else if ((adapter?.itemCount ?: 0) == 0) {
                dotsIndicator.setCurrentDot(0)
            }
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            dotsIndicator.setNumberOfDots(adapter?.itemCount ?: 0)
            if (dotsIndicator.currentDot >= (adapter?.itemCount ?: 0) && (adapter?.itemCount ?: 0) > 0) {
                dotsIndicator.setCurrentDot(min(dotsIndicator.currentDot, (adapter?.itemCount ?: 1) - 1))
            } else if ((adapter?.itemCount ?: 0) == 0) {
                dotsIndicator.setCurrentDot(0)
            }
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            dotsIndicator.setNumberOfDots(adapter?.itemCount ?: 0)
            val newCount = adapter?.itemCount ?: 0
            if (dotsIndicator.currentDot >= newCount && newCount > 0) {
                val newCurrentDot = min(dotsIndicator.currentDot, newCount - 1)
                dotsIndicator.setCurrentDot(newCurrentDot)
                currentItem = newCurrentDot
            } else if (newCount == 0) {
                dotsIndicator.setCurrentDot(0)
                currentItem = 0
            }
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            dotsIndicator.setNumberOfDots(adapter?.itemCount ?: 0)
        }
    })

    dotsIndicator.setNumberOfDots(adapter?.itemCount ?: 0)
    dotsIndicator.setCurrentDot(currentItem)
}