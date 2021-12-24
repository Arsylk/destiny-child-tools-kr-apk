package com.arsylk.mammonsmite.model.destinychild


data class DestinyChildCharacter(
    val viewIdx: ViewIdx,
    val resolvedName: String? = null,
    val data: CharData? = null,
) {
    val name: String get() = resolvedName
        ?: data?.koreanName
        ?: viewIdx.string
}