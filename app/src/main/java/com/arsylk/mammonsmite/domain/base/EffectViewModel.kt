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

abstract class EffectViewModel<Effect : UiEffect> : ViewModel() {
    private val _effect = Channel<Effect>()
    private val _isLoadingTags = MutableStateFlow(emptySet<String>())
    val effect = _effect.consumeAsFlow()
        .shareIn(viewModelScope, SharingStarted.Lazily)
    val isLoading = _isLoadingTags.toIsLoading()

    protected suspend fun setEffect(effect: Effect) {
        _effect.send(effect)
    }

    protected fun enqueueEffect(effect: Effect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    protected fun withLoading(
        context: CoroutineContext = Dispatchers.IO,
        tag: String = LOADING_TAG,
        block: suspend CoroutineScope.() -> Unit
    ) {
        _isLoadingTags.update { it + tag }
        viewModelScope.launch(context) { kotlin.runCatching { block.invoke(this) } }
            .invokeOnCompletion {
                _isLoadingTags.update { it - tag }
            }
    }

    private fun Flow<Set<String>>.toIsLoading() =
        mapLatest(Set<String>::isNotEmpty)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    companion object {
        private const val LOADING_TAG = "default_loading"
    }
}

interface UiEffect