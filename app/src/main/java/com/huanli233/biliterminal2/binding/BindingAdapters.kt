package com.huanli233.biliterminal2.binding

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.util.GlideUtil.loadFace
import com.huanli233.biliterminal2.util.GlideUtil.loadPicture
import com.huanli233.biliterminal2.util.Utils
import com.huanli233.biliterminal2.util.extensions.LoadState
import com.huanli233.biliterminal2.util.extensions.formatNumber
import com.huanli233.biliterminal2.util.extensions.gone
import com.huanli233.biliterminal2.util.removeHtml

@BindingAdapter("loadPicture")
fun loadPicture(view: ImageView, url: String?) {
    url?.let {
        view.loadPicture(
            url, setClick = true
        )
    }
}

@BindingAdapter("loadPicture")
fun loadPicture(view: ImageView, url: List<String>?) {
    url?.let {
        view.loadPicture(*url.toTypedArray(), setClick = true)
    }
}

@BindingAdapter("loadFace")
fun loadFace(view: ImageView, url: String?) {
    url?.let {
        view.loadFace(url)
    }
}

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

@BindingAdapter("removeHtml")
fun removeHtml(view: TextView, content: String?) {
    content?.let {
        view.text = content.removeHtml()
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

@BindingAdapter("copyable")
fun setCopyable(view: TextView, copyable: Boolean) {
    if (copyable) Utils.copyable(view)
}