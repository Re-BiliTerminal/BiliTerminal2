package com.huanli233.biliterminal2.ui.widget.scalablecontainer

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

class ObservableScrollView(context: Context, attrs: AttributeSet) : ScrollView(context, attrs) {

    interface OnScrollStateChangeListener {
        fun onScrollStateChanged(state: Int, direction: Int)
    }

    companion object {
        const val SCROLL_STATE_IDLE = 0
        const val SCROLL_STATE_SCROLLING = 1

        const val DIRECTION_UP = 1
        const val DIRECTION_DOWN = 2
        const val DIRECTION_NONE = 0
    }

    private var scrollListener: OnScrollStateChangeListener? = null
    private var lastScrollY = 0
    private var currentDirection = DIRECTION_NONE
    private val scrollCheckInterval = 100L
    private val scrollRunnable = Runnable {
        scrollListener?.onScrollStateChanged(SCROLL_STATE_IDLE, currentDirection)
        currentDirection = DIRECTION_NONE
    }

    fun setOnScrollStateChangeListener(listener: OnScrollStateChangeListener) {
        this.scrollListener = listener
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        val deltaY = t - oldt
        currentDirection = when {
            deltaY > 0 -> DIRECTION_DOWN
            deltaY < 0 -> DIRECTION_UP
            else -> DIRECTION_NONE
        }
        handler.removeCallbacks(scrollRunnable)
        handler.postDelayed(scrollRunnable, scrollCheckInterval)
        scrollListener?.onScrollStateChanged(SCROLL_STATE_SCROLLING, currentDirection)
    }
}