package com.arsylk.mammonsmite.presentation.screen.settings

import com.arsylk.mammonsmite.model.destinychild.DestinyChildPackage

data class SettingsForm(val fields: Map<SettingsField, SettingsFieldValue> = Default): Iterable<Pair<SettingsField, SettingsFieldValue>> {

    operator fun get(key: SettingsField): SettingsFieldValue {
        return fields[key] ?: Default[key]!!
    }

    fun update(key: SettingsField, value: SettingsFieldValue): SettingsForm {
        val mutable = fields.toMutableMap()
        mutable[key] = value
        return SettingsForm(mutable.toMap())
    }

    fun update(key: SettingsField, transform: SettingsFieldValue.() -> SettingsFieldValue): SettingsForm {
        val mutable = fields.toMutableMap()
        mutable[key] = transform(this[key])
        return SettingsForm(mutable.toMap())
    }

    fun update(transform: (pair: Pair<SettingsField, SettingsFieldValue>) -> SettingsFieldValue): SettingsForm {
        val mutable = fields.toMutableMap()
        forEach { pair ->
            mutable[pair.first] = transform(pair)
        }
        return SettingsForm(mutable.toMap())
    }

    fun valuesEqual(form: SettingsForm): Boolean {
        return all { (field, value) ->
            form[field].text == value.text
        }
    }


    override fun iterator(): Iterator<Pair<SettingsField, SettingsFieldValue>> {
        return SettingsField.values().map { it to this[it] }.iterator()
    }

    override fun toString(): String {
        return buildString {
            append("SettingsForm([")
            this@SettingsForm.forEach { (k, v) ->
                append("$k(text=${v.text}, error=${v.isError}), ")
            }
            append("])")
        }
    }

    companion object {
        val Default = SettingsField.values()
            .associateWith { SettingsFieldValue("", false, null) }
    }
}

data class SettingsFieldValue(
    val text: String,
    val isError: Boolean,
    val errorString: String?,
)

enum class SettingsField(val label: String, val type: SettingsFieldType) {
    Files("Files", SettingsFieldType.Folder),
    Models("Models", SettingsFieldType.Folder),
    Backgrounds("Backgrounds", SettingsFieldType.Folder),
    Sounds("Sounds", SettingsFieldType.Folder),
    TitleScreens("Title Screens", SettingsFieldType.Folder),
    Locale("Locale", SettingsFieldType.File),
    ModelInfo("Model Info", SettingsFieldType.File)
}

enum class SettingsFieldType {
    File, Folder
}

enum class SettingsPreset(val label: String, val pkg: DestinyChildPackage?) {
    Custom("Custom", null),
    Korea("Korea", DestinyChildPackage.KOREA),
    Japan("Japan", DestinyChildPackage.JAPAN),
    Global("Global", DestinyChildPackage.GLOBAL);
}