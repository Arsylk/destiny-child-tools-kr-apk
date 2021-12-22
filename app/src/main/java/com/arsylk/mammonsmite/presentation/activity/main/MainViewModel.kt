package com.arsylk.mammonsmite.presentation.activity.main

import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.repo.CharacterRepository

class MainViewModel(
    private val repo: CharacterRepository,
): EffectViewModel<Effect>() {

    fun load() {
        withLoading(tag = "repo") { repo.fetchAll() }
    }
}

sealed class Effect : UiEffect