package com.arsylk.mammonsmite.presentation

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import com.arsylk.mammonsmite.R
import androidx.compose.ui.text.font.FontFamily


object AppMaterialTheme {
    val DarkColors = darkColors(
        primary = Color(0xffe65100),
        primaryVariant = Color(0xffA13800),
        onPrimary = Color(0xffffffff),
        secondary = Color(0xffff9800),
        secondaryVariant = Color(0xffB26A00),
        onSecondary = Color(0xffffffff),
        error = Color(0xffd32f2f),
    )

    val ConsoleFontFamily = FontFamily(
        Font(R.font.inconsolata_regular)
    )
}


@Composable
fun AppMaterialTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = AppMaterialTheme.DarkColors,
        content = content,
    )
}