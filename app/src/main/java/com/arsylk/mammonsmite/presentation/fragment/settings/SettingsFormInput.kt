package com.arsylk.mammonsmite.presentation.fragment.settings

data class SettingsFormInput(
    val map: Map<SettingsFragment.FormField, String>
) {

    companion object {
        val Default = SettingsFormInput(emptyMap())
    }
}