package com.huanli233.biliterminal2.ui.fragment.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.huanli233.biliterminal2.databinding.FragmentSetupWelcomeBinding
import com.huanli233.biliterminal2.ui.fragment.base.BaseFragment

class WelcomeFragment: BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentSetupWelcomeBinding.inflate(inflater, container, false).root
    }

}