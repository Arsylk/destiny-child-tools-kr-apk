package com.arsylk.mammonsmite.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import com.arsylk.mammonsmite.model.file.FileType
import com.arsylk.mammonsmite.model.file.FileTypeFile
import com.arsylk.mammonsmite.model.file.FileTypeFolder
import com.arsylk.mammonsmite.presentation.Navigator
import com.arsylk.mammonsmite.presentation.composable.*
import com.arsylk.mammonsmite.presentation.dialog.result.file.ResultFileDialog
import com.arsylk.mammonsmite.presentation.nav
import com.arsylk.mammonsmite.presentation.screen.NavigableScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

object SettingsScreen : NavigableScreen {
    override val route = "/settings"
    override val label = "Settings"

    fun navigate(nav: Navigator, builder: NavOptionsBuilder.() -> Unit = {}) {
        nav.controller.navigate(route) {
            launchSingleTop = true
            apply(builder)
        }
    }

    @Composable
    override fun Compose(entry: NavBackStackEntry) {
        SettingsScreen()
    }
}

@Composable
internal fun SettingsScreen(viewModel: SettingsViewModel = getViewModel()) {
    val isLoading by viewModel.isLoading.collectAsState()
    val preset by viewModel.preset.collectAsState()
    val form by viewModel.form.collectAsState()


    val scrollState = rememberScrollState()
    SurfaceBox {
        NonBlockingProgressIndicator(isLoading = isLoading)
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            SimpleDropdownMenu(current = preset.label) {
                for (item in SettingsPreset.values()) {
                    SimpleDropdownMenuItem(label = item.label) {
                        viewModel.applyPreset(item)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            for ((field, value) in form) {
                when (val type = field.type) {
                    SettingsFieldType.File, SettingsFieldType.Folder -> {
                        FileInputField(
                            value = value,
                            type = if (type == SettingsFieldType.File) FileTypeFile else FileTypeFolder,
                            onValueChanged = { string ->
                                viewModel.updateField(field, string)
                            }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            Button(
                modifier = Modifier.align(Alignment.End),
                enabled = !isLoading,
                onClick = viewModel::saveForm,
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
internal fun FileInputField(
    value: SettingsFieldValue,
    type: FileType,
    onValueChanged: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val dialogHost = nav.resultDialogHost

    OutlinedTextField(
        value = value.text,
        onValueChange = onValueChanged,
        isError = value.isError,
        trailingIcon = {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        scope.launch {
                            val file = dialogHost.showResultDialog {
                                ResultFileDialog(type, value.text)
                            }
                            file?.absolutePath?.also(onValueChanged)
                        }
                    },
            ) {
                Icon(
                    type.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(4.dp)
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}