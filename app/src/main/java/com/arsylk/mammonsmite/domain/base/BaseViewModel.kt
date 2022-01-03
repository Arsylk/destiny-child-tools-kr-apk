package com.arsylk.mammonsmite.domain.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel() {
    private val _isLoadingTags = MutableStateFlow(emptySet<String>())
    val isLoading = _isLoadingTags.toIsLoading()


    protected fun withLoading(
        context: CoroutineContext = Dispatchers.IO,
        tag: String = LOADING_TAG,
        block: suspend CoroutineScope.() -> Unit
    ) {
        setLoading(isLoading = true, tag)
        viewModelScope.launch(context) {
            kotlin.runCatching { block.invoke(this) }
                .getOrElse { it.printStackTrace() }
        }
            .invokeOnCompletion {
                setLoading(isLoading = false, tag)
            }
    }

    protected fun setLoading(isLoading: Boolean, tag: String = LOADING_TAG) {
        _isLoadingTags.update { if (isLoading) it + tag else it - tag }
    }

    private fun Flow<Set<String>>.toIsLoading() =
        mapLatest(Set<String>::isNotEmpty)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    companion object {
        private const val LOADING_TAG = "default_loading"
    }
}