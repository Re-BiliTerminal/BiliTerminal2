@file:Suppress("FunctionName")

package com.huanli233.biliterminal2.ui.utils.hikage.extension

import android.view.ViewGroup
import com.highcapable.hikage.core.base.HikageFactoryBuilder
import com.highcapable.hikage.core.base.HikagePerformer
import com.highcapable.hikage.core.base.Hikageable

inline fun <reified T: ViewGroup.LayoutParams> ViewGroup.Hikage(
    factory: HikageFactoryBuilder.() -> Unit = {},
    performer: HikagePerformer<T>
) = Hikageable<T>(context = context, parent = this, factory = factory, performer = performer)