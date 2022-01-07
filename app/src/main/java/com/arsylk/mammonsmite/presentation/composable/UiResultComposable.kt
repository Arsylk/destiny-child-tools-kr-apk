package com.arsylk.mammonsmite.presentation.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arsylk.mammonsmite.model.common.LogLine
import com.arsylk.mammonsmite.model.common.UiResult



@ExperimentalFoundationApi
@JvmName("UiResultBoxOptional")
@Composable
fun <T> UiResultBox(
    uiResult: UiResult<T>?,
    modifier: Modifier = Modifier,
    onEmpty: @Composable (BoxScope.() -> Unit)? = null,
    onLoading: @Composable (BoxScope.() -> Unit)? = null,
    onFailure: @Composable (BoxScope.(Throwable) -> Unit)? = null,
    onSuccess: @Composable BoxScope.(T) -> Unit,
) {
    UiResultBox(
        uiResult = uiResult,
        modifier = modifier,
        onEmpty = onEmpty ?: {},
        onLoading = onLoading ?: {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        },
        onFailure = onFailure ?: { t ->
            Box(Modifier.fillMaxSize()) {
                LogLines(remember(t) { listOf(LogLine(t)) })
            }
        },
        onSuccess = onSuccess,
    )
}

@Composable
private fun <T> UiResultBox(
    uiResult: UiResult<T>?,
    modifier: Modifier = Modifier,
    onEmpty: @Composable BoxScope.() -> Unit,
    onLoading: @Composable BoxScope.() -> Unit,
    onFailure: @Composable BoxScope.(Throwable) -> Unit,
    onSuccess: @Composable BoxScope.(T) -> Unit,
) {
    Box(
        modifier = modifier.then(Modifier.fillMaxSize())
    ) {
        when (uiResult) {
            is UiResult.Success -> onSuccess(uiResult.value)
            is UiResult.Loading -> onLoading()
            is UiResult.Failure -> onFailure(uiResult.throwable)
            else -> onEmpty()
        }
    }
}