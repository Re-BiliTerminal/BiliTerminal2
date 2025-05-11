package com.huanli233.biliterminal2.ui.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.view.View
import android.view.animation.Interpolator
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import com.huanli233.biliterminal2.data.setting.LocalData
import androidx.core.view.isVisible

fun TextView.crossFadeSetText(
    text: CharSequence
) {
    animate().cancel()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && LocalData.settings.theme.animationsEnabled && text != this.text) {
        animate()
            .alpha(0.7f)
            .setDuration(150)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    this@crossFadeSetText.text = text
                    animate()
                        .alpha(1f)
                        .setDuration(150)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .setListener(null)
                        .start()
                }
            })
            .start()
    } else {
        this.text = text
    }
}

fun crossfadeViews(
    viewToShow: View,
    viewToHide: View,
    duration: Long = 300
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && LocalData.settings.theme.animationsEnabled && viewToHide.isVisible && viewToShow.visibility != View.VISIBLE) {
        viewToHide.animate().cancel()
        viewToShow.animate().cancel()

        val interpolator: Interpolator = FastOutSlowInInterpolator()
        val originalHideAlpha = viewToHide.alpha
        val originalShowAlpha = viewToShow.alpha

        viewToShow.apply {
            alpha = 0f
            visibility = View.VISIBLE
        }

        viewToHide.animate()
            .alpha(0f)
            .setDuration(duration / 2)
            .setInterpolator(interpolator)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: Animator) {
                    viewToHide.visibility = View.INVISIBLE
                    viewToHide.alpha = originalHideAlpha
                    viewToShow.alpha = originalShowAlpha
                }
            })
            .withEndAction {
                viewToHide.visibility = View.INVISIBLE
                viewToHide.alpha = originalHideAlpha

                viewToShow.animate()
                    .alpha(originalShowAlpha)
                    .setDuration(duration / 2)
                    .setInterpolator(interpolator)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationCancel(animation: Animator) {
                            viewToShow.alpha = originalShowAlpha
                        }
                    })
                    .start()
            }
            .start()
    } else {
        viewToHide.animate().cancel()
        viewToShow.animate().cancel()
        viewToShow.visibility = View.VISIBLE
        viewToHide.visibility = View.INVISIBLE
    }
}

/* Skeleton */
fun View.showSkeleton(layout: Int): SkeletonScreen? {
    return if (LocalData.settings.theme.animationsEnabled) {
        Skeleton
            .bind(this)
            .load(layout)
            .duration(1000)
            .shimmer(true)
            .angle(30)
            .build()
            .show()
    } else {
        null
    }
}