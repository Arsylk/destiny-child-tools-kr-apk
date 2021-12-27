package com.arsylk.mammonsmite.presentation.dialog.pck.unpack

import android.os.Bundle
import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Preview
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.arsylk.mammonsmite.NavGraphDirections
import com.arsylk.mammonsmite.domain.capitalizeFirstOnly
import com.arsylk.mammonsmite.domain.common.IntentUtils
import com.arsylk.mammonsmite.domain.launchWhenResumed
import com.arsylk.mammonsmite.presentation.composable.Live2DSurface
import com.arsylk.mammonsmite.presentation.composable.LogLines
import com.arsylk.mammonsmite.presentation.dialog.BaseComposeDialog
import com.arsylk.mammonsmite.presentation.view.live2d.Live2DSurfaceConfig
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

@ExperimentalFoundationApi
@ExperimentalSerializationApi
class PckUnpackDialog : BaseComposeDialog() {
    private val args by navArgs<PckUnpackDialogArgs>()
    private val viewModel by viewModel<PckUnpackViewModel> { parametersOf(args.file) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchWhenResumed {
            viewModel.effect.collect { effect ->
                when (effect) {
                    Effect.Dismiss -> dismiss()
                    is Effect.OpenFile -> IntentUtils.openFile(context, effect.file)
                    is Effect.SaveUnpacked -> {
                        val direction = PckUnpackDialogDirections
                            .actionSavePckUnpacked(effect.request)
                        findNavController().navigate(direction)
                    }
                }
            }
        }
    }

    @Composable
    override fun ComposeContent() {
        val tab by viewModel.tab.collectAsState()
        var expanded by remember(tab) { mutableStateOf(false) }
        Scaffold(
            topBar = { TopBar() },
            bottomBar = { BottomBar() },
            floatingActionButton = {
                if (tab == Tab.PREVIEW) return@Scaffold
                ActionButton(expanded) { expanded = !expanded }
            },
        ) {
            Box(modifier = Modifier.padding(it)) {
                when (tab) {
                    Tab.FILES -> FilesTabContent()
                    Tab.PREVIEW -> PreviewTabContent()
                    Tab.LOG -> LogTabContent()
                }
            }
        }
    }

    @Composable
    fun TopBar() {
        Column {
            val progress by viewModel.unpackProgress.collectAsState()
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
    fun BottomBar() {
        BottomNavigation {
            val selectedTab by viewModel.tab.collectAsState()
            for (tab in Tab.values()) {
                BottomNavigationItem(
                    selected = tab == selectedTab,
                    onClick = { viewModel.selectTab(tab) },
                    icon = { Icon(tab.icon, contentDescription = null) },
                    label = { Text(tab.name.capitalizeFirstOnly()) }
                )
            }
        }
    }

    @Composable
    fun ActionButton(expanded: Boolean, onClick: () -> Unit) {
        val rotation by animateFloatAsState(targetValue = if (expanded) 90.0f else 0.0f)
        val alpha by animateFloatAsState(targetValue = if (expanded) 1.0f else 0.0f)
        val actionSet by viewModel.actionSet.collectAsState()

        Column {
            Action.values().filter { it in actionSet }.forEachIndexed { i ,action ->
                Card(
                    modifier = Modifier
                        .size(width = 140.dp, height = 36.dp)
                        .alpha(alpha)
                        .clickable { viewModel.onActionClick(action) }
                ) {
                    Box {
                        Text(
                            text = action.text,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            FloatingActionButton(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }

    @Composable
    fun FilesTabContent() {
        val itemList by viewModel.items.collectAsState(initial = emptyList())
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(itemList) {
                PckHeaderItem(it)
            }
        }
    }

    @Composable
    fun PreviewTabContent() {
        val loadedL2dFile by viewModel.loadedL2dFile.collectAsState()
        var surfaceConfig by remember { mutableStateOf(Live2DSurfaceConfig.Default) }
        Box(Modifier.fillMaxSize()) {
            if (loadedL2dFile != null) {
                Live2DSurface(
                    loadedL2dFile = loadedL2dFile,
                    surfaceConfig = surfaceConfig,
                    playMotion = false,
                ) { offset, scale ->
                    surfaceConfig = surfaceConfig.copy(
                        scale = surfaceConfig.scale * scale,
                        offsetX = surfaceConfig.offsetX + offset.x,
                        offsetY = surfaceConfig.offsetY + offset.y,
                    )
                }
            }else {
                Text("Can't preview", modifier=Modifier.align(Alignment.Center))
            }
        }
    }

    @Composable
    fun LogTabContent() {
        val logList by viewModel.log.collectAsState()
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
                    Text(item.entry.hashString,
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }


    enum class Tab(val icon: ImageVector) {
        FILES(Icons.Default.Description),
        PREVIEW(Icons.Default.Preview),
        LOG(Icons.Default.Article),
    }

    enum class Action(val text: String) {
        CLEAN_UP("Clean up"),
        OPEN_PACKED("Open Packed"),
        OPEN_UNPACKED("Open Unpacked"),
        SAVE_MODEL("Save"),
    }
}