package com.huanli233.biliterminal2.ui

import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.ui.widget.scalablecontainer.ObservableRecyclerView
import com.huanli233.biliterminal2.ui.widget.scalablecontainer.ObservableScrollView
import com.huanli233.biliterminal2.util.extensions.invisible
import com.huanli233.biliterminal2.util.extensions.visible
import androidx.core.view.isNotEmpty

class TopBarBehaviorHelper(
    val topBar: View,
    val scrollableView: View
) {
    private var isHidden = false
    private var lastScrollY = 0
    private val scrollSlop = 25

    private val animDuration = 300L
    private val interpolator = DecelerateInterpolator(1.5f)
    private val topBarHeight by lazy { topBar.height }
    private var isScrolling = false
    private val handler = Handler(Looper.getMainLooper())

    fun install() {
        when (scrollableView) {
            is RecyclerView -> setupForRecyclerView()
            else -> setupForScrollView()
        }
    }

    val runnable: Runnable = Runnable {
        showTopBar()
    }

    val scrollCallback = object : ObservableRecyclerView.OnScrollCallback {
        override fun onScrollIdle(isUp: Boolean) {
            val recyclerView = scrollableView as RecyclerView
            val canScrollUp = recyclerView.canScrollVertically(1)
            val canScrollDown = recyclerView.canScrollVertically(-1)
            if (isUp &&
                (!canScrollDown || canScrollUp) &&
                !isHidden) {
                handler.removeCallbacks(runnable)
                showTopBar()
            } else {
                handler.postDelayed(runnable, animDuration)
            }
        }

        override fun onScrolling() {
            if (!isScrolling) {
                isScrolling = true
                hideTopBar()
            }
        }
    }

    private fun setupForRecyclerView() {
        (scrollableView as? ObservableRecyclerView)?.apply {
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    recyclerView: RecyclerView,
                    state: RecyclerView.State
                ) {
                    if (recyclerView.getChildLayoutPosition(view) == 0) {
                        outRect.top = topBarHeight
                    }
                }
            })
            addScrollCallback(scrollCallback)
        }
    }

    private fun setupForScrollView() {
        (scrollableView as? ViewGroup)?.takeIf { it.isNotEmpty() }?.let { scrollView ->
            val contentView = scrollView.getChildAt(0)

            val newPaddingTop = contentView.paddingTop + topBarHeight
            contentView.setPadding(
                contentView.paddingLeft,
                newPaddingTop,
                contentView.paddingRight,
                contentView.paddingBottom
            )
        }
        (scrollableView as? ObservableScrollView)?.setOnScrollStateChangeListener(
            object : ObservableScrollView.OnScrollStateChangeListener {
                override fun onScrollStateChanged(state: Int, direction: Int) {
                    if (state == ObservableScrollView.SCROLL_STATE_SCROLLING) {
                        scrollCallback.onScrolling()
                    } else if (state == ObservableScrollView.SCROLL_STATE_IDLE) {
                        scrollCallback.onScrollIdle(direction == ObservableScrollView.DIRECTION_UP)
                    }
                }
            }
        )
    }

    private fun handleScroll(deltaY: Int) {
        when {
            deltaY > scrollSlop && !isHidden -> hideTopBar()
            deltaY < -scrollSlop && isHidden -> showTopBar()
        }
    }

    fun hideTopBar() {
        isHidden = true
        topBar.translationY = 0.0f
        ViewCompat.animate(topBar).cancel()
        ViewCompat.animate(topBar)
            .alpha(0.0f)
            .translationY(topBarHeight.toFloat())
            .setListener(
                object : ViewPropertyAnimatorListener {
                    override fun onAnimationStart(view: View) = Unit

                    override fun onAnimationEnd(view: View) {
                        view.invisible()
                    }

                    override fun onAnimationCancel(view: View) = Unit
                }
            )
            .setInterpolator(interpolator)
            .setDuration(animDuration)
            .start()
    }

    fun showTopBar() {
        if (topBar.visibility != View.VISIBLE) {
            isHidden = false
            topBar.translationY = topBarHeight.toFloat()
            ViewCompat.animate(topBar).cancel()
            ViewCompat.animate(topBar)
                .alpha(1.0f)
                .translationY(0.0f)
                .setListener(
                    object : ViewPropertyAnimatorListener {
                        override fun onAnimationStart(view: View) {
                            view.visible()
                        }

                        override fun onAnimationEnd(view: View) = Unit

                        override fun onAnimationCancel(view: View) = Unit
                    }
                )
                .setInterpolator(interpolator)
                .setDuration(animDuration)
                .start()
        }
    }

}

fun View.bindTopBar(
    topBar: View
): TopBarBehaviorHelper = TopBarBehaviorHelper(this, topBar).apply {
    install()
}