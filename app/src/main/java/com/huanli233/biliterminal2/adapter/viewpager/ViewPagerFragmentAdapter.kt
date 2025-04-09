package com.huanli233.biliterminal2.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

@Suppress("DEPRECATION")
class ViewPagerFragmentAdapter(
    fm: FragmentManager,
    val fragmentList: List<Fragment>
) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }
}
