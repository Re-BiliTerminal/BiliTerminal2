package com.huanli233.bilizepam.utils.extensions

import android.content.Context
import com.huanli233.bilizepam.ui.activity.base.BaseActivity

val Context.originalConfigContext
    get() = (this as? BaseActivity)?.originalViewContext ?: this