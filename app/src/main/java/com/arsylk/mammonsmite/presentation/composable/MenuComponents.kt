package com.arsylk.mammonsmite.presentation.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MenuItem(
    text: String,
    icon: @Composable BoxScope.() -> Unit = {},
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        onClick = onClick,
        role = Role.Button,
    ) {
        Row {
            CompositionLocalProvider(
                LocalContentAlpha provides if (selected) ContentAlpha.high else ContentAlpha.medium,
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(8.dp),
                    content = icon
                )
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .align(Alignment.CenterVertically),
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.subtitle2,
                    )
                }
            }

        }
    }
}

@Composable
fun MenuDivider(text: String? = null) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        if (text != null) Text(
            text = text,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.48f),
            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
        )
        Divider(
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
        )
    }
}