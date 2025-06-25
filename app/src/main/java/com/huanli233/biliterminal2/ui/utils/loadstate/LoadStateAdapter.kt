package com.huanli233.biliterminal2.ui.utils.loadstate

import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import com.highcapable.betterandroid.ui.extension.view.updatePadding
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.base.Hikageable
import com.highcapable.hikage.extension.widget.textRes
import com.highcapable.hikage.extension.widget.vertical
import com.highcapable.hikage.widget.android.widget.Button
import com.highcapable.hikage.widget.android.widget.LinearLayout
import com.highcapable.hikage.widget.android.widget.ProgressBar
import com.highcapable.hikage.widget.android.widget.TextView
import com.highcapable.hikage.widget.com.google.android.material.progressindicator.CircularProgressIndicator
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.utils.extensions.gone
import com.huanli233.biliterminal2.utils.extensions.ifInEditMode
import com.huanli233.biliterminal2.utils.extensions.visible
import com.huanli233.hikage.recyclerview.HikageLoadStateAdapter
import splitties.views.gravityCenter

class LoadStateAdapter(
    private val retry: (() -> Unit)? = null
): HikageLoadStateAdapter() {

    override fun createView(loadState: LoadState): Hikage.Delegate<*> = Hikageable {
        LinearLayout(
            lparams = widthMatchParent(),
            init = {
                vertical()
                setPadding(8.dp)
                gravity = gravityCenter
            }
        ) {
            CircularProgressIndicator(
                id = "load_state_progress_indicator"
            ) {
                isIndeterminate = true
                gone()
            }
            TextView(
                id = "load_state_error_msg"
            ) {
                gone()
                textRes = R.string.load_failed
                gravity = gravityCenter
                ifInEditMode {
                    visible()
                }
            }
            Button(
                id = "load_state_retry_btn"
            ) {
                gone()
                textRes = R.string.retry
                ifInEditMode {
                    visible()
                }
            }
        }
    }

    override fun bindView(hikage: Hikage, loadState: LoadState) {
        if (loadState is LoadState.Error) {
            hikage.get<TextView>("load_state_error_msg").text = loadState.error.localizedMessage
        }
        hikage.get<ProgressBar>("load_state_progress_indicator").isVisible = loadState is LoadState.Loading
        hikage.get<Button>("load_state_retry_btn").isVisible = loadState is LoadState.Error && retry != null
        hikage.get<TextView>("load_state_error_msg").isVisible = loadState is LoadState.Error
    }
}