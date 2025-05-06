package com.huanli233.biliterminal2.ui.activity.recommend

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.databinding.ActivityCommonRecyclerviewBinding
import com.huanli233.biliterminal2.ui.activity.base.BaseMenuActivity
import com.huanli233.biliterminal2.ui.fragment.recommend.RecommendFragment

class RecommendActivity: BaseMenuActivity() {

    private lateinit var binding: ActivityCommonRecyclerviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pageName = getString(R.string.recommend)
    }

    override fun getMenuName(): String = getString(R.string.recommend)

    override fun onCreateFragment(savedInstanceState: Bundle?): Fragment {
        return RecommendFragment()
    }

}