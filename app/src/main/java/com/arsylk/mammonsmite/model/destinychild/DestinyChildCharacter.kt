package com.arsylk.mammonsmite.model.destinychild

import com.arsylk.mammonsmite.model.api.response.CharData

data class DestinyChildCharacter(
    val viewIdx: ViewIdx,
    val resolvedName: String? = null,
    val data: CharData? = null,
) {
    val name: String get() = resolvedName
        ?: data?.koreanName
        ?: viewIdx.string
}