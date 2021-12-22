package com.arsylk.mammonsmite.model.destinychild

data class LocalePatch(
    val name: String,
    val files: Map<String, LocalePatchFile>,
) {

    val characterNamesFile get() = files["c40e0023a077cb28"]
}

data class LocalePatchFile(
    val hash: String,
    val type: Type,
    val dict: Map<String, String>,
) {

    enum class Type {
        DICT, TABLE
    }
}