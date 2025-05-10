package com.huanli233.biliterminal2.ui.fragment.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.proto.NightMode
import com.huanli233.biliterminal2.data.setting.LocalData
import com.huanli233.biliterminal2.data.setting.edit
import com.huanli233.biliterminal2.data.setting.toSystemValue
import com.huanli233.biliterminal2.databinding.FragmentSetupUiBinding
import com.huanli233.biliterminal2.ui.activity.setup.UiPreviewActivity
import com.huanli233.biliterminal2.ui.fragment.base.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import splitties.fragments.start

const val ROUND_MODE_DEFAULT_PADDING_HORIZONTAL = 5
const val ROUND_MODE_DEFAULT_PADDING_VERTICAL = 3

class UiSetupFragment: BaseFragment() {

    private lateinit var binding: FragmentSetupUiBinding
    private val modes by lazy { resources.getStringArray(R.array.dark_theme_modes) }
    private var selectedThemeMode = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetupUiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFields()
        initNightTheme()
        addChangeListeners()
        binding.uiPreviewButton.setOnClickListener {
            lifecycleScope.launch {
                save()
                withContext(Dispatchers.Main) {
                    start<UiPreviewActivity>()
                }
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        initNightTheme()
    }

    private fun initNightTheme() {
        (binding.darkThemeInput as? MaterialAutoCompleteTextView)?.apply {
            setText(modes[LocalData.settings.theme.nightMode.toSystemValue().takeIf { it != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM } ?: 0])
            setSimpleItems(modes)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                selectedThemeMode = position
                lifecycleScope.launch {
                    with (LocalData.settings) {
                        runBlocking {
                            LocalData.edit {
                                theme = theme.edit {
                                    nightMode = when (selectedThemeMode) {
                                        0 -> NightMode.NIGHT_MODE_AUTO
                                        1 -> NightMode.NIGHT_MODE_DAY
                                        else -> NightMode.NIGHT_MODE_NIGHT
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addChangeListeners() {
        binding.roundMode.setOnCheckedChangeListener { _, _ ->
            lifecycleScope.launch { save() }
        }
        binding.animations.setOnCheckedChangeListener { _, _ ->
            lifecycleScope.launch { save() }
        }
        binding.uiScaleInput.addTextChangedListener {
            lifecycleScope.launch { save() }
        }
    }

    private fun initFields() {
        binding.roundMode.isChecked = LocalData.settings.uiSettings.roundMode
        binding.animations.isChecked = LocalData.settings.theme.animationsEnabled
        binding.uiScaleInput.setText(LocalData.settings.uiSettings.uiScale.toString())
    }

    private suspend fun save() {
        LocalData.edit {
            uiSettings = uiSettings.edit {
                roundMode = binding.roundMode.isChecked
                binding.uiScaleInput.valueOrError(converter = { it.toFloatOrNull()?.takeIf { it in 0.25f..5.00f } }) {
                    uiScale = it
                }
            }
            theme = theme.edit {
                animationsEnabled = binding.animations.isChecked
            }
        }
    }

    private inline fun <T> TextInputEditText.valueOrError(
        converter: (String) -> T?,
        valueReceiver: (T) -> Unit
    ) = converter(text.toString())?.also {
        getTextInputLayout()?.apply {
            isErrorEnabled = false
            error = null
        }
    }?.let(valueReceiver) ?: let {
        getTextInputLayout()?.apply {
            isErrorEnabled = true
            error = context.getString(R.string.invalid_value)
        }
    }

    private fun TextInputEditText.getTextInputLayout(): TextInputLayout? {
        var parent: ViewParent? = parent
        while (parent is View) {
            if (parent is TextInputLayout) {
                return parent
            }
            parent = parent.parent
        }
        return null
    }

}
