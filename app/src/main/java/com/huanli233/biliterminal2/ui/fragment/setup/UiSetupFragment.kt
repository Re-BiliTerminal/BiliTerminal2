package com.huanli233.biliterminal2.ui.fragment.setup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.huanli233.biliterminal2.BiliTerminal
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.UserPreferences
import com.huanli233.biliterminal2.databinding.FragmentSetupUiBinding
import com.huanli233.biliterminal2.ui.activity.setup.UiPreviewActivity
import com.huanli233.biliterminal2.ui.fragment.base.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                    startActivity(Intent(context, UiPreviewActivity::class.java))
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
            setText(modes[UserPreferences.nightMode.get()])
            setSimpleItems(modes)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                selectedThemeMode = position
                lifecycleScope.launch {
                    with (UserPreferences) {
                        val oldThemeMode = nightMode.get()
                        nightMode.set(selectedThemeMode)
                        if (oldThemeMode != selectedThemeMode) {
                            withContext(Dispatchers.Main) {
                                BiliTerminal.setDefaultNightMode()
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
        binding.roundMode.isChecked = UserPreferences.roundMode.get()
        binding.uiScaleInput.setText(UserPreferences.uiScale.get().toString())
        binding.uiPaddingHorizontalInput.setText(UserPreferences.uiPaddingHorizontal.get().toString())
        binding.uiPaddingVerticalInput.setText(UserPreferences.uiPaddingVertical.get().toString())
    }

    private suspend fun save() {
        with(UserPreferences) {
            roundMode.set(binding.roundMode.isChecked)
            binding.uiScaleInput.valueOrError(converter = { it.toFloatOrNull()?.takeIf { it in 0.25f..5.00f } }) {
                uiScale.set(it)
            }
            binding.uiPaddingHorizontalInput.valueOrError(converter = { it.toIntOrNull()?.takeIf { it in 0..30 } }) {
                uiPaddingHorizontal.set(it)
            }
            binding.uiPaddingVerticalInput.valueOrError(converter = { it.toIntOrNull()?.takeIf { it in 0..30 } }) {
                uiPaddingVertical.set(it)
            }
        }
    }

    private inline fun <T> TextView.valueOrError(
        converter: (String) -> T?,
        valueReceiver: (T) -> Unit
    ) = converter(text.toString())?.let(valueReceiver) ?: let { error = context.getString(R.string.invalid_value) }

}
