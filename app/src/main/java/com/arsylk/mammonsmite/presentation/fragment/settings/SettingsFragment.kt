package com.arsylk.mammonsmite.presentation.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.arsylk.mammonsmite.R
import com.arsylk.mammonsmite.databinding.FragmentSettingsBinding
import com.arsylk.mammonsmite.model.common.FileTypeOld
import com.arsylk.mammonsmite.model.destinychild.DestinyChildPackage
import com.arsylk.mammonsmite.presentation.dialog.file.picker.FilePickerDialog
import com.arsylk.mammonsmite.presentation.fragment.BaseBindingFragment
import com.arsylk.mammonsmite.presentation.fragment.settings.SettingsFragment.FormField.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class SettingsFragment : BaseBindingFragment<FragmentSettingsBinding>() {
    private val viewModel by viewModel<SettingsViewModel>()
    private val presetAdapter by lazy {
        val items = Preset.values()
            .map { it.name.lowercase().replaceFirstChar { it.uppercase() } }
        ArrayAdapter(requireContext(), R.layout.item_dropdown, items)
    }

    override fun inflate(inflater: LayoutInflater) = FragmentSettingsBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        (binding?.presetsMenu?.editText as? AutoCompleteTextView)?.setAdapter(presetAdapter)

        setupListeners()

        setupObservers()
        setupEffectObserver()
    }

    private fun setupListeners() {
        binding?.apply {
            (presetsMenu.editText as? AutoCompleteTextView)?.setOnItemClickListener { adapterView, view, i, l ->
                val preset = Preset.values().getOrNull(i) ?: return@setOnItemClickListener
                viewModel.submitPreset(preset)
            }

            form.forEach { (_, input) ->
                input.setOnIconClick {
                    openFilePicker(File(text), type) {
                        text = it.absolutePath
                        viewModel.submitPreset(Preset.CUSTOM)
                    }
                }
                input.setAfterTextChanged {
                    viewModel.submitPreset(Preset.CUSTOM)
                }
            }

            saveButton.setOnClickListener {
                submitForm()
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.preset.collectLatest { preset ->
                (binding?.presetsMenu?.editText as? AutoCompleteTextView)?.apply {
                    listSelection = preset.ordinal
                    setText(adapter.getItem(preset.ordinal).toString())
                    presetAdapter.filter.filter(null)
                    dismissDropDown()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.formInput.collectLatest { input ->
                binding?.apply {
                    val form = form
                    input.map.forEach { (k, v) -> form[k]?.text = v }
                }
            }
        }
    }

    private fun setupEffectObserver() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.effect.collect { effect ->
                when (effect) {
                    Effect.SettingsSaved ->
                        Toast.makeText(context, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openFilePicker(file: File, type: FileTypeOld, onFileSelected: (File) -> Unit) {
        FilePickerDialog.newInstance(file, type, onFileSelected)
            .show(parentFragmentManager, FilePickerDialog.TAG)
    }

    private fun submitForm() {
        val binding = binding ?: return
        val input = SettingsFormInput(binding.form.mapValues { it.value.text })
        viewModel.submitForm(input, fromUser = true)
    }

    private inline val FragmentSettingsBinding.form get() = mapOf(
        FILES to filesInput,
        MODELS to modelsInput,
        BACKGROUNDS to backgroundsInput,
        SOUNDS to soundsInput,
        TITLE_SCREENS to titleScreensInput,
        LOCALE to localeInput,
        MODEL_INFO to modelInfoInput,
    )

    enum class Preset(val pkg: DestinyChildPackage?) {
        CUSTOM(null),
        KOREA(DestinyChildPackage.KOREA),
        JAPAN(DestinyChildPackage.JAPAN),
        GLOBAL(DestinyChildPackage.GLOBAL);
    }

    enum class FormField {
        FILES, MODELS, BACKGROUNDS, SOUNDS, TITLE_SCREENS, LOCALE, MODEL_INFO
    }
}