package com.ethanhua.skeleton

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.huanli233.skeleton.R

abstract class SkeletonBuilder(internal val mView: View) {
    internal open var mSkeletonLayoutResID = 0
    internal open var mShimmer = true
    internal open var mUseAlpha = true
    internal open var mShimmerBaseAlpha = 0.4f
    internal open var mShimmerAlpha = 1f
    internal open var mShimmerColor = ContextCompat.getColor(mView.context, R.color.shimmer_color)
    internal open var mShimmerBaseColor = ContextCompat.getColor(mView.context, R.color.shimmer_base_color)
    internal open var mShimmerDuration = 1000L
    internal open var mShimmerDirection = Direction.LEFT_TO_RIGHT
    internal open var mShimmerAngle = 30
    internal open var mShimmerShape = Shape.LINEAR
    internal open var mRepeatCount = -1
    internal open var mRepeatDelay = 0L
    internal open var mRepeatMode = RepeatMode.RESTART

    /**
     * @param skeletonLayoutResID the loading skeleton layoutResID
     */
    open fun load(@LayoutRes skeletonLayoutResID: Int): SkeletonBuilder {
        mSkeletonLayoutResID = skeletonLayoutResID
        return this
    }

    open fun baseAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): SkeletonBuilder {
        mShimmerBaseAlpha = alpha
        return this
    }

    open fun alpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): SkeletonBuilder {
        mShimmerAlpha = alpha
        return this
    }

    open fun baseColor(@ColorRes shimmerBaseColor: Int): SkeletonBuilder {
        mShimmerBaseColor = ContextCompat.getColor(mView.context, shimmerBaseColor)
        return this
    }

    /**
     * @param shimmerColor the shimmer color
     */
    open fun color(@ColorRes shimmerColor: Int): SkeletonBuilder {
        mShimmerColor = ContextCompat.getColor(mView.context, shimmerColor)
        return this
    }

    /**
     * @param shimmer whether show shimmer animation
     */
    open fun shimmer(shimmer: Boolean): SkeletonBuilder {
        mShimmer = shimmer
        return this
    }

    /**
     * the duration of the animation , the time it will take for the highlight to move from one end of the layout
     * to the other.
     *
     * @param shimmerDuration Duration of the shimmer animation, in milliseconds
     */
    open fun duration(shimmerDuration: Long): SkeletonBuilder {
        mShimmerDuration = shimmerDuration
        return this
    }

    /**
     * @param shimmerAngle the angle of the shimmer effect in clockwise direction in degrees.
     */
    open fun angle(@IntRange(from = 0, to = 30) shimmerAngle: Int): SkeletonBuilder {
        mShimmerAngle = shimmerAngle
        return this
    }

    open fun direction(direction: Direction): SkeletonBuilder {
        mShimmerDirection = direction
        return this
    }

    open fun setUseAlpha(use: Boolean): SkeletonBuilder {
        mUseAlpha = use
        return this
    }

    open fun shape(shape: Shape): SkeletonBuilder {
        mShimmerShape = shape
        return this
    }

    open fun repeatCount(repeatCount: Int): SkeletonBuilder {
        mRepeatCount = repeatCount
        return this
    }

    open fun repeatMode(repeatMode: RepeatMode): SkeletonBuilder {
        mRepeatMode = repeatMode
        return this
    }

    open fun repeatDelay(repeatDelay: Long): SkeletonBuilder {
        mRepeatDelay = repeatDelay
        return this
    }

    abstract fun build(): SkeletonScreen
}