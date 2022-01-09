package com.arsylk.mammonsmite.presentation.screen.pck.unpacked

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Source
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.arsylk.mammonsmite.domain.common.IntentUtils
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.domain.files.SafProvider
import com.arsylk.mammonsmite.domain.noRippleClickable
import com.arsylk.mammonsmite.domain.onEffect
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.model.common.LogLine
import com.arsylk.mammonsmite.model.common.NavTab
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import com.arsylk.mammonsmite.presentation.Navigable.Companion.putArg
import com.arsylk.mammonsmite.presentation.Navigator
import com.arsylk.mammonsmite.presentation.composable.BottomTabNavigation
import com.arsylk.mammonsmite.presentation.composable.LogLinesDialog
import com.arsylk.mammonsmite.presentation.composable.SurfaceBox
import com.arsylk.mammonsmite.presentation.dialog.pck.unpacked.PckUnpackedConfigDialog
import com.arsylk.mammonsmite.presentation.dialog.pck.unpacked.PckUnpackedConfigState
import com.arsylk.mammonsmite.presentation.nav
import com.arsylk.mammonsmite.presentation.screen.NavigableScreen
import com.arsylk.mammonsmite.presentation.screen.l2d.preview.L2DPreviewScreen
import com.arsylk.mammonsmite.presentation.screen.pck.unpacked.PckUnpackedScreen.Tab
import com.arsylk.mammonsmite.presentation.screen.pck.unpacked.PckUnpackedScreen.UiState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.viewModel
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt



