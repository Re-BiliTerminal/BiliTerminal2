package com.huanli233.biliterminal2.ui.fragment.recommend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.huanli233.biliterminal2.databinding.LayoutCommonRecyclerviewBinding
import com.huanli233.biliterminal2.ui.fragment.base.BaseFragment

class RecommendFragment: BaseFragment() {

    private lateinit var binding: LayoutCommonRecyclerviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutCommonRecyclerviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO
    }

}