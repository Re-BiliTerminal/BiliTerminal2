package com.huanli233.biliterminal2.ui.utils.recyclerview

import android.content.Context
import com.huanli233.biliterminal2.data.setting.DataStore
import com.huanli233.biliterminal2.ui.widget.recyclerview.CustomGridManager
import com.huanli233.biliterminal2.ui.widget.recyclerview.CustomLinearManager
import com.huanli233.biliterminal2.ui.widget.scalablecontainer.ScaleEdgeLayoutManager
import com.huanli233.biliterminal2.ui.widget.wearable.CustomScrollingLayoutCallback
import com.huanli233.biliterminal2.ui.widget.wearable.WearableLinearLayoutManager
import com.huanli233.biliterminal2.utils.isRound

val Context.defaultLayoutManager
    get() = if (DataStore.appSettings.gridListEnabled) {
        CustomGridManager(this, 3)
    } else {
        ScaleEdgeLayoutManager(this)
    }