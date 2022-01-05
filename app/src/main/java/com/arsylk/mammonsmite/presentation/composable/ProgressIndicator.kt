package com.arsylk.mammonsmite.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex

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