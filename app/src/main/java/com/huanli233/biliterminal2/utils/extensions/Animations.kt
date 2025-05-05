package com.huanli233.biliterminal2.utils.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.UserPreferences

fun TextView.crossFadeSetText(
    text: CharSequence
) {
    animate().cancel()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && UserPreferences.animationsEnabled.get() && text != this.text) {
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

/* Skeleton */
fun View.showSkeleton(layout: Int): SkeletonScreen? {
    return if (UserPreferences.animationsEnabled.get()) {
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