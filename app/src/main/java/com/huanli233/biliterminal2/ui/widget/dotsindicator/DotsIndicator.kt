package com.huanli233.biliterminal2.ui.widget.dotsindicator

import android.animation.ArgbEvaluator
import android.content.Context
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import com.highcapable.hikage.widget.android.widget.FrameLayout
import com.highcapable.hikage.widget.android.widget.ImageView
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.ui.utils.hikage.extension.attach
import splitties.views.gravityCenter
import splitties.views.gravityCenterVertical

class DotsIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseDotsIndicator(context, attrs, defStyleAttr) {

    companion object {
        const val DEFAULT_WIDTH_FACTOR = 2.5f
    }

    private lateinit var linearLayout: LinearLayout
    private var dotsWidthFactor: Float = 0f
    private var progressMode: Boolean = false
    private var dotsElevation: Float = 0f

    var selectedDotColor: Int = 0
        set(value) {
            field = value
            refreshDotsColors()
        }

    private val argbEvaluator = ArgbEvaluator()

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        addView(linearLayout, WRAP_CONTENT, WRAP_CONTENT)

        dotsWidthFactor = DEFAULT_WIDTH_FACTOR

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.DotsIndicator)

            selectedDotColor =
                a.getColor(R.styleable.DotsIndicator_selectedDotColor, DEFAULT_POINT_COLOR)

            dotsWidthFactor = a.getFloat(R.styleable.DotsIndicator_dotsWidthFactor, 2.5f)
            if (dotsWidthFactor < 1) {
                Log.w(
                    "DotsIndicator",
                    "The dotsWidthFactor can't be set under 1.0f, please set an higher value"
                )
                dotsWidthFactor = 1f
            }

            progressMode = a.getBoolean(R.styleable.DotsIndicator_progressMode, false)

            dotsElevation = a.getDimension(R.styleable.DotsIndicator_dotsElevation, 0f)

            a.recycle()
        }

        if (isInEditMode) {
            addDots(5)
            refreshDots()
        }

    }

    override fun addDot(index: Int) {
        val dot = attach<LayoutParams>(attachToParent = false) {
            FrameLayout(
                lparams = LayoutParams {
                    gravity = gravityCenterVertical
                },
                init = {
                    clipToPadding = false
                }
            ) {
                ImageView(
                    id = "dot",
                    lparams = LayoutParams(8.dp, 8.dp) {
                        gravity = gravityCenter
                    }
                ) {
                    setBackgroundCompat(drawableRes(R.drawable.dot_background))
                }
            }
        }
        val imageView = dot.get<ImageView>("dot")
        val params = imageView.layoutParams as LayoutParams

        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
            dot.root.layoutDirection = LAYOUT_DIRECTION_LTR
        }

        params.height = dotsSize.toInt()
        params.width = params.height
        params.setMargins(dotsSpacing.toInt(), 0, dotsSpacing.toInt(), 0)
        val background = DotsGradientDrawable()
        background.cornerRadius = dotsCornerRadius
        if (isInEditMode) {
            background.setColor(if (0 == index) selectedDotColor else dotsColor)
        } else {
            background.setColor(if (pager!!.currentItem == index) selectedDotColor else dotsColor)
        }
        imageView.setBackgroundCompat(background)

        dot.root.setOnClickListener {
            if (dotsClickable && index < (pager?.count ?: 0)) {
                pager!!.setCurrentItem(index, true)
            }
        }

        dot.root.setPaddingHorizontal((dotsElevation * 0.8f).toInt())
        dot.root.setPaddingVertical((dotsElevation * 2).toInt())
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            imageView.elevation = dotsElevation
        }

        dots.add(imageView)
        linearLayout.addView(dot.root)
    }

    override fun removeDot() {
        linearLayout.removeViewAt(linearLayout.childCount - 1)
        dots.removeAt(dots.size - 1)
    }

    override fun buildOnPageChangedListener(): OnPageChangeListenerHelper {
        return object : OnPageChangeListenerHelper() {
            override fun onPageScrolled(
                selectedPosition: Int,
                nextPosition: Int,
                positionOffset: Float
            ) {
                val selectedDot = dots[selectedPosition]
                // Selected dot
                val selectedDotWidth =
                    (dotsSize + dotsSize * (dotsWidthFactor - 1) * (1 - positionOffset)).toInt()
                selectedDot.setWidth(selectedDotWidth)

                if (dots.isInBounds(nextPosition)) {
                    val nextDot = dots[nextPosition]

                    val nextDotWidth =
                        (dotsSize + dotsSize * (dotsWidthFactor - 1) * positionOffset).toInt()
                    nextDot.setWidth(nextDotWidth)

                    val selectedDotBackground = selectedDot.background as DotsGradientDrawable
                    val nextDotBackground = nextDot.background as DotsGradientDrawable

                    if (selectedDotColor != dotsColor) {
                        val selectedColor = argbEvaluator.evaluate(
                            positionOffset, selectedDotColor,
                            dotsColor
                        ) as Int
                        val nextColor = argbEvaluator.evaluate(
                            positionOffset, dotsColor,
                            selectedDotColor
                        ) as Int

                        nextDotBackground.setColor(nextColor)

                        if (progressMode && selectedPosition <= pager!!.currentItem) {
                            selectedDotBackground.setColor(selectedDotColor)
                        } else {
                            selectedDotBackground.setColor(selectedColor)
                        }
                    }
                }

                invalidate()
            }

            override fun resetPosition(position: Int) {
                dots[position].setWidth(dotsSize.toInt())
                refreshDotColor(position)
            }

            override val pageCount: Int
                get() = dots.size
        }
    }

    override fun refreshDotColor(index: Int) {
        val elevationItem = dots[index]
        val background = elevationItem.background as? DotsGradientDrawable?

        background?.let {
            if (index == pager!!.currentItem || progressMode && index < pager!!.currentItem) {
                background.setColor(selectedDotColor)
            } else {
                background.setColor(dotsColor)
            }
        }

        elevationItem.setBackgroundCompat(background)
        elevationItem.invalidate()
    }

    override val type get() = BaseDotsIndicator.Type.DEFAULT

    //*********************************************************
    // Users Methods
    //*********************************************************

    @Deprecated("Use setSelectedDotColor() instead", ReplaceWith("setSelectedDotColor()"))
    fun setSelectedPointColor(color: Int) {
        selectedDotColor = color
    }

}