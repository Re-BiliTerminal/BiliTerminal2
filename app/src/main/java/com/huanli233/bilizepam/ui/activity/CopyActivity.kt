package com.huanli233.bilizepam.ui.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.core.view.updateMargins
import com.google.android.material.textfield.TextInputLayout
import com.highcapable.betterandroid.ui.extension.view.updateMargins
import com.highcapable.betterandroid.ui.extension.view.updatePadding
import com.highcapable.hikage.extension.setContentView
import com.highcapable.hikage.extension.widget.bottomToParent
import com.highcapable.hikage.extension.widget.endToParent
import com.highcapable.hikage.extension.widget.startToParent
import com.highcapable.hikage.extension.widget.textRes
import com.highcapable.hikage.extension.widget.topToParent
import com.highcapable.hikage.widget.android.widget.TextView
import com.highcapable.hikage.widget.androidx.constraintlayout.widget.ConstraintLayout
import com.highcapable.hikage.widget.com.google.android.material.button.MaterialButton
import com.highcapable.hikage.widget.com.google.android.material.button.MaterialButtonGroup
import com.highcapable.hikage.widget.com.google.android.material.textfield.TextInputEditText
import com.highcapable.hikage.widget.com.google.android.material.textfield.TextInputLayout
import com.highcapable.hikage.widget.com.huanli233.bilizepam.ui.widget.components.TopBar
import com.highcapable.hikage.widget.com.huanli233.bilizepam.ui.widget.scalablecontainer.AppScrollView
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.activity.base.BaseActivity
import com.huanli233.bilizepam.utils.MsgUtil
import com.huanli233.bilizepam.utils.extensions.updatePaddingRelativeCompat
import splitties.views.InputType
import splitties.views.material.text
import splitties.views.onClick
import kotlin.math.abs
import kotlin.math.roundToInt

class CopyActivity: BaseActivity() {

