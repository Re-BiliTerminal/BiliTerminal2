package com.huanli233.bilizepam.utils.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.huanli233.bilizepam.ui.activity.base.BaseActivity

inline fun <T: Fragment> T.putArgument(
    builder: Bundle.() -> Unit
): T = apply {
    arguments = Bundle().apply(builder)
}

val Fragment.baseActivity: BaseActivity?
    get() = activity as? BaseActivity

fun Fragment.requireBaseActivity() = requireActivity() as BaseActivity