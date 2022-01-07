package com.arsylk.mammonsmite.model.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*


sealed class UiResult<T> {
    data class Success<T>(val value: T) : UiResult<T>()
    class Loading<T> : UiResult<T>()
    data class Failure<T>(val throwable: Throwable) : UiResult<T>()

    inline val isSuccess get() = this is Success
    inline val isLoading get() = this is Loading
    inline val nullableValue get() = (this as? Success)?.value
}

fun <T> UiResult(value: T): UiResult<T> = UiResult.Success<T>(value)

fun <T> UiResult(throwable: Throwable): UiResult<T> = UiResult.Failure<T>(throwable)

fun <T> uiResultOf(block: suspend () -> T): Flow<UiResult<T>> {
    return flow<UiResult<T>> {
            val result = block.invoke()
            currentCoroutineContext().ensureActive()
            emit(UiResult.Success<T>(result))
        }
        .onStart {
            emit(UiResult.Loading<T>())
        }
        .catch {
            currentCoroutineContext().ensureActive()
            emit(UiResult.Failure<T>(it))
        }
}

fun <T, R> Flow<T>.mapAsUiResult(block: suspend (T) -> R) =
    flatMapLatest { uiResultOf { block.invoke(it) } }

@Composable
fun <T> produceUiResult(vararg keys: Any?, block: suspend () -> T): State<UiResult<T>> {
    return produceState<UiResult<T>>(keys = keys, initialValue = UiResult.Loading()) {
        value = UiResult.Loading()
        uiResultOf(block).collect {
            ensureActive()
            value = it
        }
    }
}
@Composable
fun <T> produceNullableUiResult(vararg keys: Any?, block: suspend () -> T?): State<UiResult<T>> {
    return produceState<UiResult<T>>(keys = keys, initialValue = UiResult.Loading()) {
        value = UiResult.Loading()
        uiResultOf<T?>(block).collect {
            ensureActive()
            value = when (it) {
                is UiResult.Success -> {
                    if (it.value != null) UiResult(it.value)
                    else UiResult.Loading()
                }
                is UiResult.Loading -> UiResult.Loading()
                is UiResult.Failure -> UiResult(it.throwable)
            }
        }
    }
}