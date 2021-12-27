package com.arsylk.mammonsmite.presentation.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun InputFloatSlider(
    label: String = "",
    value: Float = 1.0f,
    valueRange: ClosedFloatingPointRange<Float> = 0.0f..2.0f,
    formatter: (Float) -> String = { String.format("%.2f", it) },
    onValueChanged: (Float) -> Unit = {},
) {
    Column {
        Text(label, modifier = Modifier.padding(4.dp))
        Row {
            Slider(
                value = value,
                valueRange = valueRange,
                onValueChange = onValueChanged,
                modifier = Modifier.weight(1.0f)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = formatter.invoke(value),
                modifier = Modifier
                    .width(56.dp)
                    .align(Alignment.CenterVertically),
                textAlign = TextAlign.End
            )
            Spacer(Modifier.width(4.dp))
        }
    }
}