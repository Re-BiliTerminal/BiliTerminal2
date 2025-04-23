package com.huanli233.biliterminal2.activity.dynamic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.huanli233.biliterminal2.databinding.FragmentSimpleListBinding

class DynamicInfoFragment : Fragment() {
    private lateinit var binding: FragmentSimpleListBinding

    private val viewModel: DynamicInfoViewModel by activityViewModels()

    companion object {
        fun newInstance() = DynamicInfoFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentSimpleListBinding.inflate(inflater, container, false).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}
