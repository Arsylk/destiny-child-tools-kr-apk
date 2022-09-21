package com.arsylk.mammonsmite.domain.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class EffectViewModel<Effect : UiEffect> : BaseViewModel() {
    private val _effect = Channel<Effect>()
    val effect = _effect.receiveAsFlow()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    protected suspend fun setEffect(effect: Effect) {
        _effect.send(effect)
    }

    fun enqueueEffect(effect: Effect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}

interface UiEffect