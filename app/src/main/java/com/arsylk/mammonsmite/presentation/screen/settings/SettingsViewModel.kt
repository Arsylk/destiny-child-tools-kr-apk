package com.arsylk.mammonsmite.presentation.screen.settings

import androidx.core.os.bundleOf
import androidx.lifecycle.viewModelScope
import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.DestinyChildFiles
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.model.destinychild.DestinyChildPackage
import com.arsylk.mammonsmite.presentation.fragment.settings.SettingsFormInput
import com.arsylk.mammonsmite.presentation.fragment.settings.SettingsFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.Serializable

class SettingsViewModel(
    private val prefs: AppPreferences,
) : EffectViewModel<Effect>() {
    private val baseForms by lazy { prepareBaseForms() }
    private val _preset = MutableStateFlow(SettingsPreset.Custom)
    private val _form = MutableStateFlow(SettingsForm())
    val preset by lazy(_preset::asStateFlow)
    val form = _form.validate()

    init {
        val pkg = prefs.destinychildPackage
        val preset = SettingsPreset.values()
            .firstOrNull { it.pkg == pkg }
            ?: SettingsPreset.Custom
        applyPreset(preset)

        viewModelScope.launch(Dispatchers.IO) {
            _form.collectLatest { form ->
                val inferredPreset = baseForms.entries.firstOrNull {
                    it.value.valuesEqual(form)
                }?.key
                ensureActive()
                _preset.value = inferredPreset ?: SettingsPreset.Custom
            }
        }
    }

    fun applyPreset(preset: SettingsPreset) {
        val newForm = form.value.update { (field, value) ->
            value.copy(text = fieldTextForPreset(field, preset))
        }
        _form.value = newForm
        _preset.value = preset
    }

    fun updateField(field: SettingsField, text: String) {
        val newForm = form.value.update(field) {
            copy(text = text)
        }
        _form.value = newForm
    }

    fun saveForm() {
        withLoading(tag = "save") {
            val form = _form.value
            form.forEach { (field, value) ->
                when (field) {
                    SettingsField.Files -> prefs.destinychildFilesPath = value.text
                    SettingsField.Models -> prefs.destinychildModelsPath = value.text
                    SettingsField.Backgrounds -> prefs.destinychildBackgroundsPath = value.text
                    SettingsField.Sounds -> prefs.destinychildSoundsPath = value.text
                    SettingsField.TitleScreens -> prefs.destinychildTitleScreensPath = value.text
                    SettingsField.Locale -> prefs.destinychildLocalePath = value.text
                    SettingsField.ModelInfo -> prefs.destinychildModelInfoPath = value.text
                }
            }
            prefs.destinychildPackage = preset.value.pkg
        }
    }

    private fun fieldTextForPreset(field: SettingsField, preset: SettingsPreset): String {
        val transform: (getter: (DestinyChildPackage) -> File) -> String? = {
            preset.pkg?.run(it)?.absolutePath
        }

        return when (field) {
            SettingsField.Files -> transform(DestinyChildFiles::getFilesFolder)
                ?: prefs.destinychildFilesPath
            SettingsField.Models -> transform(DestinyChildFiles::getModelsFolder)
                ?: prefs.destinychildModelsPath
            SettingsField.Backgrounds -> transform(DestinyChildFiles::getBackgroundsFolder)
                ?: prefs.destinychildBackgroundsPath
            SettingsField.Sounds -> transform(DestinyChildFiles::getSoundsFolder)
                ?: prefs.destinychildSoundsPath
            SettingsField.TitleScreens -> transform(DestinyChildFiles::getTitleScreensFolder)
                ?: prefs.destinychildTitleScreensPath
            SettingsField.Locale -> transform(DestinyChildFiles::getLocaleFile)
                ?: prefs.destinychildLocalePath
            SettingsField.ModelInfo -> transform(DestinyChildFiles::getModelInfoFile)
                ?: prefs.destinychildModelInfoPath
        }
    }

    private fun validateFieldValue(
        field: SettingsField,
        value: SettingsFieldValue
    ): SettingsFieldValue {
        return when (field.type) {
            SettingsFieldType.File -> {
                when {
                    value.text.isBlank() ->
                        value.copy(isError = true, errorString = "Can't be blank")
                    !IFile(value.text).isFile ->
                        value.copy(isError = true, errorString = "Invalid file")
                    else ->
                        value.copy(isError = false, errorString = null)
                }
            }
            SettingsFieldType.Folder -> {
                when {
                    value.text.isBlank() ->
                        value.copy(isError = true, errorString = "Can't be blank")
                    !IFile(value.text).isDirectory ->
                        value.copy(isError = true, errorString = "Invalid file")
                    else ->
                        value.copy(isError = false, errorString = null)
                }
            }
        }
    }

    private fun prepareBaseForms(): Map<SettingsPreset, SettingsForm> {
        return SettingsPreset.values()
            .filterNot { it == SettingsPreset.Custom }
            .associateWith { preset ->
                SettingsForm().update { (field, value) ->
                    value.copy(text = fieldTextForPreset(field, preset))
                }
            }
    }

    private fun Flow<SettingsForm>.validate() =
        flatMapLatest { form ->
            channelFlow {
                send(form)
                ensureActive()

                val job = SupervisorJob()
                val channel = Channel<Pair<SettingsField, SettingsFieldValue>>()

                form.forEach { (field, value) ->
                    launch(job + Dispatchers.IO) {
                        ensureActive()
                        channel.send(field to validateFieldValue(field, value))
                    }
                }

                var mutable = form
                channel.consumeEach { (field, value) ->
                    mutable = mutable.update(field, value)
                    ensureActive()
                    send(mutable)
                }

                awaitClose {
                    job.cancel()
                    channel.cancel()
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _form.value)
}

sealed class Effect : UiEffect