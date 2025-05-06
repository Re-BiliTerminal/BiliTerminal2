package com.huanli233.biliterminal2.utils.multitype

import com.drakeet.multitype.ItemViewBinder
import com.drakeet.multitype.ItemViewDelegate
import com.drakeet.multitype.MultiTypeAdapter

class MultiTypeRegister(val adapter: MultiTypeAdapter) {
    inline operator fun <reified T : Any> ItemViewDelegate<T, *>.unaryPlus() {
        this@MultiTypeRegister.adapter.register(this)
    }
    inline operator fun <reified T : Any> ItemViewBinder<T, *>.unaryPlus() {
        this@MultiTypeRegister.adapter.register(this)
    }
}

inline fun MultiTypeAdapter.register(
    builder: MultiTypeRegister.() -> Unit
): MultiTypeAdapter = apply {
    MultiTypeRegister(this).builder()
}