package com.huanli233.biliterminal2.ui.utils.loadstate

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.R

class LoadStateViewHolder(
    parent: ViewGroup,
    private val retry: (() -> Unit)? = null
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.item_list_load_state, parent, false)
) {
    private val progressBar: ProgressBar = itemView.findViewById(R.id.load_state_progress_bar)
    private val errorMsg: TextView = itemView.findViewById(R.id.load_state_error_msg)
    private val retryButton: Button = itemView.findViewById(R.id.load_state_retry_button)

    init {
        retryButton.setOnClickListener { retry?.invoke() }
    }

    fun bind(loadState: androidx.paging.LoadState) {
        if (loadState is androidx.paging.LoadState.Error) {
            errorMsg.text = loadState.error.localizedMessage
        }

        progressBar.isVisible = loadState is androidx.paging.LoadState.Loading
        retryButton.isVisible = loadState is androidx.paging.LoadState.Error && retry != null
        errorMsg.isVisible = loadState is androidx.paging.LoadState.Error
    }
}