package com.arsylk.mammonsmite.presentation.dialog.result.file

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.domain.onEffect
import com.arsylk.mammonsmite.domain.use
import com.arsylk.mammonsmite.model.common.UiResult
import com.arsylk.mammonsmite.model.file.*
import com.arsylk.mammonsmite.presentation.composable.ActionButton
import com.arsylk.mammonsmite.presentation.composable.ActionButtonItem
import com.arsylk.mammonsmite.presentation.composable.UiResultBox
import com.arsylk.mammonsmite.presentation.dialog.result.ResultDialogAction
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf


@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun SelectFileDialog(
    type: FileSelect,
    allowed: Collection<Action> = emptySet(),
    actions: ResultDialogAction<IFile>
) {
    val viewModel by viewModel<ResultFileViewModel> { parametersOf(type) }
    val title by viewModel.title.collectAsState()
    val items by viewModel.items.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()

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
                TopBar(title = title)
                Divider()
                Box(Modifier.weight(1.0f)) {
                    Content(
                        uiResult = items,
                        selectedItem = selectedItem,
                        onItemClick = viewModel::onItemClick,
                    )

                    val list = Action.values().filter { it in allowed }
                    if (list.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            var expanded by remember(title, selectedItem) { mutableStateOf(false) }
                            ActionButton(
                                expanded = expanded,
                                actions = list,
                                onClick = { expanded = !expanded },
                                onActionClick = { action ->
                                    expanded = false
                                    viewModel.requestAction(action)
                                },
                            )
                        }
                    }
                }
                Divider()
                BottomBar(selectedItem = selectedItem, onCancel = actions::dismiss) { item ->
                    viewModel.onItemClick(item)
                }
            }
        }
    }


    var requestedAction by remember { mutableStateOf<Action?>(null) }
    viewModel.onEffect { effect ->
        when (effect) {
            is Effect.FileSelected -> actions.select(effect.file)
            Effect.Dismiss -> actions.dismiss()
            is Effect.ActionRequested -> requestedAction = effect.action
        }
    }
    use(requestedAction) { action ->
        when(action) {
            Action.NewFile, Action.NewFolder -> {
                val filetype =  if (action == Action.NewFile) FileTypeFile else FileTypeFolder
                NewFileAction(
                    type = filetype,
                    validate = { value ->
                        viewModel.validateNewFile(value, filetype)
                    },
                    onDismiss = { requestedAction = null },
                    onConfirm = { value ->
                        requestedAction = null
                        viewModel.createNewFile(value, filetype)
                    }
                )
            }
        }
    }

    BackHandler { viewModel.navigateUp() }
}

@Composable
internal fun TopBar(title: String) {
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

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
internal fun Content(
    uiResult: UiResult<List<ResultFileItem>>,
    selectedItem: ResultFileItem?,
    onItemClick: (ResultFileItem) -> Unit,
) {
    UiResultBox(uiResult = uiResult) { items ->
        if (items.isNotEmpty()) LazyColumn(Modifier.fillMaxSize()) {
            items(items) { item ->
                val alpha = if (item.enabled) ContentAlpha.high else ContentAlpha.disabled
                val alphaFloat by animateFloatAsState(if (item == selectedItem) 0.125f else 0.0f)

                CompositionLocalProvider(
                    LocalContentAlpha provides alpha
                ) {
                    ListItem(
                        text = { Text(item.label) },
                        icon = { Icon(item.type.icon, contentDescription = null) },
                        modifier = Modifier
                            .background(
                                MaterialTheme.colors.onSurface.copy(alpha = alphaFloat)
                            )
                            .clickable(item.enabled) {
                                onItemClick.invoke(item)
                            }
                    )
                }
            }
        } else Text(
            text = "Empty",
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
internal fun BottomBar(
    selectedItem: ResultFileItem?,
    onCancel: () -> Unit,
    onSelect: (ResultFileItem) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(16.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        OutlinedButton(onClick = onCancel) {
            Text("Cancel")
        }
        Spacer(Modifier.width(16.dp))
        OutlinedButton(
            enabled = selectedItem != null,
            onClick = { selectedItem?.also(onSelect) },
            content = { Text("Select") },
        )
    }
}

@Composable
internal fun NewFileAction(
    type: FileType,
    validate: (String) -> NewFileError?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf("") }
    val error = remember(value) { validate(value) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New ${type.label}") },
        confirmButton = {
            Button(
                onClick = { onConfirm(value) },
                enabled = error == null,
            ) {
                Text("Ok")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            Column {
                Text(
                    text = error?.label ?: "",
                    color = MaterialTheme.colors.error,
                )
                TextField(
                    value = value,
                    onValueChange = { value = it },
                    isError = error != null,
                    singleLine = true,
                    colors = textFieldColors(
                        backgroundColor = Color.Transparent,
                    )
                )
            }
        }
    )
}

enum class Action(
    override val label: String,
    override val icon: ImageVector,
) : ActionButtonItem {
    NewFile("New File", Icons.Default.NoteAdd),
    NewFolder("New Folder", Icons.Default.CreateNewFolder),
}