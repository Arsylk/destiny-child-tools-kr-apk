package com.arsylk.mammonsmite.presentation.fragment.pck.unpacked

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Source
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.navArgs
import com.arsylk.mammonsmite.presentation.dialog.pck.unpacked.PckUnpackedConfigDialog
import com.arsylk.mammonsmite.presentation.fragment.BaseComposeFragment
import com.arsylk.mammonsmite.presentation.fragment.pck.unpacked.items.UnpackedLive2DItem
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

@ExperimentalSerializationApi
class PckUnpackedFragment : BaseComposeFragment() {
    private val args by navArgs<PckUnpackedFragmentArgs>()
    private val viewModel by viewModel<PckUnpackedViewModel> { parametersOf(args.saveRequest) }

    @Composable
    override fun ComposeContent() {
        args.saveRequest?.run { UnpackedPckSaveDialog(this) }
        Column(Modifier.fillMaxSize()) {
            var selectedTab by remember { mutableStateOf(Tab.LIVE2D) }
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth(),
            ) {
                val live2dListState = rememberLazyListState()
                Crossfade(targetState = selectedTab) { tab ->
                    when (tab) {
                        Tab.LIVE2D -> UnpackedLive2DTab(live2dListState)
                        Tab.PCK -> UnpackedPckTab()
                    }
                }
            }
            BottomNavigation {
                for (tab in Tab.values()) {
                    BottomNavigationItem(
                        selected = tab == selectedTab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.text) }
                    )
                }
            }
        }
    }

    @Composable
    fun UnpackedLive2DTab(listState: LazyListState) {
        val items by viewModel.live2dItems.collectAsState()
        var showItemConfig by remember { mutableStateOf<UnpackedLive2DItem?>(null) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = listState,
        ) {
            itemsIndexed(items, key = { _, item -> item.key }) { i, item ->
                UnpackedLive2DItem(
                    item = item,
                    onClick = { showItemConfig = items[i] }
                )
            }
        }
        showItemConfig?.run {
            UnpackedLive2DConfigDialog(this) {
                showItemConfig = null
            }
        }
    }

    @Composable
    fun UnpackedPckTab() {

    }

    @Composable
    fun UnpackedPckSaveDialog(request: PckUnpackedSaveRequest) {
        var showDialog by remember(request) { mutableStateOf(true) }
        if (!showDialog) return

        var state by remember(request) {
            mutableStateOf(viewModel.prepareSaveState(request))
        }
        PckUnpackedConfigDialog(
            state = state,
            onDismissRequest = { showDialog = false },
            onStateChanged = { state = it },
        ) {
            showDialog = false
            viewModel.saveUnpackedPckConfig(request, it)
        }
    }

    @Composable
    fun UnpackedLive2DConfigDialog(item: UnpackedLive2DItem, onDismiss: () -> Unit) {
        var showDialog by remember(item) { mutableStateOf(true) }
        if (!showDialog) return

        var state by remember(item) {
            mutableStateOf(viewModel.prepareLive2DConfigState(item))
        }
        PckUnpackedConfigDialog(
            state = state,
            onDismissRequest = {
                showDialog = false
                onDismiss.invoke()
            },
            onStateChanged = { state = it },
        ) {
            showDialog = false
            onDismiss.invoke()
            viewModel.saveUnpackedPckConfig(item.pck, item.l2dFile, it)
        }
    }

    enum class Tab(val text: String, val icon: ImageVector) {
        LIVE2D("Live 2D", Icons.Default.Slideshow),
        PCK("Pck", Icons.Default.Source),
    }
}