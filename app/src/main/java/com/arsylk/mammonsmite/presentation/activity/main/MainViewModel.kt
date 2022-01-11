package com.arsylk.mammonsmite.presentation.activity.main

import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.repo.CharacterRepository
import com.arsylk.mammonsmite.domain.sync.SyncService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest

class MainViewModel(
    private val syncService: SyncService,
): EffectViewModel<Effect>() {
    private val _progress = MutableStateFlow(0)
    val progress by lazy(_progress::asStateFlow)

    init { load() }

    fun load() {
        withLoading(tag = "sync") {
            syncService.getSyncFlow()
                .collectLatest { (store, int) ->
                    _progress.value = int
                }
        }
    }
}

sealed class Effect : UiEffect