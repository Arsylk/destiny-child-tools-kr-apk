package com.arsylk.mammonsmite.presentation.screen.l2d.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.arsylk.mammonsmite.domain.onEffect
import com.arsylk.mammonsmite.domain.toSnackbarMessage
import com.arsylk.mammonsmite.presentation.Navigable.Companion.putArg
import com.arsylk.mammonsmite.presentation.Navigator
import com.arsylk.mammonsmite.presentation.composable.InputDialog
import com.arsylk.mammonsmite.presentation.composable.InputFloatSlider
import com.arsylk.mammonsmite.presentation.composable.Live2DSurface
import com.arsylk.mammonsmite.presentation.nav
import com.arsylk.mammonsmite.presentation.screen.NavigableScreen
import com.arsylk.mammonsmite.presentation.view.live2d.BackgroundScale
import com.arsylk.mammonsmite.presentation.view.live2d.Live2DSurfaceConfig
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

@ExperimentalComposeUiApi
@ExperimentalSerializationApi
object L2DPreviewScreen : NavigableScreen {
    override val route = "/l2d/preview/{path}"
    override val label = "L2D Preview"
    override val fullscreen = true
    override val args = listOf(
        navArgument("path") { type = NavType.StringType }
    )

    fun navigate(nav: Navigator, path: String, builder: NavOptionsBuilder.() -> Unit = {}) {
        val route = L2DPreviewScreen.route.putArg("path", path)
        nav.controller.navigate(route) {
            launchSingleTop = true
            apply(builder)
        }
    }

    @Composable
    override fun Compose(entry: NavBackStackEntry) {
        val file = remember { File(entry.arguments?.getString("path")!!) }
        val viewModel by viewModel<L2DPreviewViewModel> { parametersOf(file) }

        L2DPreviewScreen(viewModel)
    }
}

@ExperimentalComposeUiApi
@ExperimentalSerializationApi
@Composable
fun L2DPreviewScreen(viewModel: L2DPreviewViewModel) {
    val nav = nav
    val state = rememberScaffoldState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = state,
    ) { padding ->
        var showDialog by remember { mutableStateOf(false) }
        var playMotion by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { playMotion = true },
                        onDoubleTap = { showDialog = true },
                    )
                }
        ) {
            val isLoading by viewModel.isLoading.collectAsState()
            if (isLoading) CircularProgressIndicator(Modifier.align(Alignment.Center))


            val loadedL2dFile by viewModel.loadedL2dFile.collectAsState()
            val surfaceConfig by viewModel.surfaceConfig.collectAsState()

            Live2DSurface(
                loadedL2dFile = loadedL2dFile,
                surfaceConfig = surfaceConfig,
                playMotion = playMotion,
                onMotionPlayed = { playMotion = false }
            ) { offset, zoom ->
                viewModel.updateSurfaceConfig {
                    copy(
                        scale = scale * zoom,
                        offsetX = offsetX + offset.x,
                        offsetY = offsetY + offset.y,
                    )
                }
            }

            if (showDialog) ConfigDialog(
                config = surfaceConfig,
                onUpdateConfig = viewModel::updateSurfaceConfig,
                onDismiss = { showDialog = false }
            )
        }
    }
    viewModel.onEffect { effect ->
        when (effect) {
            is Effect.FatalError -> {
                state.snackbarHostState
                    .showSnackbar(
                        message = effect.t.toSnackbarMessage(),
                        actionLabel = "Close",
                        duration = SnackbarDuration.Indefinite,
                    )
                nav.controller.popBackStack()
            }
        }
    }
}
@ExperimentalComposeUiApi
@Composable
fun ConfigDialog(
    config: Live2DSurfaceConfig,
    onUpdateConfig: (Live2DSurfaceConfig.() -> Live2DSurfaceConfig) -> Unit,
    onDismiss: () -> Unit,
) {
    InputDialog(
        title = "Config",
        onDismissRequest = onDismiss,
    ) {
        ConfigInputForm(config, onUpdateConfig)
    }
}

@Composable
fun ConfigInputForm(
    config: Live2DSurfaceConfig,
    onUpdateConfig: (Live2DSurfaceConfig.() -> Live2DSurfaceConfig) -> Unit,
) {
    Column {
        ConfigInputBackgroundScale(config.bgScale, onUpdateConfig)
        Spacer(Modifier.height(12.dp))
        InputFloatSlider(
            label = "Model scale",
            value = config.scale,
            valueRange = Live2DSurfaceConfig.rangeScale,
            onValueChanged = { onUpdateConfig { copy(scale = it) } }
        )
        Spacer(Modifier.height(12.dp))
        InputFloatSlider(
            label = "Model Offset X",
            value = config.offsetX,
            valueRange = Live2DSurfaceConfig.rangeOffsetX,
            onValueChanged = { onUpdateConfig { copy(offsetX = it) } }
        )
        Spacer(Modifier.height(12.dp))
        InputFloatSlider(
            label = "Model Offset Y",
            value = config.offsetY,
            valueRange = Live2DSurfaceConfig.rangeOffsetY,
            onValueChanged = { onUpdateConfig { copy(offsetY = it) } }
        )
        Spacer(Modifier.height(12.dp))
        Row {
            Row(Modifier.weight(1.0f)) {
                Checkbox(
                    checked = config.animate,
                    onCheckedChange = { onUpdateConfig { copy(animate = it) } },
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                Text("Animate", Modifier.align(Alignment.CenterVertically))
            }
            Row(Modifier.weight(1.0f)) {
                Checkbox(
                    checked = config.tappable,
                    onCheckedChange = { onUpdateConfig { copy(tappable = it) } },
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                Text("Tappable", Modifier.align(Alignment.CenterVertically))
            }
        }
    }
}

@Composable
fun ConfigInputBackgroundScale(
    scale: BackgroundScale,
   onUpdateConfig: (Live2DSurfaceConfig.() -> Live2DSurfaceConfig) -> Unit,
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    Column {
        Text("Background scale")
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
                .clickable { dropdownExpanded = !dropdownExpanded },
        ) {
            Text(scale.text, Modifier.padding(12.dp))
            Spacer(Modifier.weight(1.0f))
            Icon(
                imageVector = if (dropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .align(Alignment.CenterVertically)
            )
        }
        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false },
        ) {
            for (type in BackgroundScale.values()) {
                DropdownMenuItem(
                    onClick = {
                        onUpdateConfig { copy(bgScale = type) }
                        dropdownExpanded = false
                    }
                ) {
                    Text(type.text, Modifier.padding(12.dp))
                }
            }
        }
    }
}