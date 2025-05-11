package com.huanli233.biliterminal2.ui.widget.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.databinding.WidgetLoadingViewBinding
import com.huanli233.biliterminal2.ui.utils.crossfadeViews
import com.huanli233.biliterminal2.utils.extensions.invisible
import com.huanli233.biliterminal2.utils.extensions.visible

private enum class LoadingState {
    LOADING,
    HIDDEN,
    ERROR,
    EMPTY
}

class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var binding: WidgetLoadingViewBinding =
        WidgetLoadingViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var onRetry: (() -> Unit)? = null
    private var state: LoadingState = LoadingState.LOADING

    init {
        layoutParams = ViewGroup.LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        binding.loadingImage.setOnClickListener {
            if (state == LoadingState.ERROR) {
                onRetry?.invoke()
            }
        }
        if (!isInEditMode) {
            loading()
        }
    }

    fun loading(
        view: View? = null
    ) {
        state = LoadingState.LOADING
        if (view == null) show() else crossFadeShow(view)
        binding.loadingImage.setImageResource(R.drawable.loading_2233)
        binding.loadingProgress.visible()
        binding.loadingText.invisible()
    }

    fun error(
        message: String? = null,
        view: View? = null
    ) {
        state = LoadingState.ERROR
        if (view == null) show() else crossFadeShow(view)
        binding.loadingImage.setImageResource(R.drawable.loading_2233_error)
        binding.loadingProgress.invisible()
        if (message != null) {
            binding.loadingText.text = message
            binding.loadingText.visible()
        } else {
            binding.loadingText.invisible()
        }
    }

    fun empty(
        view: View? = null
    ) {
        state = LoadingState.EMPTY
        if (view == null) show() else crossFadeShow(view)
        binding.loadingImage.setImageResource(R.drawable.loading_2233_empty)
        binding.loadingProgress.invisible()
        binding.loadingText.text = context.getString(R.string.empty_tip)
        binding.loadingText.visible()
    }

    fun show() {
        visible()
    }

    fun hide() {
        state = LoadingState.HIDDEN
        invisible()
    }

    fun crossFadeShow(view: View) {
        crossfadeViews(this, view)
    }

    fun crossFadeHide(view: View) {
        crossfadeViews(view, this)
    }

    fun onRetry(retry: () -> Unit) {
        this.onRetry = retry
    }

}