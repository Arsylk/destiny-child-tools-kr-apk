package com.arsylk.mammonsmite.presentation.fragment.settings

import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.DestinyChildFiles
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.presentation.fragment.settings.SettingsFragment.*
import com.arsylk.mammonsmite.presentation.fragment.settings.SettingsFragment.FormField.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val prefs: AppPreferences
) : EffectViewModel<Effect>() {
    private val _preset = MutableStateFlow(Preset.CUSTOM)
    private val _formInput = MutableStateFlow(SettingsFormInput.Default)
    val preset by lazy(_preset::asStateFlow)
    val formInput by lazy(_formInput::asStateFlow)

    init {
        val preset = Preset.values()
            .firstOrNull { it.pkg == prefs.destinychildPackage }
            ?: Preset.CUSTOM
        submitForm(formForPreset(preset))
    }

    fun submitPreset(preset: Preset) {
        _preset.value = preset
        prefs.destinychildPackage = preset.pkg
        if (preset == Preset.CUSTOM) return
        submitForm(formForPreset(preset))
    }

    fun submitForm(input: SettingsFormInput, fromUser: Boolean = false) {
        _formInput.value = input
        input.map.forEach { (k, v) ->
            when (k) {
                FILES -> prefs.destinychildFilesPath = v
                MODELS -> prefs.destinychildModelsPath = v
                BACKGROUNDS -> prefs.destinychildBackgroundsPath = v
                SOUNDS -> prefs.destinychildSoundsPath = v
                TITLE_SCREENS -> prefs.destinychildTitleScreensPath = v
                LOCALE -> prefs.destinychildLocalePath = v
                MODEL_INFO -> prefs.destinychildModelInfoPath = v
            }
        }

        val preset = presetForForm(input)
        _preset.value = preset
        prefs.destinychildPackage = preset.pkg

        if (fromUser) enqueueEffect(Effect.SettingsSaved)
    }

    private fun formForPreset(preset: Preset): SettingsFormInput {
        return when (val pkg = preset.pkg) {
            null -> SettingsFormInput(
                mapOf(
                    FILES to prefs.destinychildFilesPath,
                    MODELS to prefs.destinychildModelsPath,
                    BACKGROUNDS to prefs.destinychildBackgroundsPath,
                    SOUNDS to prefs.destinychildSoundsPath,
                    TITLE_SCREENS to prefs.destinychildTitleScreensPath,
                    LOCALE to prefs.destinychildLocalePath,
                    MODEL_INFO to prefs.destinychildModelInfoPath,
                )
            )
            else -> SettingsFormInput(
                mapOf(
                    FILES to DestinyChildFiles.getFilesFolder(pkg),
                    MODELS to DestinyChildFiles.getModelsFolder(pkg),
                    BACKGROUNDS to DestinyChildFiles.getBackgroundsFolder(pkg),
                    SOUNDS to DestinyChildFiles.getSoundsFolder(pkg),
                    TITLE_SCREENS to DestinyChildFiles.getTitleScreensFolder(pkg),
                    LOCALE to DestinyChildFiles.getLocaleFile(pkg),
                    MODEL_INFO to DestinyChildFiles.getModelInfoFile(pkg),
                ).mapValues { it.value.absolutePath }
            )
        }
    }

    private fun presetForForm(input: SettingsFormInput): Preset {
        return when (input) {
            formForPreset(Preset.KOREA) -> Preset.KOREA
            formForPreset(Preset.JAPAN) -> Preset.JAPAN
            formForPreset(Preset.GLOBAL) -> Preset.GLOBAL
            else -> Preset.CUSTOM
        }
    }
}

sealed class Effect : UiEffect {
    object SettingsSaved : Effect()
}