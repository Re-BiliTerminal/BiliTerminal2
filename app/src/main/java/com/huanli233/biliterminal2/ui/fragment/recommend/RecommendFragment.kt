package com.huanli233.biliterminal2.ui.fragment.recommend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import com.huanli233.biliterminal2.databinding.LayoutCommonRecyclerviewBinding
import com.huanli233.biliterminal2.databinding.LayoutCommonSwiperefreshRecyclerviewBinding
import com.huanli233.biliterminal2.ui.fragment.base.BaseFragment
import com.huanli233.biliterminal2.ui.fragment.base.BaseMenuFragment
import com.huanli233.biliterminal2.ui.recyclerview.adapters.VideoPagingAdapter
import com.huanli233.biliterminal2.ui.utils.loadstate.LoadStateAdapter
import com.huanli233.biliterminal2.ui.utils.recyclerview.defaultLayoutManager
import com.huanli233.biliterminal2.utils.MsgUtil
import com.huanli233.biliterminal2.utils.extensions.invisible
import com.huanli233.biliterminal2.utils.extensions.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecommendFragment: BaseMenuFragment() {

    private lateinit var binding: LayoutCommonSwiperefreshRecyclerviewBinding
    private val viewModel: RecommendViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutCommonSwiperefreshRecyclerviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = requireContext().defaultLayoutManager
        val pagingAdapter = VideoPagingAdapter()
        val loadStateAdapter = LoadStateAdapter {
            pagingAdapter.retry()
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            pagingAdapter.refresh()
            binding.swipeRefreshLayout.isRefreshing = false
        }
        binding.recyclerView.adapter = pagingAdapter.withLoadStateFooter(loadStateAdapter)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.videos.collectLatest(pagingAdapter::submitData)
            }
        }

        binding.loadingView.onRetry(pagingAdapter::retry)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                pagingAdapter.loadStateFlow.collect { loadState ->
                    if (loadState.refresh is LoadState.NotLoading && pagingAdapter.itemCount == 0 || loadState.refresh is LoadState.Error) {
                        binding.recyclerView.invisible()
                        binding.loadingView.error()
                    } else if (loadState.source.refresh is LoadState.Loading) {
                        binding.recyclerView.invisible()
                        binding.loadingView.loading()
                    } else {
                        binding.recyclerView.visible()
                        binding.loadingView.hide()
                    }
                }
            }
        }
    }

}