@file:Suppress("FunctionName")

package com.huanli233.bilizepam.ui.utils.hikage.extension

import android.view.ViewGroup
import com.highcapable.hikage.core.base.HikageFactoryBuilder
import com.highcapable.hikage.core.base.HikagePerformer
import com.highcapable.hikage.core.base.Hikageable

inline fun <reified T: ViewGroup.LayoutParams> ViewGroup.attach(
    factory: HikageFactoryBuilder.() -> Unit = {},
    attachToParent: Boolean = true,
    performer: HikagePerformer<T>
) = Hikageable<T>(context = context, parent = this, attachToParent = attachToParent, factory = factory, performer = performer)