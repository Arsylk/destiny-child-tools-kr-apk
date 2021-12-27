package com.arsylk.mammonsmite.presentation.fragment.l2dpreview

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.fragment.navArgs
import com.arsylk.mammonsmite.domain.launchWhenResumed
import com.arsylk.mammonsmite.presentation.composable.InputDialogContent
import com.arsylk.mammonsmite.presentation.composable.InputFloatSlider
import com.arsylk.mammonsmite.presentation.composable.Live2DSurface
import com.arsylk.mammonsmite.presentation.fragment.BaseComposeFragment
import com.arsylk.mammonsmite.presentation.view.live2d.BackgroundScale
import com.arsylk.mammonsmite.presentation.view.live2d.Live2DSurfaceConfig
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

@ExperimentalSerializationApi
class L2DPreviewFragment : BaseComposeFragment() {
    private val args by navArgs<L2DPreviewFragmentArgs>()
    private val viewModel by viewModel<L2DPreviewViewModel> { parametersOf(args.l2dFile) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchWhenResumed {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is Effect.FatalError -> TODO()
                }
            }
        }
    }

    @Preview
    @Composable
    override fun ComposeContent() {
        var showDialog by remember { mutableStateOf(false) }
        var playMotion by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { showDialog = true },
                        onTap = { playMotion = true },
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
            ) { offset, zoom ->
                viewModel.updateSurfaceConfig {
                    copy(
                        scale = scale * zoom,
                        offsetX = offsetX + offset.x,
                        offsetY = offsetY + offset.y,
                    )
                }
            }

            if (showDialog) ConfigDialog(surfaceConfig) { showDialog = false }
        }
    }

    @Composable
    fun ConfigDialog(config: Live2DSurfaceConfig, onDismiss: () -> Unit = {}) {
        Dialog(onDismissRequest = onDismiss) {
            InputDialogContent("Config") {
                ConfigInputForm(config)
            }
        }
    }

    @Composable
    fun ConfigInputForm(config: Live2DSurfaceConfig) {
        Column {
            ConfigInputBackgroundScale(config.bgScale)
            Spacer(Modifier.height(12.dp))
            InputFloatSlider(
                label = "Model scale",
                value = config.scale,
                valueRange = Live2DSurfaceConfig.rangeScale,
                onValueChanged = { viewModel.updateSurfaceConfig { copy(scale = it) } }
            )
            Spacer(Modifier.height(12.dp))
            InputFloatSlider(
                label = "Model Offset X",
                value = config.offsetX,
                valueRange = Live2DSurfaceConfig.rangeOffsetX,
                onValueChanged = { viewModel.updateSurfaceConfig { copy(offsetX = it) } }
            )
            Spacer(Modifier.height(12.dp))
            InputFloatSlider(
                label = "Model Offset Y",
                value = config.offsetY,
                valueRange = Live2DSurfaceConfig.rangeOffsetY,
                onValueChanged = { viewModel.updateSurfaceConfig { copy(offsetY = it) } }
            )
            Spacer(Modifier.height(12.dp))
            Row {
                Row(Modifier.weight(1.0f)) {
                    Checkbox(
                        checked = config.animate,
                        onCheckedChange = { viewModel.updateSurfaceConfig { copy(animate = it) } },
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                    Text("Animate", Modifier.align(Alignment.CenterVertically))
                }
                Row(Modifier.weight(1.0f)) {
                    Checkbox(
                        checked = config.tappable,
                        onCheckedChange = { viewModel.updateSurfaceConfig { copy(tappable = it) } },
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                    Text("Tappable", Modifier.align(Alignment.CenterVertically))
                }
            }
        }
    }

    @Composable
    fun ConfigInputBackgroundScale(scale: BackgroundScale) {
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
                            viewModel.updateSurfaceConfig { copy(bgScale = type) }
                            dropdownExpanded = false
                        }
                    ) {
                        Text(type.text, Modifier.padding(12.dp))
                    }
                }
            }
        }
    }
}