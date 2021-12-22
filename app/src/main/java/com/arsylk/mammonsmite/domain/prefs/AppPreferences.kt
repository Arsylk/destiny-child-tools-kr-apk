package com.arsylk.mammonsmite.domain.prefs

import android.content.Context
import androidx.core.content.edit
import com.arsylk.mammonsmite.domain.files.DestinyChildFiles
import com.arsylk.mammonsmite.model.destinychild.DestinyChildPackage
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var destinychildPackage
        by NullableEnumValue(values = DestinyChildPackage.values())
    var destinychildFilesPath
        by StringValue { DestinyChildFiles.filesFolder.absolutePath }
    var destinychildModelsPath
        by StringValue { DestinyChildFiles.modelsFolder.absolutePath }
    var destinychildBackgroundsPath
        by StringValue { DestinyChildFiles.backgroundsFolder.absolutePath }
    var destinychildSoundsPath
        by StringValue { DestinyChildFiles.soundsFolder.absolutePath }
    var destinychildTitleScreensPath
        by StringValue { DestinyChildFiles.titleScreensFolder.absolutePath }
    var destinychildLocalePath
        by StringValue { DestinyChildFiles.localeFile.absolutePath }
    var destinychildModelInfoPath
        by StringValue { DestinyChildFiles.modelInfoFile.absolutePath }


    private class StringValue(val key: String? = null, initializer: () -> String) {
        private val default by lazy(initializer)

        operator fun getValue(thisRef: AppPreferences, p: KProperty<*>): String {
            val key = key ?: p.name
            return thisRef.prefs.getString(key, default) ?: default
        }
        operator fun setValue(thisRef: AppPreferences, p: KProperty<*>, value: String) {
            val key = key ?: p.name
            thisRef.prefs.edit(commit = true) {
                putString(key, value)
            }
        }
    }

    private class EnumValue<T: Enum<T>>(
        val key: String? = null,
        val values: Array<T>,
        initializer: () -> T,
    ) {
        private val default by lazy(initializer)

        operator fun getValue(thisRef: AppPreferences, p: KProperty<*>): T {
            val key = key ?: p.name
            val str = thisRef.prefs.getString(key, null)
            return values.firstOrNull { it.name == str } ?: default
        }

        operator fun setValue(thisRef: AppPreferences, p: KProperty<*>, value: T) {
            val key = key ?: p.name
            thisRef.prefs.edit(commit = true) {
                putString(key, value.name)
            }
        }
    }

    private class NullableEnumValue<T: Enum<T>>(
        val key: String? = null,
        val values: Array<T>,
        initializer: () -> T? = { null },
    ) {
        private val default by lazy(initializer)

        operator fun getValue(thisRef: AppPreferences, p: KProperty<*>): T? {
            val key = key ?: p.name
            val str = thisRef.prefs.getString(key, null)
            return values.firstOrNull { it.name == str } ?: default
        }

        operator fun setValue(thisRef: AppPreferences, p: KProperty<*>, value: T?) {
            val key = key ?: p.name
            thisRef.prefs.edit(commit = true) {
                putString(key, value?.name)
            }
        }
    }
}