package com.arsylk.mammonsmite.presentation.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex


@Composable
fun ClickCapture(enabled: Boolean = true, onClick: () -> Unit) {
    if (!enabled) return
    Box(
        Modifier
            .fillMaxSize()
            .zIndex(10000.0f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
    )
}