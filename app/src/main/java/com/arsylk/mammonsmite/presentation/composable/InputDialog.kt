package com.arsylk.mammonsmite.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arsylk.mammonsmite.model.common.InputField


@Composable
fun InputDialogContent(title: String = "Test", content: @Composable BoxScope.() -> Unit = {}) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth(0.9f)
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
                    text = title,
                    fontSize = 20.sp
                )
            }
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), content = content)
        }
    }
}

@Composable
fun InputDialogField(
    input: InputField<String>,
    labelText: String? = null,
    showErrorText: Boolean = true,
    onValueChange: (InputField<String>) -> Unit,
) {
    InputDialogField(
        value = input.value,
        labelText = labelText,
        isError = input.isError,
        errorText = input.errorText,
        showErrorText = showErrorText,
    ) {
        onValueChange(
            input.copy(
                value = it
            )
        )
    }
}

@Composable
fun InputDialogField(
    value: String,
    labelText: String? = null,
    isError: Boolean = false,
    errorText: String? = null,
    showErrorText: Boolean = true,
    onValueChange: (String) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            label = { if (labelText != null) Text(labelText) },
            singleLine = true,
            isError = isError,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
        )
        if (showErrorText) Text(
            text = if (isError) errorText ?: "" else "",
            color = MaterialTheme.colors.error,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}