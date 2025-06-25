package com.huanli233.bilizepam.ui.activity.recommend

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.activity.base.BaseMenuActivity
import com.huanli233.bilizepam.ui.fragment.recommend.RecommendFragment

class RecommendActivity: BaseMenuActivity() {
    override fun getMenuName(): String = getString(R.string.recommend)

    override fun onCreateFragment(savedInstanceState: Bundle?): Fragment {
        return RecommendFragment()
    }

}