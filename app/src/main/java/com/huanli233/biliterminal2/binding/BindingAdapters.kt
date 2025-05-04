package com.huanli233.biliterminal2.binding

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.utils.extensions.LoadState
import com.huanli233.biliterminal2.utils.extensions.formatNumber
import com.huanli233.biliterminal2.utils.extensions.gone

@BindingAdapter("formatNumber")
fun formatNumber(view: TextView, number: Long?) {
    number?.let {
        view.text = number.formatNumber()
    }
}

@BindingAdapter("formatNumber")
fun formatNumber(view: TextView, number: Int?) {
    number?.let {
        view.text = number.formatNumber()
    }
}

@BindingAdapter("loadState")
fun loadState(view: ImageView, state: LoadState<*>?) {
    state?.let {
        state.onLoading {
            view.setImageResource(R.mipmap.loading_2233)
        }.onSuccess {
            view.gone()
        }.onError {
            view.setImageResource(R.mipmap.loading_2233_error)
        }
    }
}

@BindingAdapter("visibleIf")
fun visibleIf(view: View, boolean: Boolean?) {
    view.visibility = if (boolean == true) View.VISIBLE else View.GONE
}
