package com.huanli233.bilizepam.ui.widget.views // 或者您希望的任何包名

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextSwitcher

/**
 * 一个自定义的 TextSwitcher，它会在切换子视图时测量两个子视图（当前和下一个），
 * 并将自己的边界设置为能容纳两者的最大宽度和高度。
 * 这解决了在子视图尺寸不同时，使用 wrap_content 会导致动画跳动或裁剪的问题。
 */
class AnimateBoundsTextSwitcher @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : TextSwitcher(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY &&
            MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        var maxWidth = 0
        var maxHeight = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child != null && child.visibility != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
                maxWidth = maxWidth.coerceAtLeast(child.measuredWidth)
                maxHeight = maxHeight.coerceAtLeast(child.measuredHeight)
            }
        }

        maxWidth += paddingLeft + paddingRight
        maxHeight += paddingTop + paddingBottom

        setMeasuredDimension(
            resolveSize(maxWidth, widthMeasureSpec),
            resolveSize(maxHeight, heightMeasureSpec)
        )
    }
}