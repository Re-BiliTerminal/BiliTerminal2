package com.huanli233.biliterminal2.ui.activity.setting

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.view.updateMargins
import com.highcapable.betterandroid.ui.extension.view.updateMargins
import com.highcapable.betterandroid.ui.extension.view.updatePadding
import com.highcapable.hikage.extension.setContentView
import com.highcapable.hikage.extension.widget.bottomToParent
import com.highcapable.hikage.extension.widget.endToParent
import com.highcapable.hikage.extension.widget.onClick
import com.highcapable.hikage.extension.widget.startToParent
import com.highcapable.hikage.extension.widget.textRes
import com.highcapable.hikage.extension.widget.topToParent
import com.highcapable.hikage.widget.android.widget.ImageView
import com.highcapable.hikage.widget.android.widget.LinearLayout
import com.highcapable.hikage.widget.android.widget.TextView
import com.highcapable.hikage.widget.androidx.constraintlayout.widget.ConstraintLayout
import com.highcapable.hikage.widget.com.google.android.material.button.MaterialButton
import com.highcapable.hikage.widget.com.google.android.material.card.MaterialCardView
import com.highcapable.hikage.widget.com.google.android.material.chip.Chip
import com.highcapable.hikage.widget.com.google.android.material.divider.MaterialDivider
import com.highcapable.hikage.widget.com.google.android.material.textview.MaterialTextView
import com.highcapable.hikage.widget.com.huanli233.biliterminal2.ui.widget.components.TopBar
import com.highcapable.hikage.widget.com.huanli233.biliterminal2.ui.widget.scalablecontainer.AppScrollView
import com.highcapable.hikage.widget.com.huanli233.biliterminal2.ui.widget.views.MarqueeTextView
import com.highcapable.hikage.widget.com.huanli233.biliterminal2.ui.widget.wearable.BoxInsetLayout
import com.huanli233.biliterminal2.BuildConfig
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.ui.activity.base.BaseActivity
import com.huanli233.biliterminal2.ui.dialog.Dialogs
import com.huanli233.biliterminal2.ui.utils.hikage.extension.boldTypeFace
import com.huanli233.biliterminal2.utils.extensions.editModeText
import com.huanli233.biliterminal2.utils.extensions.startActivityOrMsg
import com.huanli233.biliterminal2.utils.extensions.updatePaddingRelativeCompat
import splitties.views.cardview.contentPadding

const val URL_QQ_CHANNEL = "https://pd.qq.com/s/fdti2l61d"
const val ID_QQ_GROUP = "719041250"

class AboutActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView {
            ConstraintLayout(matchParent()) {
                TopBar(
                    lparams = widthMatchParent {
                        topToParent()
                    },
                    init = { setTitle(stringResource(R.string.about)) }
                )

                AppScrollView(
                    lparams = widthMatchParent(0) {
                        topToBottom = R.id.top_bar
                        bottomToParent()
                    }
                ) {
                    BoxInsetLayout(widthMatchParent()) {
                        ConstraintLayout(
                            lparams = widthMatchParent(),
                            init = {
                                updatePadding(horizontal = 6.dp)
                                updatePaddingRelativeCompat(bottom = 16.dp)
                            }
                        ) {
                            ImageView(
                                id = "icon",
                                lparams = LayoutParams(64.dp, 64.dp) {
                                    topToParent()
                                    startToParent()
                                    endToParent()
                                    updateMargins(top = 20.dp)
                                }
                            ) {
                                setImageResource(R.mipmap.icon)
                            }
                            TextView(
                                id = "app_name_text_view",
                                lparams = LayoutParams {
                                    startToParent()
                                    endToParent()
                                    topToBottom = viewId("icon")
                                    updateMargins(top = 4.dp)
                                }
                            ) {
                                boldTypeFace()
                                textSize = 16f
                                text = stringRes(R.string.app_name)
                            }
                            MarqueeTextView(
                                id = "app_desc_text_view",
                                lparams = LayoutParams {
                                    startToParent()
                                    endToParent()
                                    topToBottom = viewId("app_name_text_view")
                                }
                            ) {
                                alpha = 0.85f
                                textSize = 12f
                                maxLines = 1
                                textRes = R.string.about_description
                            }
                            Chip(
                                id = "version_name_chip",
                                lparams = LayoutParams {
                                    topToBottom = viewId("app_desc_text_view")
                                    startToParent()
                                    endToParent()
                                }
                            ) {
                                text = getString(R.string.version_name_format, BuildConfig.VERSION_NAME)
                                if (BuildConfig.DEBUG) {
                                    chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.icon_bug_report, theme)
                                }
                                editModeText = "v1.0.0"
                            }
                            MaterialDivider(
                                id = "divider",
                                lparams = widthMatchParent {
                                    topToBottom = viewId("version_name_chip")
                                    updateMargins(vertical = 2.dp)
                                }
                            ) {
                                dividerInsetStart = 16.dp
                                dividerInsetEnd = 16.dp
                            }
                            TextView(
                                id = "open_source_info",
                                lparams = LayoutParams {
                                    topToBottom = viewId("divider")
                                    startToParent()
                                    endToParent()
                                    updateMargins(top = 5.dp)
                                }
                            ) {
                                textRes = R.string.about_opensource
                                textSize = 12f
                            }
                            MaterialButton(
                                attr = R.layout.style_view_material_textbutton,
                                id = "go_to_repo_button",
                                lparams = LayoutParams {
                                    topToBottom = viewId("open_source_info")
                                    startToParent()
                                    endToParent()
                                }
                            ) {
                                textRes = R.string.repo_url
                                onClick {
                                    startActivityOrMsg(Intent(Intent.ACTION_VIEW, getString(R.string.repo_url).toUri()))
                                }
                            }
                            MaterialCardView(
                                id = "contact_card",
                                lparams = LayoutParams {
                                    topToBottom = viewId("go_to_repo_button")
                                    startToParent()
                                    endToParent()
                                },
                                init = {
                                    contentPadding = 8.dp
                                }
                            ) {
                                MaterialTextView {
                                    textRes = R.string.contact_info
                                    textSize = 11f
                                }
                            }
                            MaterialButton(
                                id = "qq_channel",
                                lparams = LayoutParams {
                                    topToBottom = viewId("contact_card")
                                    startToParent()
                                    endToParent()
                                    updateMargins(top = 6.dp)
                                }
                            ) {
                                icon = drawableRes(R.drawable.icon_public)
                                iconPadding = 3.dp
                                textRes = R.string.qq_channel
                                onClick {
                                    Dialogs.textAction(originalViewContext, URL_QQ_CHANNEL) {
                                        startActivityOrMsg(Intent(Intent.ACTION_VIEW, URL_QQ_CHANNEL.toUri()))
                                    }
                                }
                            }
                            MaterialButton(
                                id = "qq_group",
                                lparams = LayoutParams {
                                    topToBottom = viewId("qq_channel")
                                    startToParent()
                                    endToParent()
                                }
                            ) {
                                icon = drawableRes(R.drawable.icon_group)
                                iconPadding = 3.dp
                                textRes = R.string.qq_group
                                onClick {
                                    Dialogs.text(originalViewContext, getString(R.string.group_id, ID_QQ_GROUP))
                                }
                            }
                            MaterialCardView(
                                lparams = LayoutParams {
                                    topToBottom = viewId("qq_group")
                                    startToParent()
                                    endToParent()
                                    updateMargins(top = 8.dp)
                                },
                                init = { contentPadding = 8.dp }
                            ) {
                                LinearLayout(init = { orientation = LinearLayout.VERTICAL }) {
                                    MaterialTextView(
                                        lparams = LayoutParams {
                                            gravity = Gravity.CENTER_HORIZONTAL
                                        }
                                    ) {
                                        boldTypeFace()
                                        textRes = R.string.disclaimer
                                    }
                                    MaterialTextView {
                                        textRes = R.string.about_to_uncle
                                        textSize = 11f
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}