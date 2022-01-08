package com.arsylk.mammonsmite.presentation.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp


@Composable
fun SimpleDropdownMenu(
    current: String,
    enabled: Boolean = true,
    content: @Composable DropdownMenuScope.() -> Unit
) {
    val state = remember { mutableStateOf(false) }
    var expanded by state
    val rotation by animateFloatAsState(targetValue = if (expanded) -180.0f else 0.0f)
    val alpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
    CompositionLocalProvider(LocalContentAlpha provides alpha) {
        Column {
            Row(
                modifier = Modifier
                    .background(
                        MaterialTheme.colors.surface,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colors.primary,
                        RoundedCornerShape(8.dp)
                    )
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { expanded = !expanded },
            ) {
                Text(current, Modifier.padding(12.dp))
                Spacer(Modifier.weight(1.0f))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .align(Alignment.CenterVertically)
                        .rotate(rotation)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                val scope = remember { DropdownMenuScope(state, this) }
                content(scope)
            }
        }
    }
}

@Composable
fun DropdownMenuScope.SimpleDropdownMenuItem(
    label: String,
    autoCollapse: Boolean = true,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        onClick = {
            if (autoCollapse) expanded = false
            onClick.invoke()
        },
    ) {
        Text(label, Modifier.padding(12.dp))
    }
}

class DropdownMenuScope(state: MutableState<Boolean>, scope: ColumnScope) : ColumnScope by scope {
    var expanded by state
}