    private val content by lazy { intent?.getStringExtra("content").orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView {
            ConstraintLayout(matchParent()) {
                TopBar(
                    lparams = widthMatchParent {
                        topToParent()
                        startToParent()
                    },
                    init = {
                        setTitle(stringRes(R.string.copy))
                    }
                )
                AppScrollView(
                    lparams = widthMatchParent(0) {
                        startToParent()
                        bottomToParent()
                        topToBottom = R.id.top_bar
                    }
                ) {
                    ConstraintLayout(
                        lparams = widthMatchParent(),
                        init = {
                            updatePadding(
                                horizontal = dimenRes(R.dimen.page_padding_horizontal).roundToInt()
                            )
                            updatePaddingRelativeCompat(bottom = dimenRes(R.dimen.page_bottom_padding).toInt())
                        }
                    ) {
                        val content = TextInputLayout(
                            id = "content_layout",
                            attr = R.layout.style_view_filled_textfield,
                            lparams = widthMatchParent {
                                updateMargins(top = 2.dp)
                                topToParent()
                                startToParent()
                            },
                            init = {
                                hint = stringRes(R.string.text)
                            }
                        ) {
                            TextInputEditText(widthMatchParent()) {
                                setText(content)
                                whenAvailableTyped<EditText>("begin") { beginEditText ->
                                    whenAvailableTyped<EditText>("end") { endEditText ->
                                        onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                                            if (!hasFocus) {
                                                beginEditText.setText(selectionStart.toString())
                                                endEditText.setText(selectionEnd.toString())
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        TextView(
                            id = "select_range_tip",
                            lparams = LayoutParams {
                                updateMargins(top = 5.dp, bottom = 6.dp)
                                topToBottom = viewId("content_layout")
                                startToParent()
                            }
                        ) {
                            text = stringRes(R.string.select_range)
                        }
                        val textWatcher = object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {
                                whenAvailableTyped<TextInputLayout>("begin") { beginEditText ->
                                    whenAvailableTyped<TextInputLayout>("end") { endEditText ->
                                        beginEditText.text.toString().toIntOrNull()?.also { begin ->
                                            endEditText.text.toString().toIntOrNull()?.also { end ->
                                                val textLength = content.editText?.text?.length ?: 0
                                                if (begin >= 0 && end >= 0 && begin <= textLength && end <= textLength) {
                                                    content.editText?.setSelection(begin, end)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) = Unit

                            override fun onTextChanged(
                                str: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) = Unit
                        }
                        val beginTextInputLayout = TextInputLayout(
                            id = "begin",
                            lparams = LayoutParams(width = 0) {
                                updateMargins(horizontal = 3.dp)
                                topToBottom = viewId("select_range_tip")
                                startToParent()
                                endToStart = viewId("index_between_tip")
                            },
                            init = {
                                hint = stringRes(R.string.hint_start_pos)
                            }
                        ) {
                            TextInputEditText(widthMatchParent()) {
                                setText("0")
                                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                                addTextChangedListener(textWatcher)
                            }
                        }
                        TextView(
                            id = "index_between_tip",
                            lparams = LayoutParams {
                                topToTop = viewId("begin")
                                bottomToBottom = viewId("begin")
                                startToEnd = viewId("begin")
                                endToStart = viewId("end")
                                updateMargins(horizontal = 3.dp)
                            }
                        ) { textRes = R.string.tilde }
                        val endTextInputLayout = TextInputLayout(
                            id = "end",
                            lparams = LayoutParams(width = 0) {
                                updateMargins(horizontal = 3.dp)
                                topToTop = viewId("begin")
                                bottomToBottom = viewId("begin")
                                startToEnd = viewId("index_between_tip")
                                endToParent()
                            },
                            init = {
                                hint = stringRes(R.string.hint_end_pos)
                            }
                        ) {
                            TextInputEditText(widthMatchParent()) {
                                setText("0")
                                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                                addTextChangedListener(textWatcher)
                            }
                        }
                        MaterialButtonGroup(
                            id = "action_buttons",
                            lparams = widthMatchParent {
                                topToBottom = viewId("begin")
                                startToParent()
                            },
                            init = {
                                spacing = 2.dp
                            }
                        ) {
                            MaterialButton(
                                attr = R.layout.style_view_icon_button,
                                id = "begin_left",
                                lparams = LayoutParams(width = 0) {
                                    weight = 0.25f
                                }
                            ) {
                                text = "<"
                                onClick {
                                    beginTextInputLayout.editText?.setText(
                                        beginTextInputLayout.editText?.text?.toString()?.toIntOrNull()?.minus(1)?.takeIf { it >= 0 }
                                            ?.toString()
                                            ?: "0"
                                    )
                                }
                            }
                            MaterialButton(
                                attr = R.layout.style_view_icon_button,
                                id = "begin_right",
                                lparams = LayoutParams(width = 0) {
                                    weight = 0.25f
                                }
                            ) {
                                text = ">"
                                onClick {
                                    val length = content.editText?.text?.length ?: 0
                                    beginTextInputLayout.editText?.setText(
                                        beginTextInputLayout.editText?.text?.toString()?.toIntOrNull()?.plus(1)?.takeIf {
                                            it <= length
                                        }?.toString() ?: (length).toString()
                                    )
                                }
                            }
                            MaterialButton(
                                attr = R.layout.style_view_icon_button,
                                id = "end_left",
                                lparams = LayoutParams(width = 0) {
                                    weight = 0.25f
                                }
                            ) {
                                text = "<"
                                onClick {
                                    endTextInputLayout.editText?.setText(
                                        endTextInputLayout.editText?.text?.toString()?.toIntOrNull()?.minus(1)?.takeIf { it >= 0 }
                                            ?.toString()
                                            ?: "0"
                                    )
                                }
                            }
                            MaterialButton(
                                attr = R.layout.style_view_icon_button,
                                id = "end_right",
                                lparams = LayoutParams(width = 0) {
                                    weight = 0.25f
                                }
                            ) {
                                text = ">"
                                onClick {
                                    val length = content.editText?.text?.length ?: 0
                                    endTextInputLayout.editText?.setText(
                                        endTextInputLayout.editText?.text?.toString()?.toIntOrNull()?.plus(1)?.takeIf {
                                            it <= length
                                        }?.toString() ?: (length).toString()
                                    )
                                }
                            }
                        }
                        MaterialButton(
                            id = "copy",
                            lparams = LayoutParams {
                                updateMargins(top = 6.dp)
                                startToParent()
                                endToStart = viewId("copy_all")
                                topToBottom = viewId("action_buttons")
                            }
                        ) {
                            textRes = R.string.copy
                            onClick {
                                runCatching {
                                    (context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("content",
                                        content.editText?.text?.let { fullText ->
                                            val startIndex = beginTextInputLayout.editText?.text?.toString()?.toIntOrNull() ?: 0
                                            val endIndex = endTextInputLayout.editText?.text?.toString()?.toIntOrNull() ?: fullText.length

                                            val actualStart = minOf(startIndex, endIndex).coerceIn(0, fullText.length)
                                            val actualEnd = maxOf(startIndex, endIndex).coerceIn(0, fullText.length)

                                            fullText.substring(actualStart until actualEnd)
                                        } ?: ""
                                    ))
                                }.onSuccess {
                                    MsgUtil.showMsg(stringRes(R.string.copied))
                                }
                            }
                        }
                        MaterialButton(
                            id = "copy_all",
                            lparams = LayoutParams {
                                topToTop = viewId("copy")
                                startToEnd = viewId("copy")
                                endToParent()
                            }
                        ) {
                            textRes = R.string.copy_all
                            onClick {
                                runCatching {
                                    (context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("content", content.editText?.text ?: ""))
                                }.onSuccess {
                                    MsgUtil.showMsg(stringRes(R.string.copied))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}