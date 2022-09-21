package com.arsylk.mammonsmite.presentation.dialog.result

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp


@Composable
fun ResultDialogScaffold(
    title: String,
    bottomBar: @Composable RowScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.9f)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .background(
                    color = MaterialTheme.colors.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(2.dp, MaterialTheme.colors.primaryVariant, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                ResultDialogTopBar(title = title)
                Divider()
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxWidth(),
                    content = content,
                )
                Divider()
                ResultDialogBottomBar(content = bottomBar)
            }
        }
    }
}

@Composable
fun ResultDialogTopBar(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .background(MaterialTheme.colors.primary)
            .horizontalScroll(rememberScrollState())
    ) {
        Text(
            text = title,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.h6,
        )
    }
}

@Composable
fun ResultDialogBottomBar(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(16.dp),
        horizontalArrangement = Arrangement.End,
        content = content,
    )
}