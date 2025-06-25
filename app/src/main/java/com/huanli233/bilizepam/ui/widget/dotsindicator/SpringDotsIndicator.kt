package com.huanli233.bilizepam.ui.widget.dotsindicator

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.RelativeLayout
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.huanli233.bilizepam.R
import androidx.core.content.withStyledAttributes
import androidx.core.view.contains
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.extension.widget.centerInParent
import com.highcapable.hikage.widget.android.widget.ImageView
import com.highcapable.hikage.widget.android.widget.RelativeLayout
import com.huanli233.bilizepam.ui.utils.hikage.extension.attach
import com.huanli233.bilizepam.utils.extensions.setBackgroundCompat
import splitties.views.gravityCenterVertical

class SpringDotsIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseDotsIndicator(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_DAMPING_RATIO = 0.5f
        private const val DEFAULT_STIFFNESS = 300
    }

    private var dotIndicatorView: View? = null
    private var dotIndicatorHikage: Hikage? = null

    // Attributes
    private var dotsStrokeWidth: Float = 0f
    private var dotsStrokeColor: Int = 0
    private var dotIndicatorColor: Int = 0
    private var stiffness: Float = 0f
    private var dampingRatio: Float = 0f

    private val dotIndicatorSize: Float
    private var dotIndicatorSpring: SpringAnimation? = null
    private val strokeDotsLinearLayout: LinearLayout = LinearLayout(context)

    init {

        val horizontalPadding = dpToPxF(24f)
        clipToPadding = false
        setPadding(horizontalPadding.toInt(), 0, horizontalPadding.toInt(), 0)
        strokeDotsLinearLayout.orientation = HORIZONTAL
        addView(
            strokeDotsLinearLayout, ViewGroup.LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

        dotsStrokeWidth = dpToPxF(2f) // 2dp
        dotIndicatorColor = context.getThemePrimaryColor()
        dotsStrokeColor = dotIndicatorColor
        stiffness = DEFAULT_STIFFNESS.toFloat()
        dampingRatio = DEFAULT_DAMPING_RATIO

        if (attrs != null) {
            getContext().withStyledAttributes(attrs, R.styleable.SpringDotsIndicator) {

                // Dots attributes
                dotIndicatorColor =
                    getColor(R.styleable.SpringDotsIndicator_dotsColor, dotIndicatorColor)
                dotsStrokeColor = getColor(
                    R.styleable.SpringDotsIndicator_dotsStrokeColor,
                    dotIndicatorColor
                )
                stiffness = getFloat(R.styleable.SpringDotsIndicator_stiffness, stiffness)
                dampingRatio = getFloat(R.styleable.SpringDotsIndicator_dampingRatio, dampingRatio)

                // Spring dots attributes
                dotsStrokeWidth = getDimension(
                    R.styleable.SpringDotsIndicator_dotsStrokeWidth,
                    dotsStrokeWidth
                )

            }
        }

        dotIndicatorSize = dotsSize

        if (isInEditMode) {
            addDots(5)
            addView(buildDot(false).root)
        }

        setUpDotIndicator()
    }

    private fun setUpDotIndicator() {
        if (pager?.isEmpty == true) {
            return
        }

        if (dotIndicatorView != null && contains(dotIndicatorView!!)) {
            removeView(dotIndicatorView)
        }

        val hikage = buildDot(false)
        dotIndicatorHikage = hikage
        dotIndicatorView = hikage.root
        addView(dotIndicatorView)
        dotIndicatorSpring = SpringAnimation(dotIndicatorView, SpringAnimation.TRANSLATION_X)
        val springForce = SpringForce(0f)
        springForce.dampingRatio = dampingRatio
        springForce.stiffness = stiffness
        dotIndicatorSpring!!.spring = springForce
    }

    override fun addDot(index: Int) {
        val dot = buildDot(true)
        dot.root.setOnClickListener {
            if (dotsClickable && index < (pager?.count ?: 0)) {
                pager!!.setCurrentItem(index, true)
            }
        }

        dots.add(dot.get<View>("spring_dot") as ImageView)
        strokeDotsLinearLayout.addView(dot.root)
    }

    private fun buildDot(stroke: Boolean): Hikage {
        val dotHikage = attach<LayoutParams>(attachToParent = false) {
            RelativeLayout(lparams = LayoutParams { gravity = gravityCenterVertical }) {
                ImageView(
                    id = "spring_dot",
                    lparams = LayoutParams(8.dp, 8.dp) {
                        centerInParent()
                    }
                ) {
                    setBackgroundCompat(drawableRes(R.drawable.spring_dot_background))
                }
            }
        }
        val dot = dotHikage.root
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
            dot.layoutDirection = LAYOUT_DIRECTION_LTR
        }

        val dotView = dotHikage.get<ImageView>("spring_dot")
        dotView.setBackgroundResource(
            if (stroke) R.drawable.spring_dot_stroke_background else R.drawable.spring_dot_background
        )
        val params = dotView.layoutParams as RelativeLayout.LayoutParams
        params.height = (if (stroke) dotsSize else dotIndicatorSize).toInt()
        params.width = params.height
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)

        params.setMargins(dotsSpacing.toInt(), 0, dotsSpacing.toInt(), 0)

        setUpDotBackground(stroke, dotView)
        return dotHikage
    }

    private fun setUpDotBackground(stroke: Boolean, dot: ImageView) {
        val dotBackground =
            dot.background as GradientDrawable
        if (stroke) {
            dotBackground.setStroke(dotsStrokeWidth.toInt(), dotsStrokeColor)
        } else {
            dotBackground.setColor(dotIndicatorColor)
        }
        dotBackground.cornerRadius = dotsCornerRadius
    }

    override fun removeDot() {
        strokeDotsLinearLayout.removeViewAt(strokeDotsLinearLayout.childCount - 1)
        dots.removeAt(dots.size - 1)
    }

    override fun refreshDotColor(index: Int) {
        setUpDotBackground(true, dots[index])
    }

    override fun buildOnPageChangedListener(): OnPageChangeListenerHelper {
        return object : OnPageChangeListenerHelper() {

            override val pageCount: Int
                get() = dots.size

            override fun onPageScrolled(
                selectedPosition: Int,
                nextPosition: Int,
                positionOffset: Float
            ) {
                val distance = dotsSize + dotsSpacing * 2
                val x = (dots[selectedPosition].parent as ViewGroup).left
                val globalPositionOffsetPixels = x + distance * positionOffset
                dotIndicatorSpring?.animateToFinalPosition(globalPositionOffsetPixels)
            }

            override fun resetPosition(position: Int) {
                // Empty
            }
        }
    }

    override val type get() = Type.SPRING

    //*********************************************************
    // Users Methods
    //*********************************************************

    /**
     * Set the indicator dot color.
     *
     * @param color the color fo the indicator dot.
     */
    fun setDotIndicatorColor(color: Int) {
        if (dotIndicatorView != null) {
            dotIndicatorColor = color
            setUpDotBackground(false, dotIndicatorHikage?.get<ImageView>("spring_dot")!!)
        }
    }

    /**
     * Set the stroke indicator dots color.
     *
     * @param color the color for the stroke indicator dots.
     */
    fun setStrokeDotsIndicatorColor(color: Int) {
        dotsStrokeColor = color
        for (v in dots) {
            setUpDotBackground(true, v)
        }
    }

    /**
     * Set the dots stroke width.
     *
     * @param width the stroke width color for the indicator dots.
     */
    fun setDotsStrokeWidth(width: Float) {
        dotsStrokeWidth = width
        for (v in dots) {
            setUpDotBackground(true, v)
        }
    }
}