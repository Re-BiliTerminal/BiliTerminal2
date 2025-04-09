package com.huanli233.biliterminal2.util.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.huanli233.biliterminal2.adapter.viewpager.ViewPagerFragmentAdapter

data class HeaderView<T>(
    val data: T
)
data class FooterView<T>(
    val data: T
)

@Suppress("NOTHING_TO_INLINE")
inline fun ViewPager.setupFragments(
    fragmentManager: FragmentManager,
    vararg fragments: Fragment
) = ViewPagerFragmentAdapter(fragmentManager, fragments.toList()).also {
    adapter = it
}