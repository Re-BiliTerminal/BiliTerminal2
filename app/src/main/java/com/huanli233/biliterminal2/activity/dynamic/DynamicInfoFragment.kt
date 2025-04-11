package com.huanli233.biliterminal2.activity.dynamic

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.base.BaseActivity
import com.huanli233.biliterminal2.adapter.dynamic.DynamicHolder
import com.huanli233.biliterminal2.databinding.FragmentDynamicInfoBinding
import com.huanli233.biliterminal2.bean.Dynamic
import com.huanli233.biliterminal2.util.*
import java.lang.Exception

class DynamicInfoFragment : Fragment() {
    private var _binding: FragmentDynamicInfoBinding? = null
    private val binding get() = _binding!!

    private var dynamic: Dynamic? = null
    private var onFinishLoad: Runnable? = null

    companion object {
        private const val TAG = "DynamicInfoFragment"

        fun newInstance(id: Long) = DynamicInfoFragment().apply {
            arguments = Bundle().apply {
                putLong("id", id)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getLong("id")?.let { id ->
            try {
                dynamic = TerminalContext.getInstance()
                    .getDynamicById(id)
                    .getValue()
                    .getOrThrow()
            } catch (e: Exception) {
                Log.wtf(TAG, e)
                MsgUtil.showMsg("找不到动态信息QAQ")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDynamicInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupScrollViewLayout()
        loadDynamicContent()
    }

    private fun setupScrollViewLayout() {
        if (Preferences.getBoolean("ui_landscape", false)) {
            val metrics = getRealDisplayMetrics()
            val paddings = metrics.widthPixels / 6
            binding.scrollView.setPadding(paddings, 0, paddings, 0)
        }
    }

    private fun getRealDisplayMetrics(): DisplayMetrics {
        return DisplayMetrics().also { metrics ->
            val windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                windowManager.defaultDisplay.getRealMetrics(metrics)
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getMetrics(metrics)
            }
        }
    }

    private fun loadDynamicContent() {
        ThreadManager.execute {
            if (!isAdded) return@execute

            requireActivity().runOnUiThread {
                dynamic?.let { dynamic ->
                    inflateDynamicViews(dynamic)
                    setupForwardDynamic(dynamic)
                    onFinishLoad?.run()
                }
            }
        }
    }

    private fun inflateDynamicViews(dynamic: Dynamic) {
        val dynamicView = LayoutInflater.from(requireContext())
            .inflate(R.layout.cell_dynamic, binding.scrollView)

        DynamicHolder(dynamicView, requireActivity() as BaseActivity, isChild = false).apply {
            showDynamic(dynamic, requireContext(), isChild = false)
            setupDeleteButton(dynamic)
        }
    }

    private fun DynamicHolder.setupDeleteButton(dynamic: Dynamic) {
        item_dynamic_delete?.apply {
            onLongClickListener = DynamicHolder.getDeleteListener(requireActivity(), dynamic)
            visibility = if (dynamic.canDelete) View.VISIBLE else View.GONE
        }
    }

    private fun setupForwardDynamic(dynamic: Dynamic) {
        dynamic.dynamic_forward?.let { forwardDynamic ->
            binding.cellDynamicChild.visibility = View.VISIBLE
            DynamicHolder(binding.cellDynamicChild, requireActivity() as BaseActivity, true).apply {
                showDynamic(forwardDynamic, requireContext(), isChild = true)
            }
        }
    }

    fun setOnFinishLoad(runnable: Runnable) {
        this.onFinishLoad = runnable
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}