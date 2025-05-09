package com.huanli233.biliterminal2.ui.widget.pager

import androidx.viewpager2.widget.ViewPager2

fun ViewPager2.setupWithIndicator(pageIndicatorView: PageIndicatorView) {
    pageIndicatorView.setPager(this)
}