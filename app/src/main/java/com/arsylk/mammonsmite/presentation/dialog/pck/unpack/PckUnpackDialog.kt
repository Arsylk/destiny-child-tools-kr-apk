package com.arsylk.mammonsmite.presentation.dialog.pck.unpack

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.*
import com.arsylk.mammonsmite.domain.common.IntentUtils
import com.arsylk.mammonsmite.domain.onEffect
import com.arsylk.mammonsmite.model.common.LogLine
import com.arsylk.mammonsmite.model.common.NavTab
import com.arsylk.mammonsmite.model.common.OperationProgress
import com.arsylk.mammonsmite.model.file.FileType
import com.arsylk.mammonsmite.model.file.FileTypeFile
import com.arsylk.mammonsmite.model.file.FileTypeFolder
import com.arsylk.mammonsmite.model.live2d.L2DFileLoaded
import com.arsylk.mammonsmite.presentation.Navigable.Companion.putArg
import com.arsylk.mammonsmite.presentation.Navigator
import com.arsylk.mammonsmite.presentation.composable.*
import com.arsylk.mammonsmite.presentation.dialog.NavigableDialog
import com.arsylk.mammonsmite.presentation.dialog.pck.unpack.PckUnpackDialog.Action
import com.arsylk.mammonsmite.presentation.dialog.pck.unpack.PckUnpackDialog.Tab
import com.arsylk.mammonsmite.presentation.nav
import com.arsylk.mammonsmite.presentation.screen.home.HomeScreen
import com.arsylk.mammonsmite.presentation.screen.pck.unpacked.PckUnpackedScreen
import com.arsylk.mammonsmite.presentation.view.live2d.Live2DSurfaceConfig
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File


@ExperimentalMaterialApi
@ExperimentalSerializationApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
object PckUnpackDialog : NavigableDialog {
    override val route = "/pck/unpack/{path}"
    override val label = "Unpack Pck"
    override val args = listOf(
        navArgument("path") { type = NavType.StringType }
    )

    fun navigate(nav: Navigator, path: String, builder: NavOptionsBuilder.() -> Unit = {}) {
        val route = route.putArg("path", path)
        nav.controller.navigate(route) {
            launchSingleTop = true
            apply(builder)
        }
    }

    @Composable
    override fun Compose(entry: NavBackStackEntry) {
        val file = remember { File(entry.arguments?.getString("path")!!) }
        val viewModel by viewModel<PckUnpackViewModel> { parametersOf(file) }

        PckUnpackDialog(viewModel)
    }


    enum class Tab(override val label: String, override val icon: ImageVector) : NavTab {
        FILES("Files", Icons.Default.Description),
        PREVIEW("Preview", Icons.Default.Preview),
        LOG("Log", Icons.Default.Article),
    }

    enum class Action(override val label: String, override val icon: ImageVector?) : ActionButtonItem {
        CLEAN_UP("Clean up", Icons.Default.CleaningServices),
        OPEN_PACKED("Open Packed", FileTypeFile.icon),
        OPEN_UNPACKED("Open Unpacked", FileTypeFolder.icon),
        SAVE_MODEL("Save", Icons.Default.Save),
    }
}


@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalSerializationApi
@ExperimentalFoundationApi
@Composable
fun PckUnpackDialog(viewModel: PckUnpackViewModel) {
    val tab by viewModel.selectedTab.collectAsState()
    var expanded by remember(tab) { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.fillMaxSize(0.9f),
        topBar = {
            val progress by viewModel.unpackProgress.collectAsState()
            TopBar(progress)
        },
        bottomBar = {
            BottomTabNavigation(
                tabs = Tab.values(),
                selected = tab,
                onTabClick = viewModel::selectTab
            )
        },
        floatingActionButton = {
            if (tab == Tab.PREVIEW) return@Scaffold
            val actionSet by viewModel.actionSet.collectAsState()
            val list = remember(actionSet) { Action.values().filter { it in actionSet } }

            ActionButton(
                expanded = expanded,
                actions = list,
                onClick = { expanded = !expanded },
                onActionClick = viewModel::onActionClick,
            )
        },
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            when (tab) {
                Tab.FILES -> {
                    val isLoading by viewModel.isLoading.collectAsState()
                    val itemList by viewModel.items.collectAsState(initial = emptyList())
                    if (isLoading && itemList.isEmpty()) CircularProgressIndicator(Modifier.align(Alignment.Center))
                    else FilesTabContent(itemList)
                }
                Tab.PREVIEW -> {
                    val loadedL2dFile by viewModel.loadedL2dFile.collectAsState()
                    PreviewTabContent(loadedL2dFile)
                }
                Tab.LOG -> {
                    val logList by viewModel.log.collectAsState()
                    LogTabContent(logList)
                }
            }
        }
    }

    val context = LocalContext.current
    val nav = nav
    viewModel.onEffect(nav) { effect ->
        when (effect) {
            Effect.Dismiss -> nav.controller.popBackStack()
            is Effect.OpenFile -> IntentUtils.openFile(context, effect.file)
            is Effect.SaveUnpacked -> PckUnpackedScreen.navigate(nav, effect.folder) {
                popUpTo(HomeScreen.route)
            }
        }
    }
}

@Composable
fun TopBar(progress: OperationProgress) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = { Text(progress.toString()) },
            backgroundColor = MaterialTheme.colors.primaryVariant,
        )
        LinearProgressIndicator(
            progress = progress.percentage,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun FilesTabContent(items: List<PckHeaderItem>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items) { item ->
            PckHeaderItem(item)
        }
    }
}

@Composable
fun PreviewTabContent(loadedL2dFile: L2DFileLoaded?) {
    var surfaceConfig by remember { mutableStateOf(Live2DSurfaceConfig.Default) }
    Box(Modifier.fillMaxSize()) {
        if (loadedL2dFile != null) {
            Live2DSurface(
                loadedL2dFile = loadedL2dFile,
                surfaceConfig = surfaceConfig,
                playMotion = false,
                onMotionPlayed = {}
            ) { offset, scale ->
                surfaceConfig = surfaceConfig.copy(
                    scale = surfaceConfig.scale * scale,
                    offsetX = surfaceConfig.offsetX + offset.x,
                    offsetY = surfaceConfig.offsetY + offset.y,
                )
            }
        } else {
            Text("Can't preview", modifier = Modifier.align(Alignment.Center))
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun LogTabContent(logList: List<LogLine>) {
    LogLines(logList)
}

@Composable
fun PckHeaderItem(item: PckHeaderItem) {
    Column(modifier = Modifier.padding(8.dp)) {
        when (item) {
            is PckHeaderItem.Packed -> {
                Text(item.entry.hashString)
            }
            is PckHeaderItem.Unpacked -> {
                Text(item.entry.filename)
                Text(
                    item.entry.hashString,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}