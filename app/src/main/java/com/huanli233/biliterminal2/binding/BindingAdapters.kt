package com.huanli233.biliterminal2.binding

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.huanli233.biliterminal2.util.GlideUtil.loadFace
import com.huanli233.biliterminal2.util.GlideUtil.loadPicture
import com.huanli233.biliterminal2.util.Utils
import com.huanli233.biliterminal2.util.extensions.formatNumber

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

@BindingAdapter("copyable")
fun setCopyable(view: TextView, copyable: Boolean) {
    if (copyable) Utils.copyable(view)
}