@OptIn(ExperimentalFoundationApi::class, ExperimentalSerializationApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
object PckUnpackedScreen : NavigableScreen {
    override val route = "/pck/unpacked?save={save}"
    override val label = "Unpacked"
    override val args = listOf(
        navArgument("save") {
            type = NavType.StringType
            nullable = true
        }
    )

    fun navigate(nav: Navigator, save: File? = null, builder: NavOptionsBuilder.() -> Unit = {}) {
        val route = if (save != null) route.putArg("save", save.absolutePath) else route
        nav.controller.navigate(route) {
            launchSingleTop = true
            apply(builder)
        }
    }

    @Composable
    override fun Compose(entry: NavBackStackEntry) {
        val saveFile = remember { entry.arguments?.getString("save")?.let(::File) }
        PckUnpackedScreen(
            saveFile = saveFile,
            onSaveHandled = { entry.arguments?.remove("save") }
        )
    }

    enum class Tab(override val label: String, override val icon: ImageVector) : NavTab {
        LIVE2D("Live 2D", Icons.Default.Slideshow),
        PCK("Pck", Icons.Default.Source),
    }

    sealed class UiState {
        object Nothing : UiState()
        data class PackPck(val pck: UnpackedPckFile, val load: Boolean) : UiState()
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalSerializationApi
@Composable
fun PckUnpackedScreen(
    viewModel: PckUnpackedViewModel = getViewModel(),
    saveFile: File? = null,
    onSaveHandled: () -> Unit = {},
) {
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedTab by remember { mutableStateOf(Tab.LIVE2D) }
    val scaffoldState = rememberScaffoldState()

    Scaffold(scaffoldState = scaffoldState) { padding ->
        SurfaceBox(
            modifier = Modifier.padding(padding)
        ) {
            Column(Modifier.fillMaxSize()) {
                if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(),)
                else LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = 1.0f)

                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxWidth(),
                ) {
                    val listState = rememberLazyListState()
                    Crossfade(targetState = selectedTab) { tab ->
                        when (tab) {
                            Tab.LIVE2D -> {
                                val items by viewModel.items.collectAsState()
                                UnpackedLive2DTab(listState, items, viewModel::setActionDrawerItem)
                            }
                            Tab.PCK -> UnpackedPckTab()
                        }
                    }
                }
                BottomTabNavigation(
                    tabs = Tab.values(),
                    selected = selectedTab,
                    onTabClick = { tab -> selectedTab = tab }
                )
            }
            BottomDrawerHost()
        }
    }


    var uiState by remember { mutableStateOf<UiState>(UiState.Nothing) }
    viewModel.onEffect { effect ->
        when (effect) {
            is Effect.ShowSnackbar -> scaffoldState.snackbarHostState
                .showSnackbar(effect.text, effect.action)
            is Effect.PackPck -> uiState = UiState.PackPck(effect.pck, effect.load)
        }
    }
    when (val state = uiState) {
        UiState.Nothing -> {}
        is UiState.PackPck -> {
            HandlePackPck(state, onHandled = { uiState = UiState.Nothing })
        }
    }

    HandleSaveFile(saveFile, onSaveHandled)
}

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalSerializationApi
@Composable
internal fun BottomDrawerHost() {
    val (OPEN, CLOSED) = 0 to 1

    val viewModel by viewModel<PckUnpackedViewModel>()
    val scope = rememberCoroutineScope()

    val state = rememberSwipeableState(CLOSED)
    val height = 360.dp
    val heightPx = with(LocalDensity.current) { height.toPx() }
    val anchors = mapOf(heightPx to CLOSED, 0f to OPEN)
    var offsetY = min(heightPx, max(0f, state.offset.value))
    val item by viewModel.drawerItem.collectAsState()
    var shown by remember(item) { mutableStateOf(false) }
    if (item == null) return

    offsetY = if (!shown && offsetY == 0.0f) heightPx else offsetY
    val scrim = MaterialTheme.colors.surface
        .copy(alpha = (1.0f - (offsetY / heightPx)) * 0.5f)

    LaunchedEffect(item) {
        if (state.currentValue != OPEN) state.animateTo(OPEN)
    }
    BackHandler(item != null) {
        scope.launch {
            state.animateTo(CLOSED)
            viewModel.setActionDrawerItem(null)
        }
    }
    if (state.currentValue == CLOSED && state.targetValue == CLOSED && shown) {
        LaunchedEffect(item) {
            state.animateTo(CLOSED)
            viewModel.setActionDrawerItem(null)
        }
    }
    shown = true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(scrim)
            .swipeable(
                state = state,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Vertical,
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
                    .noRippleClickable {
                        scope.launch {
                            state.animateTo(CLOSED)
                            viewModel.setActionDrawerItem(null)
                        }
                    }
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .offset { IntOffset(x = 0, y = offsetY.roundToInt()) },
                verticalArrangement = Arrangement.Top,
            ) {
                item?.run {
                    UnpackedLive2DActionSheet(
                        item = this,
                        onDismiss = {
                            state.animateTo(CLOSED)
                            viewModel.setActionDrawerItem(null)
                        }
                    )
                }
            }
        }
    }
}

