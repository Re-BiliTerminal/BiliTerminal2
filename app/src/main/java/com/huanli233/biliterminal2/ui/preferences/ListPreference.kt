package com.huanli233.biliterminal2.ui.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.ArrayRes
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.huanli233.biliterminal2.utils.typedarray.TypedArrayUtils
import androidx.preference.R as PreferenceR
import androidx.core.content.withStyledAttributes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.ui.dialog.Dialogs.dialogBuilder
import com.huanli233.biliterminal2.ui.widget.scalablecontainer.AppRecyclerView
import com.huanli233.biliterminal2.utils.extensions.dp2px
import com.huanli233.biliterminal2.utils.extensions.originalConfigContext
import splitties.dimensions.dp

/**
 * A [Preference] that displays a list of entries as a Material Design dialog.
 *
 * This preference saves a string value. This string will be the value from the
 * [entryValues] array.
 */
@SuppressLint("PrivateResource")
open class MaterialListPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = TypedArrayUtils.getAttr(context, PreferenceR.attr.preferenceStyle, android.R.attr.preferenceStyle),
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes) {

    private var mEntries: Array<CharSequence>? = null
    private var mEntryValues: Array<CharSequence>? = null
    private var mValue: String? = null
    private var mValueSet: Boolean = false

    init {
        context.withStyledAttributes(
            attrs, PreferenceR.styleable.ListPreference, defStyleAttr, defStyleRes
        ) {

            mEntries = TypedArrayUtils.getTextArray(
                this, PreferenceR.styleable.ListPreference_entries,
                PreferenceR.styleable.ListPreference_android_entries
            )

            mEntryValues = TypedArrayUtils.getTextArray(
                this, PreferenceR.styleable.ListPreference_entryValues,
                PreferenceR.styleable.ListPreference_android_entryValues
            )

            if (TypedArrayUtils.getBoolean(this, PreferenceR.styleable.ListPreference_useSimpleSummaryProvider,
                    PreferenceR.styleable.ListPreference_useSimpleSummaryProvider, false)) {
                setSummaryProvider(SimpleSummaryProvider.getInstance());
            }
        }
    }

    /**
     * Sets the human-readable entries to be shown in the list. This will be shown in subsequent
     * dialogs.
     *
     * Each entry must have a corresponding index in [entryValues].
     *
     * @param entries The entries
     * @see #setEntryValues
     */
    fun setEntries(entries: Array<CharSequence>?) {
        mEntries = entries
    }

    /**
     * @param entriesResId The entries array as a resource
     * @see #setEntries
     */
    fun setEntries(@ArrayRes entriesResId: Int) {
        setEntries(context.resources.getTextArray(entriesResId))
    }

    /**
     * The list of entries to be shown in the list in subsequent dialogs.
     *
     * @return The list as an array
     */
    fun getEntries(): Array<CharSequence>? {
        return mEntries
    }

    /**
     * The array to find the value to save for a preference when an entry from entries is
     * selected. If a user clicks on the second item in entries, the second item in this array
     * will be saved to the preference.
     *
     * @param entryValues The array to be used as values to save for the preference
     */
    fun setEntryValues(entryValues: Array<CharSequence>?) {
        mEntryValues = entryValues
    }

    /**
     * @param entryValuesResId The entry values array as a resource
     * @see #setEntryValues
     */
    fun setEntryValues(@ArrayRes entryValuesResId: Int) {
        setEntryValues(context.resources.getTextArray(entryValuesResId))
    }

    /**
     * Returns the array of values to be saved for the preference.
     *
     * @return The array of values
     */
    fun getEntryValues(): Array<CharSequence>? {
        return mEntryValues
    }

    /**
     * Sets the value of the key. This should be one of the entries in [getEntryValues].
     *
     * @param value The value to set for the key
     */
    fun setValue(value: String?) {
        val changed = !TextUtils.equals(mValue, value)
        if (changed || !mValueSet) {
            mValue = value
            mValueSet = true
            persistString(value)
            if (changed) {
                notifyChanged()
            }
        }
    }

    /**
     * Returns the value of the key. This should be one of the entries in [getEntryValues].
     *
     * @return The value of the key
     */
    fun getValue(): String? {
        return mValue
    }

    /**
     * Returns the entry corresponding to the current value.
     *
     * @return The entry corresponding to the current value, or `null`
     */
    fun getEntry(): CharSequence? {
        val index = getValueIndex()
        return if (index >= 0 && mEntries != null) mEntries!![index] else null
    }

    /**
     * Returns the index of the given value (in the entry values array).
     *
     * @param value The value whose index should be returned
     * @return The index of the value, or -1 if not found
     */
    fun findIndexOfValue(value: String?): Int {
        if (value != null && mEntryValues != null) {
            for (i in mEntryValues!!.indices.reversed()) {
                if (TextUtils.equals(mEntryValues!![i].toString(), value)) {
                    return i
                }
            }
        }
        return -1
    }

    /**
     * Sets the value to the given index from the entry values.
     *
     * @param index The index of the value to set
     */
    fun setValueIndex(index: Int) {
        if (mEntryValues != null && index >= 0 && index < mEntryValues!!.size) {
            setValue(mEntryValues!![index].toString())
        }
    }

    private fun getValueIndex(): Int {
        return findIndexOfValue(mValue)
    }

    override fun onClick() {
        if (mEntries == null || mEntryValues == null || mEntries!!.size != mEntryValues!!.size) {
            Log.e(TAG, "ListPreference requires matching 'entries' and 'entryValues' arrays.")
            return
        }

        val entries = mEntries!!
        val entryValues = mEntryValues!!
        val currentValue = getValue()
        val initialSelectedIndex = findIndexOfValue(currentValue)

        val originalConfigContext = context.originalConfigContext

        val view = LayoutInflater.from(context)
            .cloneInContext(originalConfigContext)
            .inflate(R.layout.dialog_list_custom, null)
        val recyclerView = view.findViewById<AppRecyclerView>(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(originalConfigContext)
        recyclerView.adapter = SingleChoiceAdapter(entries, initialSelectedIndex) { clickedPosition -> }

        val checkedItem = findIndexOfValue(currentValue)

        originalConfigContext.dialogBuilder()
            .setTitle(title)
            .setSingleChoiceItems(entries, checkedItem) { dialog, which ->
                if (which >= 0 && which < entryValues.size) {
                    val selectedValue = entryValues[which].toString()
                    if (callChangeListener(selectedValue)) {
                        setValue(selectedValue)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        setValue(getPersistedString(defaultValue as String?))
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            return superState
        }

        val myState = SavedState(superState)
        myState.mValue = getValue()
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state.javaClass != SavedState::class.java) {
            super.onRestoreInstanceState(state)
            return
        }

        val myState = state as SavedState
        super.onRestoreInstanceState(myState.superState)
        setValue(myState.mValue)
    }

    private class SavedState : BaseSavedState {
        var mValue: String? = null

        constructor(source: Parcel) : super(source) {
            mValue = source.readString()
        }

        constructor(superState: Parcelable?) : super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeString(mValue)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    class SimpleSummaryProvider : SummaryProvider<MaterialListPreference> {

        private object HOLDER {
            val INSTANCE = SimpleSummaryProvider()
        }

        companion object {
            @JvmStatic
            fun getInstance(): SimpleSummaryProvider {
                return HOLDER.INSTANCE
            }
        }

        override fun provideSummary(preference: MaterialListPreference): CharSequence {
            return if (TextUtils.isEmpty(preference.getEntry())) {
                preference.context.getString(com.huanli233.biliterminal2.R.string.not_set_text)
            } else {
                preference.getEntry()!!
            }
        }
    }

    companion object {
        private val TAG = MaterialListPreference::class.java.simpleName
    }
}

class SingleChoiceAdapter(
    private val entries: Array<CharSequence>,
    initialSelectedIndex: Int,
    private val onItemClick: (position: Int) -> Unit
) : RecyclerView.Adapter<SingleChoiceAdapter.ViewHolder>() {

    private var selectedPosition = initialSelectedIndex

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val radioButton: CheckedTextView = itemView.findViewById(R.id.text1)

        init {
            itemView.setOnClickListener {
                val clickedPosition = bindingAdapterPosition
                if (clickedPosition != RecyclerView.NO_POSITION && clickedPosition != selectedPosition) {
                    val oldSelectedPosition = selectedPosition
                    selectedPosition = clickedPosition
                    notifyItemChanged(oldSelectedPosition)
                    notifyItemChanged(selectedPosition)
                    onItemClick.invoke(selectedPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .cloneInContext(parent.context)
            .inflate(R.layout.item_single_choice, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.radioButton.text = entries[position]
        holder.radioButton.isChecked = position == selectedPosition
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    fun getSelectedPosition(): Int {
        return selectedPosition
    }
}