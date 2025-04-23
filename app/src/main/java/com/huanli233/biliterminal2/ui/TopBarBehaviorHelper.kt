package com.huanli233.biliterminal2.ui

import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ScrollView
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.ui.widget.scalablecontainer.ObservableRecyclerView
import com.huanli233.biliterminal2.ui.widget.scalablecontainer.ObservableScrollView
import com.huanli233.biliterminal2.util.extensions.invisible
import com.huanli233.biliterminal2.util.extensions.visible
import androidx.core.view.isNotEmpty

class TopBarBehaviorHelper(
    val topBar: View
) {
    private var isHidden = false
    private val scrollSlop = 25

    private val animDuration = 300L
    private val interpolator = DecelerateInterpolator(1.5f)
    private val topBarHeight by lazy { topBar.height }
    private var isScrolling = false
    private val handler = Handler(Looper.getMainLooper())

    fun installFor(scrollableView: View) {
        when (scrollableView) {
            is RecyclerView -> setupForRecyclerView(scrollableView)
            else -> setupForScrollView(scrollableView)
        }
    }

    val runnable: Runnable = Runnable {
        showTopBar()
    }

    private fun setupForRecyclerView(
        scrollableView: View
    ) {
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
            addScrollCallback(ScrollCallBack(scrollableView))
        }
    }

    private fun setupForScrollView(
        scrollableView: View
    ) {
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
        val callback = ScrollCallBack(scrollableView)
        (scrollableView as? ObservableScrollView)?.setOnScrollStateChangeListener(
            object : ObservableScrollView.OnScrollStateChangeListener {
                override fun onScrollStateChanged(state: Int, direction: Int) {
                    if (state == ObservableScrollView.SCROLL_STATE_SCROLLING) {
                        callback.onScrolling()
                    } else if (state == ObservableScrollView.SCROLL_STATE_IDLE) {
                        callback.onScrollIdle(direction == ObservableScrollView.DIRECTION_UP)
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

    inner class ScrollCallBack(
        private val scrollableView: View
    ) : ObservableRecyclerView.OnScrollCallback {
        override fun onScrollIdle(isUp: Boolean) {
            val recyclerView = scrollableView as? RecyclerView
            val scrollView = scrollableView as? ScrollView
            val canScrollUp = recyclerView?.canScrollVertically(1) ?: scrollView?.canScrollVertically(1)
            val canScrollDown = recyclerView?.canScrollVertically(-1) ?: scrollView?.canScrollVertically(-1)
            if (isUp &&
                (canScrollDown != true || canScrollUp == true) &&
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

}

interface TopBarBinder {
    fun bindToTopBar(scrollableView: View)
}

private val topBarBehaviorHelperMap = mutableMapOf<View, TopBarBehaviorHelper>()

fun createOrGet(topBar: View): TopBarBehaviorHelper = topBarBehaviorHelperMap.getOrPut(topBar) {
    TopBarBehaviorHelper(topBar)
}

fun View.bindTopBar(
    topBar: View
): TopBarBehaviorHelper = createOrGet(topBar).apply {
    installFor(this@bindTopBar)
}