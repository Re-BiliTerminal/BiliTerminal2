package com.huanli233.biliterminal2.ui.fragment.setup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.huanli233.biliterminal2.BiliTerminal
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.proto.NightMode
import com.huanli233.biliterminal2.data.setting.DataStore
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
            setText(modes[DataStore.appSettings.nightMode.toSystemValue().takeIf { it != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM } ?: 0])
            setSimpleItems(modes)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                selectedThemeMode = position
                lifecycleScope.launch {
                    with (DataStore.appSettings) {
                        val oldThemeMode = nightMode.toSystemValue()
                        runBlocking {
                            DataStore.editData {
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

    private fun addChangeListeners() {
        binding.roundMode.setOnCheckedChangeListener { _, _ ->
            lifecycleScope.launch {
                save()
            }
        }
        binding.uiScaleInput.addTextChangedListener {
            lifecycleScope.launch {
                save()
            }
        }
        binding.uiPaddingHorizontalInput.addTextChangedListener {
            lifecycleScope.launch {
                save()
            }
        }
        binding.uiPaddingVerticalInput.addTextChangedListener {
            lifecycleScope.launch {
                save()
            }
        }
    }

    private fun initFields() {
        binding.roundMode.isChecked = DataStore.appSettings.roundMode
        binding.uiScaleInput.setText(DataStore.appSettings.uiScale.toString())
        binding.uiPaddingHorizontalInput.setText(DataStore.appSettings.uiPaddingHorizontal.toString())
        binding.uiPaddingVerticalInput.setText(DataStore.appSettings.uiPaddingVertical.toString())
    }

    private suspend fun save() {
        DataStore.editData {
            roundMode = binding.roundMode.isChecked
            binding.uiScaleInput.valueOrError(converter = { it.toFloatOrNull()?.takeIf { it in 0.25f..5.00f } }) {
                uiScale = it
            }
            binding.uiPaddingHorizontalInput.valueOrError(converter = { it.toIntOrNull()?.takeIf { it in 0..30 } }) {
                uiPaddingHorizontal = it
            }
            binding.uiPaddingVerticalInput.valueOrError(converter = { it.toIntOrNull()?.takeIf { it in 0..30 } }) {
                uiPaddingVertical = it
            }
        }
    }

    private inline fun <T> TextView.valueOrError(
        converter: (String) -> T?,
        valueReceiver: (T) -> Unit
    ) = converter(text.toString())?.let(valueReceiver) ?: let { error = context.getString(R.string.invalid_value) }

}
