package com.arsylk.mammonsmite.presentation.screen.locale.patch

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.domain.onEffect
import com.arsylk.mammonsmite.domain.toSnackbarMessage
import com.arsylk.mammonsmite.domain.use
import com.arsylk.mammonsmite.model.file.FileSelect
import com.arsylk.mammonsmite.model.common.UiResult
import com.arsylk.mammonsmite.model.destinychild.LocalePatch
import com.arsylk.mammonsmite.presentation.Navigable.Companion.putArg
import com.arsylk.mammonsmite.presentation.Navigator
import com.arsylk.mammonsmite.presentation.composable.NonBlockingProgressIndicator
import com.arsylk.mammonsmite.presentation.composable.SurfaceColumn
import com.arsylk.mammonsmite.presentation.composable.UiResultBox
import com.arsylk.mammonsmite.presentation.dialog.result.file.Action
import com.arsylk.mammonsmite.presentation.dialog.result.file.ResultFileDialog
import com.arsylk.mammonsmite.presentation.nav
import com.arsylk.mammonsmite.presentation.screen.NavigableScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.androidx.compose.getViewModel
import java.io.File


@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalSerializationApi
@ExperimentalFoundationApi
object LocalePatchScreen : NavigableScreen {
    override val route = "/locale/patch?load={load}"
    override val label = "Patch Locale"
    override val args = listOf(
        navArgument("load") {
            type = NavType.StringType
            nullable = true
        }
    )

    fun navigate(nav: Navigator, load: File? = null, builder: NavOptionsBuilder.() -> Unit = {}) {
        val route = if (load != null) route.putArg("load", load.absolutePath) else route
        nav.controller.navigate(route) {
            launchSingleTop = true
            apply(builder)
        }
    }

    @Composable
    override fun Compose(entry: NavBackStackEntry) {
        val loadFile = remember { entry.arguments?.getString("load")?.let(::File) }
        LocalePatchScreen(
            loadFile = loadFile
        )
    }

