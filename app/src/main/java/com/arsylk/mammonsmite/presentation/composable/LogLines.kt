package com.arsylk.mammonsmite.presentation.composable

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arsylk.mammonsmite.model.common.LogLine
import com.arsylk.mammonsmite.presentation.AppMaterialTheme


@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun LogLinesDialog(
    list: List<LogLine>,
    onDismiss: () -> Unit
) {
    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
        onDismissRequest = onDismiss,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .defaultMinSize(minHeight = 320.dp)
                .background(
                    color = MaterialTheme.colors.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(2.dp, MaterialTheme.colors.primaryVariant, RoundedCornerShape(16.dp))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colors.primary,
                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .border(
                            2.dp,
                            MaterialTheme.colors.primaryVariant,
                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .padding(16.dp)
                        .height(32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Log",
                        fontSize = 20.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    LogLines(list)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogLines(list: List<LogLine>) {
    Box(modifier = Modifier.fillMaxWidth()) {
        CompositionLocalProvider(LocalOverScrollConfiguration provides null) {
            LazyColumn(
                modifier = Modifier
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