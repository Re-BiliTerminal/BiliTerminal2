package com.huanli233.bilizepam.ui.utils.viewpager2.transformer

import android.view.View
import androidx.viewpager2.widget.ViewPager2

class AlphaTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        if (position < -1) {
            page.setAlpha(0f)
        } else if (position <= 0) {
            page.setAlpha(1 + position)
        } else if (position <= 1) {
            page.setAlpha(1 - position)
        } else {
            page.setAlpha(0f)
        }
    }
}
