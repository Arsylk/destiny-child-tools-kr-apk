package com.arsylk.mammonsmite.presentation.composable

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arsylk.mammonsmite.presentation.AppMaterialTheme
import com.arsylk.mammonsmite.presentation.dialog.pck.unpack.PckUnpackDialog

@Preview
@Composable
fun x() {
    AppMaterialTheme {
        Box(Modifier.background(Color.White)) {
            var expanded by remember { mutableStateOf(true) }
            ActionButton(
                expanded = expanded,
                actions = listOf(
                    object : ActionButtonItem {
                        override val label = "Action 1"
                        override val icon = Icons.Default.Face
                    },
                    object : ActionButtonItem {
                        override val label = "A lot Action 2"
                        override val icon = Icons.Default.Umbrella
                    },
                    object : ActionButtonItem {
                        override val label = "Action 3"
                        override val icon = null
                    },
                ),
                onClick = { expanded = !expanded },
                onActionClick = {},
            )
        }
    }
}

@Composable
fun <T : ActionButtonItem> ActionButton(
    expanded: Boolean,
    actions: List<T>,
    onClick: () -> Unit,
    onActionClick: (T) -> Unit
) {
    val rotation by animateFloatAsState(targetValue = if (expanded) 270.0f else 0.0f)

    Column {
        Column(
            modifier = Modifier.wrapContentHeight()
        ) {
            actions.forEachIndexed { i, action ->
                ActionButtonItem(
                    action = action,
                    expanded = expanded,
                    pos = i,
                    count = actions.size,
                    onActionClick = onActionClick,
                )
            }
        }


        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier
                .align(Alignment.End)
                .size(fabSize),
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
internal fun <T : ActionButtonItem> ColumnScope.ActionButtonItem(
    action: T,
    expanded: Boolean,
    pos: Int,
    count: Int,
    onActionClick: (T) -> Unit,
) {
    val alpha by animateFloatAsState(if (expanded) 1.0f else 0.0f)
    val shadow by animateDpAsState(if (expanded) 8.dp else 0.dp)
    val offsetY by animateDpAsState(
        if (expanded) 0.dp else (rowSize + rowOffsetSize) * (count - pos) + (rowSize / 4)
    )
    val icon = action.icon

    Column(
        modifier = Modifier
            .offset(y = offsetY)
            .align(Alignment.End),
    ) {
        Row(
            modifier = Modifier
                .alpha(alpha),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            Box {
                Card(
                    modifier = Modifier
                        .shadow(
                            elevation = shadow,
                            shape = RoundedCornerShape(4.dp),
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onActionClick(action) },
                    shape = RoundedCornerShape(4.dp),
                    elevation = 2.dp,
                ) {
                    Text(
                        text = action.label,
                        modifier = Modifier
                            .padding(
                                vertical = 4.dp, horizontal = 8.dp
                            ),
                        style = MaterialTheme.typography.subtitle1,
                        fontSize = 13.sp,
                    )
                }
            }
            Box(modifier = Modifier.width(fabSize)) {
                Surface(
                    modifier = Modifier
                        .size(rowSize)
                        .align(Alignment.Center)
                        .shadow(
                            elevation = shadow,
                            shape = CircleShape,
                        )
                        .clickable { onActionClick(action) },
                    shape = CircleShape,
                    color = MaterialTheme.colors.secondary,
                ) {
                    if (icon != null) Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(rowOffsetSize))
    }
}

internal inline val fabSize get() = 56.dp
internal inline val rowSize get() = 36.dp
internal inline val rowOffsetSize get() = 12.dp

interface ActionButtonItem {
    val label: String
    val icon: ImageVector? get() = null
}