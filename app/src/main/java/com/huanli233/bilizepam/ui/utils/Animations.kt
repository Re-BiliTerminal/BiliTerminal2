package com.huanli233.bilizepam.ui.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.AutoTransition
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import androidx.transition.TransitionValues
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import com.google.android.material.transition.MaterialFade
import com.huanli233.bilizepam.data.setting.LocalData

inline fun playAnimation(block: () -> Unit) {
    if (LocalData.settings.theme.animationsEnabled) {
        block()
    }
}

class TransitionTargetBuilder(private val transition: Transition) {
    operator fun View.unaryPlus() = add(this)
    operator fun View.unaryMinus() = remove(this)
    fun add(view: View) = transition.addTarget(view)

    fun add(id: Int) = transition.addTarget(id)
    fun add(name: String) = transition.addTarget(name)

    fun remove(id: Int) = transition.removeTarget(id)
    fun remove(view: View) = transition.removeTarget(view)
    fun remove(name: String) = transition.removeTarget(name)
}

inline fun Transition.addTargets(builder: TransitionTargetBuilder.() -> Unit) = apply {
    builder(TransitionTargetBuilder(this))
}

class TransitionSetBuilder(private val transitionSet: TransitionSet) {
    operator fun Transition.unaryPlus() = add(this)
    fun add(transition: Transition) = transitionSet.addTransition(transition)
}

inline fun buildTransitionSet(builder: TransitionSetBuilder.() -> Unit): TransitionSet {
    val transitionSet = TransitionSet()
    TransitionSetBuilder(transitionSet).builder()
    return transitionSet
}

open class TransitionListener: Transition.TransitionListener {
    override fun onTransitionStart(transition: Transition) {}
    override fun onTransitionEnd(transition: Transition) {}
    override fun onTransitionCancel(transition: Transition) {}
    override fun onTransitionPause(transition: Transition) {}
    override fun onTransitionResume(transition: Transition) {}
}

interface TransitionListenerBuilder {
    fun onStart(action: (transition: Transition) -> Unit)
    fun onEnd(action: (transition: Transition) -> Unit)
    fun onCancel(action: (transition: Transition) -> Unit)
    fun onPause(action: (transition: Transition) -> Unit)
    fun onResume(action: (transition: Transition) -> Unit)

    fun build(): Transition.TransitionListener
}

@PublishedApi
internal class TransitionListenerBuilderImpl : TransitionListenerBuilder {
    internal var onTransitionStart: ((transition: Transition) -> Unit)? = null
    internal var onTransitionEnd: ((transition: Transition) -> Unit)? = null
    internal var onTransitionCancel: ((transition: Transition) -> Unit)? = null
    internal var onTransitionPause: ((transition: Transition) -> Unit)? = null
    internal var onTransitionResume: ((transition: Transition) -> Unit)? = null
    override fun onStart(action: (transition: Transition) -> Unit) {
        onTransitionStart = action
    }
    override fun onEnd(action: (transition: Transition) -> Unit) {
        onTransitionEnd = action
    }
    override fun onCancel(action: (transition: Transition) -> Unit) {
        onTransitionCancel = action
    }
    override fun onPause(action: (transition: Transition) -> Unit) {
        onTransitionPause = action
    }
    override fun onResume(action: (transition: Transition) -> Unit) {
        onTransitionResume = action
    }

    override fun build(): Transition.TransitionListener = object : TransitionListener() {
        override fun onTransitionStart(transition: Transition) {
            onTransitionStart?.invoke(transition)
        }

        override fun onTransitionEnd(transition: Transition) {
            onTransitionEnd?.invoke(transition)
            transition.removeListener(this)
        }

        override fun onTransitionCancel(transition: Transition) {
            onTransitionCancel?.invoke(transition)
            transition.removeListener(this)
        }

        override fun onTransitionPause(transition: Transition) {
            onTransitionPause?.invoke(transition)
        }

        override fun onTransitionResume(transition: Transition) {
            onTransitionResume?.invoke(transition)
        }
    }
}

inline fun Transition.listen(
    builder: TransitionListenerBuilder.() -> Unit = {}
) = apply {
    val listener = TransitionListenerBuilderImpl().apply(builder).build()
    this.addListener(listener)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Transition.duration(duration: Long?) = apply { duration?.let { this.duration = duration } }

@Suppress("NOTHING_TO_INLINE")
inline fun ViewGroup.beginDelayedTransition(
    transition: Transition = AutoTransition()
) {
    TransitionManager.beginDelayedTransition(this, transition)
}

@Suppress("NOTHING_TO_INLINE")
inline fun ViewGroup.beginDelayedMaterialFade(
    duration: Long? = null,
    targetBuilder: (TransitionTargetBuilder.() -> Unit) = {}
) {
    beginDelayedTransition(
        MaterialFade()
            .setInterpolator(FastOutSlowInInterpolator())
            .duration(duration)
            .addTargets(targetBuilder)
    )
}

@Suppress("NOTHING_TO_INLINE")
inline fun ViewGroup.beginDelayedFade(
    duration: Long? = 300,
    targetBuilder: (TransitionTargetBuilder.() -> Unit) = {}
) {
    beginDelayedTransition(
        Fade()
            .setInterpolator(FastOutSlowInInterpolator())
            .duration(duration)
            .addTargets(targetBuilder)
    )
}

fun TextView.animateTextChange(
    newText: CharSequence,
    duration: Long? = 300,
    onTransitionEnd: (() -> Unit)? = null
) {
    if (!LocalData.settings.theme.animationsEnabled) {
        text = newText
        onTransitionEnd?.invoke()
        return
    }
    animate()?.cancel()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && newText != this.text) {
        val parent = parent as? ViewGroup
            ?: run {
                text = newText
                onTransitionEnd?.invoke()
                return
            }

        val transition = buildTransitionSet {
            +AutoTransition()
            +TextFadeTransition()
        }.setInterpolator(FastOutSlowInInterpolator()).duration(duration)
        transition.listen {
            onEnd { onTransitionEnd?.invoke() }
        }
        parent.beginDelayedTransition(transition)
    }
    this.text = newText
}

class TextFadeTransition : Transition() {

    private companion object {
        private const val PROPNAME_TEXT = "com.huanli233.textfade:text"
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    private fun captureValues(transitionValues: TransitionValues) {
        (transitionValues.view as? TextView)?.let { textView ->
            transitionValues.values[PROPNAME_TEXT] = textView.text
        }
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues == null || endValues == null ||
            startValues.view !is TextView || endValues.view !is TextView
        ) {
            return null
        }

        val startTextView = startValues.view as TextView
        val endTextView = endValues.view as TextView

        val startText = startValues.values[PROPNAME_TEXT] as? CharSequence
        val endText = endValues.values[PROPNAME_TEXT] as? CharSequence

        if (startText != null && endText != null && startText != endText) {
            val fadeOut = ObjectAnimator.ofFloat(startTextView, "alpha", 1f, 0.3f)
            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    endTextView.text = endText
                }
            })
            fadeOut.interpolator = FastOutSlowInInterpolator()
            fadeOut.setDuration(duration)

            val fadeIn = ObjectAnimator.ofFloat(endTextView, "alpha", 0.3f, 1f)
            fadeIn.interpolator = FastOutSlowInInterpolator()
            fadeIn.setDuration(duration)

            return AnimatorSet().apply {
                playTogether(fadeOut, fadeIn)
            }
        }
        return null
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