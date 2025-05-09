package com.huanli233.biliterminal2.ui.preferences

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.Preference
import com.huanli233.biliterminal2.R
import androidx.core.content.withStyledAttributes
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.huanli233.biliterminal2.data.setting.DataStore
import com.huanli233.biliterminal2.ui.dialog.Dialogs.dialogBuilder
import com.huanli233.biliterminal2.utils.MsgUtil
import com.huanli233.biliterminal2.utils.extensions.originalConfigContext
import splitties.alertdialog.appcompat.cancelButton
import splitties.dimensions.dp


open class EditTextPreference(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
): BasePreference(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0, 0)
    constructor(context: Context): this(context, null)

    var emptyAllowed = false

    var text: String? = null
        set(value) {
            val wasBlocking = shouldDisableDependents()

            field = value
            persistString(value)

            val isBlocking = shouldDisableDependents()
            if (isBlocking != wasBlocking) {
                notifyDependencyChange(isBlocking)
            }

            notifyChanged()
        }

    init {
        context.withStyledAttributes(
            attrs, R.styleable.AppEditTextPreference, defStyleAttr, defStyleRes
        ) {
            if (getBoolean(R.styleable.AppEditTextPreference_useSimpleSummaryProvider, false)) {
                setSummaryProvider(SimpleSummaryProvider)
            }
            emptyAllowed = getBoolean(R.styleable.AppEditTextPreference_emptyAllowed, false)
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        text = getPersistedString(defaultValue as? String)
    }

    override fun shouldDisableDependents(): Boolean {
        return text.isNullOrEmpty() || super.shouldDisableDependents()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            return superState
        }

        return SavedState(superState).apply {
            mText = text
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state::javaClass != SavedState::javaClass) {
            super.onRestoreInstanceState(state)
            return
        }

        val myState = state as SavedState
        super.onRestoreInstanceState(myState.superState)
        text = myState.mText
    }

    override fun onClick() {
        val mEditText = onCreateEditText()
        mEditText.editText?.setText(text)
        context.originalConfigContext.dialogBuilder()
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> }
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                val inputText = mEditText.editText?.text.toString()
                if (checkText(inputText)) {
                    text = inputText
                } else {
                    MsgUtil.showMsg(context.getString(R.string.invalid_input))
                }
            }
            .setView(mEditText)
            .show()
    }

    open fun onCreateEditText(): TextInputLayout {
        return TextInputLayout(context.originalConfigContext).apply layout@{
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val editText = TextInputEditText(context.originalConfigContext).apply {
                addTextChangedListener {
                    if (!checkText(it.toString())) {
                        isErrorEnabled = true
                        this@layout.error = context.getString(R.string.invalid_input)
                    } else {
                        isErrorEnabled = false
                        this@layout.error = null
                    }
                }
            }
            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                if (DataStore.appSettings.fullScreenDialogDisabled) {
                    updatePadding(top = context.dp(22), left = context.dp(17), right = context.dp(17))
                } else {
                    updateMargins(left = context.dp(16), right = context.dp(16))
                }
            }
            addView(editText, layoutParams)
        }
    }

    open fun checkText(text: String): Boolean {
        return true
    }

    inner class SavedState: BaseSavedState {
        var mText: String? = null
        constructor(superState: Parcelable?): super(superState)
        constructor(source: Parcel): super(source) {
            mText = source.readString()
        }
        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeString(mText)
        }
    }

    object SimpleSummaryProvider : SummaryProvider<EditTextPreference> {
        override fun provideSummary(preference: EditTextPreference): CharSequence? {
            return if (preference.text.isNullOrEmpty()) {
                preference.context.getString(R.string.not_set_text)
            } else {
                preference.text
            }
        }

    }
}