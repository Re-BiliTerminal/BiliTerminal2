@file:Suppress("unused", "FunctionName")

package com.huanli233.bilizepam.ui.utils.hikage.extension


import android.widget.FrameLayout
import androidx.`annotation`.XmlRes
import com.highcapable.hikage.`annotation`.Hikageable
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.Hikage.Performer
import com.highcapable.hikage.core.base.HikagePerformer
import com.highcapable.hikage.core.base.HikageView
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName
import android.view.ViewGroup.LayoutParams as ViewGroup_LayoutParams
import android.widget.FrameLayout.LayoutParams as FrameLayout_LayoutParams
import com.highcapable.hikage.core.Hikage.LayoutParams as Hikage_LayoutParams
import com.highcapable.hikage.widget.android.widget.FrameLayout
import com.highcapable.hikage.widget.com.huanli233.bilizepam.ui.widget.scalablecontainer.AppScrollView
import com.highcapable.hikage.widget.com.huanli233.bilizepam.ui.widget.wearable.BoxInsetLayout
import com.huanli233.bilizepam.ui.widget.scalablecontainer.AppScrollView
import com.huanli233.bilizepam.ui.widget.wearable.BoxInsetLayout

/**
 * Resolve a match_parent [FrameLayout].
 * @see FrameLayout
 * @see Hikage.Performer.ViewGroup
 * @return [FrameLayout]
 */
@Hikageable
inline fun <reified LP : ViewGroup_LayoutParams> Performer<LP>.FullFrameLayout(
    id: String? = null,
    @XmlRes attr: Int = -1,
    `init`: HikageView<FrameLayout> = {},
    performer: HikagePerformer<FrameLayout_LayoutParams> = {},
): FrameLayout = FrameLayout(matchParent(), id, attr, init, performer)

/**
 * Resolve a match_parent [FrameLayout].
 * @see FrameLayout
 * @see Hikage.Performer.ViewGroup
 * @return [FrameLayout]
 */
@Hikageable
inline fun <reified LP : ViewGroup_LayoutParams> Performer<LP>.AppScrollViewWithBoxInset(
    lparams: Hikage_LayoutParams? = null,
    id: String? = null,
    @XmlRes attr: Int = -1,
    `init`: HikageView<AppScrollView> = {},
    performer: HikagePerformer<BoxInsetLayout.LayoutParams> = {},
): AppScrollView = AppScrollView(matchParent(), id, attr, init) {
    BoxInsetLayout(widthMatchParent()) {
        this.apply(performer)
    }
}
