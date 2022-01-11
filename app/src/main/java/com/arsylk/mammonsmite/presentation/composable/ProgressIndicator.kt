package com.arsylk.mammonsmite.presentation.composable

import androidx.annotation.IntRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex

@Composable
fun BoxScope.NonBlockingProgressIndicator(
    @IntRange(from = 0, to = 100) progress: Int,
    hideOnFull: Boolean = true,
    color: Color = MaterialTheme.colors.primaryVariant,
) {
    val percent = remember(progress) { progress.toFloat() / 100 }
    if (!hideOnFull || progress < 100) Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .zIndex(999.0f)
            .fillMaxWidth()
    ) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            color = color,
            progress = percent
        )
    }
}
@Composable
fun BoxScope.NonBlockingProgressIndicator(
    isLoading: Boolean,
    color: Color = MaterialTheme.colors.primaryVariant,
) {
    if (isLoading) Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .zIndex(999.0f)
            .fillMaxWidth()
    ) {
        LinearProgressIndicator(
            color = color,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun BoxScope.BlockingProgressIndicator(
    isLoading: Boolean,
    color: Color = MaterialTheme.colors.primaryVariant,
) {
    if (isLoading) Box(
        modifier = Modifier
            .zIndex(999.0f)
            .background(MaterialTheme.colors.surface.copy(alpha = 0.6f))
            .fillMaxSize()
    ) {
        CircularProgressIndicator(
            color = color,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}