package com.arsylk.mammonsmite.domain.destinychild

import com.arsylk.mammonsmite.model.destinychild.LocalePatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class EngLocaleRepository {
    private val _engLocale = MutableStateFlow<LocalePatch?>(null)
    val engLocale by lazy(_engLocale::asStateFlow)

    fun setEngLocale(patch: LocalePatch) {
        _engLocale.value = patch
    }

}