package com.arsylk.mammonsmite.presentation.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SurfaceBox(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val m = modifier.then(Modifier.fillMaxSize())
    Surface(m) { Box(m, content = content) }
}

@Composable
fun SurfaceColumn(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val m = modifier.then(Modifier.fillMaxSize())
    Surface(m) { Column(m, content = content) }
}