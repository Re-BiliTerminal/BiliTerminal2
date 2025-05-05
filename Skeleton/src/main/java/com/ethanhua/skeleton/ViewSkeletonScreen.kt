package com.ethanhua.skeleton

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.huanli233.skeleton.R

/**
 * Created by ethanhua on 2017/7/29.
 */
class ViewSkeletonScreen private constructor(builder: Builder) : SkeletonScreen {
    private val mViewReplacer = ViewReplacer(builder.mView)
    private val mActualView = builder.mView
    private val mUseAlpha = builder.mUseAlpha
    private val mShimmerAngle = builder.mShimmerAngle
    private val mSkeletonResID = builder.mSkeletonLayoutResID
    private val mShimmerColor = builder.mShimmerColor
    private val mShimmerBaseColor = builder.mShimmerBaseColor
    private val mShimmerAlpha = builder.mShimmerAlpha
    private val mShimmerBaseAlpha = builder.mShimmerBaseAlpha
    private val mShimmer = builder.mShimmer
    private val mShimmerDuration = builder.mShimmerDuration
    private val mShimmerDirection = builder.mShimmerDirection
    private val mShimmerShape = builder.mShimmerShape
    private val mRepeatCount = builder.mRepeatCount
    private val mRepeatDelay = builder.mRepeatDelay
    private val mRepeatMode = builder.mRepeatMode

    private fun generateShimmerContainerLayout(parentView: ViewGroup): ShimmerFrameLayout {
        val shimmerLayout = LayoutInflater.from(mActualView.context).inflate(R.layout.layout_shimmer, parentView, false) as ShimmerFrameLayout
        val shimmer = if (mUseAlpha) {
            Shimmer.AlphaHighlightBuilder()
                    .setBaseAlpha(mShimmerBaseAlpha)
                    .setHighlightAlpha(mShimmerAlpha)
        } else {
            Shimmer.ColorHighlightBuilder()
                    .setBaseColor(mShimmerBaseColor)
                    .setHighlightColor(mShimmerColor)
        }.apply {
            setTilt(mShimmerAngle.toFloat())
            setShape(mShimmerShape.value)
            setDirection(mShimmerDirection.value)
            setDuration(mShimmerDuration)
            setRepeatCount(mRepeatCount)
            setRepeatDelay(mRepeatDelay)
            setRepeatMode(mRepeatMode.value)
        }.build()
        shimmerLayout.setShimmer(shimmer)
        val innerView = LayoutInflater.from(mActualView.context).inflate(mSkeletonResID, shimmerLayout, false)
        innerView.layoutParams?.let {
            shimmerLayout.layoutParams = it
        }
        shimmerLayout.addView(innerView)
        shimmerLayout.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                shimmerLayout.startShimmer()
            }

            override fun onViewDetachedFromWindow(v: View) {
                shimmerLayout.stopShimmer()
            }
        })
        shimmerLayout.startShimmer()
        return shimmerLayout
    }

    private fun generateSkeletonLoadingView(): View? {
        val viewParent = mActualView.parent
        if (viewParent == null) {
            Log.e(TAG, "the source view have not attach to any view")
            return null
        }
        val parentView = viewParent as ViewGroup
        return if (mShimmer) {
            generateShimmerContainerLayout(parentView)
        } else LayoutInflater.from(mActualView.context).inflate(mSkeletonResID, parentView, false)
    }

    override fun show(): ViewSkeletonScreen {
        val skeletonLoadingView = generateSkeletonLoadingView()
        if (skeletonLoadingView != null) {
            mViewReplacer.replace(skeletonLoadingView)
        }
        return this
    }

    override fun hide() {
        (mViewReplacer.targetView as? ShimmerFrameLayout)?.stopShimmer()
        mViewReplacer.restore()
    }

    class Builder(mView: View) : SkeletonBuilder(mView) {
        override fun build(): ViewSkeletonScreen {
            return ViewSkeletonScreen(this)
        }
    }

    companion object {
        private val TAG = ViewSkeletonScreen::class.java.name
    }
}