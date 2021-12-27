package com.arsylk.mammonsmite.presentation.fragment.pck.unpacked

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Source
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.fragment.navArgs
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import com.arsylk.mammonsmite.presentation.composable.InputDialogContent
import com.arsylk.mammonsmite.presentation.composable.InputDialogField
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
        UnpackedPckSaveDialog(args.saveRequest)
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
    fun UnpackedPckSaveDialog(request: PckUnpackedSaveRequest?) {
        var showDialog by remember(request) { mutableStateOf(request != null) }
        if (!showDialog) return
        val input by viewModel.saveInput.collectAsState()
        input?.apply {
            Dialog(onDismissRequest = { showDialog = !showDialog }) {
                InputDialogContent(title = "Save Unpacked") {
                    val viewIdx = remember(viewIdxText) { ViewIdx.parse(viewIdxText) }

                    val nameError = name.isBlank()
                    val folderError by viewModel.saveInputFolderExists.collectAsState()
                    val viewIdxError = (viewIdx == null && viewIdxText.isNotBlank()) && isL2d


                    Column {
                        InputDialogField(
                            labelText = "Name",
                            value = name,
                            isError = nameError,
                            errorText = "Name can't be blank",
                            onValueChange = { viewModel.updateSaveInput { copy(name = it) } },
                        )
                        Spacer(Modifier.height(12.dp))
                        InputDialogField(
                            labelText = "Folder Name",
                            value = folderName,
                            isError = folderError,
                            errorText = if (folderName.isBlank()) "Folder can't be blank" else "Folder already exists",
                            onValueChange = { viewModel.updateSaveInput { copy(folderName = it) } },
                        )
                        if (isL2d) {
                            Spacer(Modifier.height(12.dp))
                            InputDialogField(
                                labelText = "View Idx",
                                value = viewIdxText,
                                isError = viewIdxError,
                                errorText = "Invalid View Idx",
                                onValueChange = { viewModel.updateSaveInput { copy(viewIdxText = it) } },
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        InputDialogField(
                            labelText = "Game Relative Path",
                            value = gameRelativePath,
                            isError = false,
                            onValueChange = { viewModel.updateSaveInput { copy(gameRelativePath = it) } },
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            enabled = !(nameError || folderError || viewIdxError),
                            modifier = Modifier.align(Alignment.End),
                            onClick = {
                                showDialog = false
                                viewModel.saveUnpacked()
                            },
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun UnpackedLive2DTab(listState: LazyListState) {
        val items by viewModel.live2dItems.collectAsState()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = listState,
        ) {
            items(items, key = UnpackedLive2DItem::key) { item ->
                UnpackedLive2DItem(item = item, onClick = { println(it) })
            }
        }
    }

    @Composable
    fun UnpackedPckTab() {

    }

    enum class Tab(val text: String, val icon: ImageVector) {
        LIVE2D("Live 2D", Icons.Default.Slideshow),
        PCK("Pck", Icons.Default.Source),
    }
}