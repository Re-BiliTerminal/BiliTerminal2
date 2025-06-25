package com.huanli233.bilizepam.ui.utils.recyclerview

import android.content.Context
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.ui.widget.recyclerview.CustomGridManager
import com.huanli233.bilizepam.ui.widget.scalablecontainer.ScaleEdgeLayoutManager

val Context.defaultLayoutManager
    get() = if (LocalData.settings.uiSettings.gridListEnabled) {
        CustomGridManager(this, 3)
    } else {
        ScaleEdgeLayoutManager(this)
    }