    enum class ResultState { EMPTY, LOADING, READY, ERROR }
}

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalSerializationApi
@Composable
internal fun LocalePatchScreen(
    viewModel: LocalePatchViewModel = getViewModel(),
    loadFile: File? = null,
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val scaffoldState = rememberScaffoldState()

    val sourceSrc by viewModel.sourceSrc.collectAsState()
    val sourcePatch by viewModel.sourcePatch.collectAsState()
    val patchSrc by viewModel.patchSrc.collectAsState()
    val patchPatch by viewModel.patchPatch.collectAsState()

    val itemApplied by viewModel.itemApplied.collectAsState(null)
    val destination by viewModel.destination.collectAsState()

    Scaffold(scaffoldState = scaffoldState) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NonBlockingProgressIndicator(isLoading)
            SurfaceColumn(Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.weight(1.0f)) {
                        SourceSelector(
                            source = sourceSrc,
                            label = "Source",
                            showGame = true,
                            showRemote = false,
                            enabled = !sourcePatch.isLoading,
                            onSourceSelected = viewModel::setSourceSrc,
                        )
                        Spacer(Modifier.height(8.dp))
                        PatchItemDescription(uiResult = sourcePatch)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1.0f)) {
                        SourceSelector(
                            source = patchSrc,
                            label = "Patch",
                            showGame = false,
                            showRemote = true,
                            enabled = !patchPatch.isLoading,
                            onSourceSelected = viewModel::setPatchSrc,
                        )
                        Spacer(Modifier.height(8.dp))
                        PatchItemDescription(uiResult = patchPatch)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Column(Modifier.weight(1.0f)) {
                    DestinationSelector(
                        destination = destination,
                        enabled = itemApplied?.isSuccess == true,
                        onDestinationSelected = viewModel::setDestination,
                    )
                    Spacer(Modifier.height(8.dp))
                    PatchAppliedDescription(
                        uiResult = itemApplied,
                        modifier = Modifier.weight(1.0f)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        modifier = Modifier.align(Alignment.End),
                        enabled = itemApplied?.isSuccess == true,
                        onClick = {
                            use(itemApplied?.nullableValue) { applied ->
                                viewModel.savePatchTo(applied, destination)
                            }
                        },
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }

    viewModel.onEffect { effect ->
        when (effect) {
            is Effect.Success -> scaffoldState.snackbarHostState.showSnackbar(effect.path)
            is Effect.Failure -> scaffoldState.snackbarHostState
                .showSnackbar(effect.throwable.toSnackbarMessage())
        }
    }

    LaunchedEffect(loadFile) {
        if (loadFile != null) {
            viewModel.setPatchSrc(
                PatchSource.Local(IFile(loadFile), LocalLocalePatch.PCK)
            )
        }
    }
}

@ExperimentalSerializationApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
internal fun SourceSelector(
    source: PatchSource,
    label: String,
    enabled: Boolean,
    showGame: Boolean,
    showRemote: Boolean,
    onSourceSelected: (PatchSource) -> Unit,
) {
    val dialogHost = nav.resultDialogHost
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) -180.0f else 0.0f)

    val alpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
    CompositionLocalProvider(LocalContentAlpha provides alpha) {
        Column {
            Text(text = label)
            Spacer(Modifier.height(8.dp))
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
                Text(source.label, Modifier.padding(12.dp))
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
                if (showGame) DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSourceSelected.invoke(PatchSource.Game)
                    }
                ) {
                    Text(PatchSource.Game.label, Modifier.padding(12.dp))
                }
                if (showRemote) for (type in RemoteLocalePatch.values()) {
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            onSourceSelected.invoke(PatchSource.Remote(type))
                        }
                    ) {
                        Text(type.label, Modifier.padding(12.dp))
                    }
                }
                for (type in LocalLocalePatch.values()) {
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            scope.launch {
                                val file = dialogHost.showResultDialog {
                                    ResultFileDialog(FileSelect.FILE)
                                }
                                if (file != null) {
                                    onSourceSelected.invoke(PatchSource.Local(file, type))
                                }
                            }
                        }
                    ) {
                        Text(type.label, Modifier.padding(12.dp))
                    }
                }
            }
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
internal fun DestinationSelector(
    destination: PatchDestination,
    enabled: Boolean,
    onDestinationSelected: (PatchDestination) -> Unit,
) {
    val dialogHost = nav.resultDialogHost
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) -180.0f else 0.0f)

    val alpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
    CompositionLocalProvider(LocalContentAlpha provides alpha) {
        Column {
            Text(text = "Destination")
            Spacer(Modifier.height(8.dp))
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
                Text(destination.label, Modifier.padding(12.dp))
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
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onDestinationSelected(PatchDestination.Game)
                    }
                ) {
                    Text(PatchDestination.Game.label, Modifier.padding(12.dp))
                }
                for (type in LocalLocalePatch.values()) {
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            scope.launch {
                                val file = dialogHost.showResultDialog {
                                    ResultFileDialog(
                                        type = FileSelect.FILE,
                                        allowed = setOf(Action.NewFile),
                                    )
                                }
                                if (file != null) when (type) {
                                    LocalLocalePatch.JSON -> onDestinationSelected(PatchDestination.Json(file))
                                    LocalLocalePatch.PCK -> onDestinationSelected(PatchDestination.Pck(file))
                                }
                            }
                        }
                    ) {
                        Text(type.label, Modifier.padding(12.dp))
                    }
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun PatchItemDescription(modifier: Modifier = Modifier, uiResult: UiResult<LocalePatch>) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.then(
            Modifier
                .background(
                    color = MaterialTheme.colors.surface,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(2.dp, MaterialTheme.colors.primaryVariant, RoundedCornerShape(8.dp))
                .height(120.dp)
        )
    ) {
        UiResultBox(
            modifier = Modifier.fillMaxSize(),
            uiResult = uiResult
        ) { item ->
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                if (item.name.isNotBlank()) Text(
                    text = item.name,
                    style = MaterialTheme.typography.subtitle1,
                )
                if (!item.date.isNullOrBlank()) Text(
                    text = item.date,
                    style = MaterialTheme.typography.subtitle1,
                )
                Text(
                    text = "Files: ${item.files.size}",
                    style = MaterialTheme.typography.subtitle1,
                )
                Text(
                    text = "Entries: ${item.files.values.sumOf { it.dict.size }}",
                    style = MaterialTheme.typography.subtitle1,
                )
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
internal fun PatchAppliedDescription(modifier: Modifier = Modifier, uiResult: UiResult<PatchAppliedItem>?) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.then(
            Modifier
                .background(
                    color = MaterialTheme.colors.surface,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(2.dp, MaterialTheme.colors.primaryVariant, RoundedCornerShape(8.dp))
                .fillMaxSize()
        )
    ) {
        UiResultBox(
            modifier = Modifier.fillMaxSize(),
            uiResult = uiResult,
            onEmpty = { Text("Nothing selected") },
        ) { applied ->
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Files: ${applied.patch.files.size}")
                Text("Entries: ${applied.patch.files.values.sumOf { it.dict.size }}")
                Text("New Files: ${applied.newFiles}")
                Text("New Keys: ${applied.newKeys}")
                Text("Changed Values: ${applied.changedValues}")
            }
        }
    }
}