@ExperimentalSerializationApi
@ExperimentalComposeUiApi
@Composable
fun UnpackedLive2DTab(
    state: LazyListState,
    items: List<UnpackedLive2DItem>,
    openActionDrawer: (UnpackedLive2DItem) -> Unit
) {
    var showItemConfig by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        state = state,
    ) {
        items(
            items = items,
            key = { item -> item.key },

            ) { item ->
            UnpackedLive2DItem(
                item = item,
                onClick = { openActionDrawer.invoke(item) },
                onLongClick = { showItemConfig = item.key }
            )
        }
    }
    val config = items.firstOrNull { it.key == showItemConfig }

    when {
        config != null -> {
            UnpackedLive2DConfigDialog(config) {
                showItemConfig = null
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalSerializationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
internal fun UnpackedLive2DActionSheet(item: UnpackedLive2DItem, onDismiss: suspend () -> Unit) {
    val viewModel by viewModel<PckUnpackedViewModel>()
    val scope = rememberCoroutineScope()
    val nav = nav
    val context = LocalContext.current

    @Composable
    fun SimpleListItem(text: String, onClick: suspend () -> Unit) {
        ListItem(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = { scope.launch { onClick.invoke() } }),
            text = { Text(text) }
        )
    }

    Card(
        modifier = Modifier
            .height(360.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),

    ) {
        Column(Modifier.padding(vertical = 8.dp)) {
            SimpleListItem(text = "Preview") {
                L2DPreviewScreen.navigate(nav, item.l2dFile.folder.absolutePath)
            }
            SimpleListItem(text = "Load") {
                onDismiss.invoke()
                viewModel.enqueueEffect(Effect.PackPck(item.pck, load = true))
            }
            if (item.isBackedUp)  SimpleListItem(text = "Restore") {
                onDismiss.invoke()
                viewModel.restorePckBackup(item.pck)
            }
            SimpleListItem(text = "Delete") {
                viewModel.deleteLive2DItem(item)
                onDismiss.invoke()
            }
            ListItem(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { },
                text = { Text("Wallpaper") }
            )
            ListItem(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { },
                text = { Text("Info") }
            )
            SimpleListItem(text = "Open") {
                IntentUtils.openFile(context, item.l2dFile.folder)
            }
            SimpleListItem(text = "Pack") {
                onDismiss.invoke()
                viewModel.enqueueEffect(Effect.PackPck(item.pck, load = false))
            }
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalSerializationApi
@Composable
internal fun UnpackedLive2DConfigDialog(
    item: UnpackedLive2DItem,
    onDismiss: () -> Unit,
) {
    val viewModel by viewModel<PckUnpackedViewModel>()
    var state by remember(item) {
        mutableStateOf(viewModel.prepareLive2DConfigState(item))
    }
    PckUnpackedConfigDialog(
        state = state,
        onDismissRequest = onDismiss,
        onStateChanged = { state = it },
    ) {
        onDismiss.invoke()
        viewModel.saveUnpackedPckConfig(it)
    }
}


// TODO Debug only ?
@Composable
internal fun UnpackedPckTab(prefs: AppPreferences = get()) {
    val path = remember { IFile(prefs.destinychildFilesPath).listFiles().map { it.absolutePath } }
    val data =
        remember { IFile("/storage/emulated/0/Android/data").listFiles().map { it.absolutePath } }

    Column(Modifier.fillMaxSize()) {
        Text(
            "SAF: ${SafProvider.safDoc?.uri} -> ${SafProvider.safDoc?.name}",
            color = Color.LightGray
        )
        Text("SAF URI: ${SafProvider.safRootUri}", color = Color.LightGray)
        Card(Modifier.weight(1.0f)) {
            Column {
                Text("DC Path: ${prefs.destinychildFilesPath}", fontSize = 18.sp)
                LazyColumn {
                    items(path) {
                        Text(it, fontSize = 14.sp)
                    }
                }
            }
        }
        Card(Modifier.weight(1.0f)) {
            Column(Modifier.weight(1.0f)) {
                Text("Data Path: \"/storage/emulated/0/Android/data\"", fontSize = 18.sp)
                LazyColumn {
                    items(data) {
                        Text(it, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalSerializationApi
@Composable
internal fun HandlePackPck(state: UiState.PackPck, onHandled: () -> Unit) {
    val viewModel by viewModel<PckUnpackedViewModel>()

    val lines by produceState(emptyList<LogLine>(), state.pck) {
        viewModel.packUnpackedPck(state.pck, state.load)
            .collectLatest { value = it }
    }
    LogLinesDialog(list = lines, onDismiss = onHandled)
}


@ExperimentalComposeUiApi
@ExperimentalSerializationApi
@Composable
internal fun HandleSaveFile(saveFile: File?, onSaveHandled: () -> Unit) {
    val viewModel by viewModel<PckUnpackedViewModel>()
    var saveState by remember(saveFile) {
        mutableStateOf<PckUnpackedConfigState?>(null)
    }
    val state = saveState
    if (state != null) {
        PckUnpackedConfigDialog(
            state = state,
            onDismissRequest = { saveState = null },
            onStateChanged = { saveState = it },
            onConfirmClick = {
                saveState = null
                viewModel.saveUnpackedPckConfig(it, moveToUnpacked = true)
            }
        )
    }


    LaunchedEffect(saveFile) {
        if (saveFile != null) {
            onSaveHandled.invoke()
            saveState = viewModel.prepareSaveState(saveFile)
        }
    }
}