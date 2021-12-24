package com.arsylk.mammonsmite.presentation.composable

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arsylk.mammonsmite.model.common.LogLine
import com.arsylk.mammonsmite.presentation.AppMaterialTheme


@ExperimentalFoundationApi
@Composable
fun LogLines(list: List<LogLine>) {
    Box(modifier = Modifier.fillMaxWidth()) {
        CompositionLocalProvider(LocalOverScrollConfiguration provides null) {
            LazyColumn(modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            ) {
                items(list) {
                    LogLineItem(it)
                }
            }
        }
    }
}

@Composable
fun LogLineItem(line: LogLine) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { expanded = !expanded },
            )
    ) {
        Text(
            text = line.msg,
            color = line.type.color,
            maxLines = 1,
            fontFamily = AppMaterialTheme.ConsoleFontFamily,
            modifier = Modifier.fillMaxWidth()
        )
        val stackTrace = line.throwable?.stackTraceToString()
        if (stackTrace != null && expanded)
            Text(
                text = stackTrace,
                fontFamily = AppMaterialTheme.ConsoleFontFamily,
                modifier = Modifier.fillMaxWidth()
            )
    }
}