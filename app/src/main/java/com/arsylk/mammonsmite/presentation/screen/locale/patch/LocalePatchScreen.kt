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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.arsylk.mammonsmite.model.common.LogLine
import com.arsylk.mammonsmite.presentation.Navigable.Companion.putArg
import com.arsylk.mammonsmite.presentation.Navigator
import com.arsylk.mammonsmite.presentation.composable.LogLines
import com.arsylk.mammonsmite.presentation.composable.NonBlockingProgressIndicator
import com.arsylk.mammonsmite.presentation.composable.SurfaceColumn
import com.arsylk.mammonsmite.presentation.screen.NavigableScreen
import com.arsylk.mammonsmite.presentation.screen.locale.patch.LocalePatchScreen.ResultState
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.androidx.compose.getViewModel
import java.io.File


@ExperimentalFoundationApi
@ExperimentalSerializationApi
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

@ExperimentalFoundationApi
@ExperimentalSerializationApi
@Composable
internal fun LocalePatchScreen(
    viewModel: LocalePatchViewModel = getViewModel(),
    loadFile: File? = null,
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val scaffoldState = rememberScaffoldState()


    val itemSource by viewModel.itemSource.collectAsState()
    val itemPatch by viewModel.itemPatch.collectAsState()
    Scaffold(scaffoldState = scaffoldState) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NonBlockingProgressIndicator(isLoading)
            SurfaceColumn {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.weight(1.0f)) {
                        SourceSelector(
                            source = itemSource?.source,
                            label = "Source",
                            showRemote = false,
                            enabled = itemSource?.isLoading != true
                        ) { source ->
                            viewModel.loadLocalePatchSource(source)
                        }
                        PatchItemDescription(item = itemSource)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1.0f)) {
                        SourceSelector(
                            source = itemPatch?.source,
                            label = "Patch",
                            showRemote = true,
                            enabled = itemPatch?.isLoading != true
                        ) { source ->
                            viewModel.loadLocalePatchPatch(source)
                        }
                        PatchItemDescription(item = itemPatch)
                    }
                }
                Column(Modifier.weight(1.0f)) {

                }
            }
        }
    }

    LaunchedEffect(loadFile) {
        if (loadFile != null) {
            viewModel.loadLocalePatchSource(PatchSource.Local(loadFile, LocalLocalePatch.PCK))
        }
    }
}

@Composable
fun SourceSelector(
    source: PatchSource?,
    label: String,
    enabled: Boolean,
    showRemote: Boolean,
    onSourceSelected: (PatchSource) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) -180.0f else 0.0f)

    val alpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
    CompositionLocalProvider(LocalContentAlpha provides alpha) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                Text(source?.label ?: "", Modifier.padding(12.dp))
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
fun PatchItemDescription(modifier: Modifier = Modifier, item: PatchItem?) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.then(
            Modifier
                .background(
                    color = MaterialTheme.colors.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(2.dp, MaterialTheme.colors.primaryVariant, RoundedCornerShape(16.dp))
                .height(120.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box {
                when {
                    item == null -> Text(
                        text = "Nothing selected",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    item.isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                    item.throwable != null -> LogLines(
                        list = remember(item) { listOf(LogLine(item.throwable)) }
                    )
                    item.patch != null -> Column {
                        Text(
                            text = item.patch.name,
                            style = MaterialTheme.typography.subtitle1,
                        )
                        if (!item.patch.date.isNullOrBlank()) Text(
                            text = item.patch.date,
                            style = MaterialTheme.typography.subtitle1,
                        )
                        Text(
                            text = "Files: ${item.patch.files.size}",
                            style = MaterialTheme.typography.subtitle1,
                        )
                        Text(
                            text = "Entries: ${item.patch.files.values.sumOf { it.dict.size }}",
                            style = MaterialTheme.typography.subtitle1,
                        )
                    }
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun PatchResultDescription(modifier: Modifier = Modifier, source: PatchItem?, patch: PatchItem?) {
    var state by remember(source, patch) {
        val initial = when {
            source?.patch != null && patch?.source != null -> ResultState.READY
            source?.throwable != null || patch?.throwable != null -> ResultState.ERROR
            source?.isLoading == true || patch?.isLoading == true -> ResultState.LOADING
            else -> ResultState.EMPTY
        }
        mutableStateOf(initial)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.then(
            Modifier
                .background(
                    color = MaterialTheme.colors.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(2.dp, MaterialTheme.colors.primaryVariant, RoundedCornerShape(16.dp))
                .fillMaxSize()
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                when (state) {
                    ResultState.EMPTY -> Text(
                        text = "Nothing selected",
                        style = MaterialTheme.typography.subtitle1,
                    )
                    ResultState.LOADING -> CircularProgressIndicator()
                    ResultState.ERROR -> LogLines(
                        list = remember(source, patch) {
                            listOfNotNull(
                                source?.throwable?.let(::LogLine),
                                patch?.throwable?.let(::LogLine),
                            )
                        }
                    )
                    ResultState.READY -> {
                        Text("ready")
                    }
                }
            }
        }
    }
}