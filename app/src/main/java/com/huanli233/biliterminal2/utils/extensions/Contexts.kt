package com.huanli233.biliterminal2.utils.extensions

import android.content.Context
import com.huanli233.biliterminal2.ui.activity.base.BaseActivity

val Context.originalConfigContext
    get() = (this as? BaseActivity)?.originalViewContext ?: this