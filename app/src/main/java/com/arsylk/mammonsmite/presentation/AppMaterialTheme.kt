package com.arsylk.mammonsmite.presentation

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import com.arsylk.mammonsmite.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight


object AppMaterialTheme {
    val DarkColors = darkColors(
        primary = Color(0xffe65100L),
        primaryVariant = Color(0xffA13800L),
        onPrimary = Color(0xffffffffL),
        secondary = Color(0xffff9800L),
        secondaryVariant = Color(0xffB26A00L),
        onSecondary = Color(0xffffffffL),
        error = Color(0xffd32f2fL),
    )

    val OxygenFontFamily = FontFamily(
        Font(R.font.oxygen_regular, FontWeight.Normal),
        Font(R.font.oxygen_bold, FontWeight.Bold),
        Font(R.font.oxygen_light, FontWeight.Light),
    )

    val OxygenMonoFontFamily = FontFamily(
        Font(R.font.oxygen_mono)
    )

    val ConsoleFontFamily = FontFamily(
        Font(R.font.inconsolata_regular)
    )

    val Typography = Typography(
        defaultFontFamily = OxygenFontFamily,
    )
}


@Composable
fun AppMaterialTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = AppMaterialTheme.DarkColors,
        typography = AppMaterialTheme.Typography,
        content = content,
    )
}