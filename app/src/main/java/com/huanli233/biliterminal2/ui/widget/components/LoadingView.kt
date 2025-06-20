package com.huanli233.biliterminal2.ui.widget.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.databinding.WidgetLoadingViewBinding
import com.huanli233.biliterminal2.utils.extensions.invisible
import com.huanli233.biliterminal2.utils.extensions.visible

enum class LoadingState {
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
    var state: LoadingState = LoadingState.LOADING
        private set

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

    fun loading() {
        state = LoadingState.LOADING
        show()
        binding.loadingImage.setImageResource(R.drawable.loading_2233)
        binding.loadingProgress.visible()
        binding.loadingText.invisible()
    }

    fun error(message: String? = null,) {
        state = LoadingState.ERROR
        show()
        binding.loadingImage.setImageResource(R.drawable.loading_2233_error)
        binding.loadingProgress.invisible()
        if (message != null) {
            binding.loadingText.text = message
            binding.loadingText.visible()
        } else {
            binding.loadingText.invisible()
        }
    }

    fun empty() {
        state = LoadingState.EMPTY
        show()
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

    fun onRetry(retry: () -> Unit) {
        this.onRetry = retry
    }

}