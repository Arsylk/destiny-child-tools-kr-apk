package com.arsylk.mammonsmite.presentation.screen.pck.swap

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import coil.compose.rememberImagePainter
import com.arsylk.mammonsmite.domain.dismissAndShow
import com.arsylk.mammonsmite.domain.onEffect
import com.arsylk.mammonsmite.domain.toSnackbarMessage
import com.arsylk.mammonsmite.domain.use
import com.arsylk.mammonsmite.model.pck.UnpackedPckLive2D
import com.arsylk.mammonsmite.presentation.Navigator
import com.arsylk.mammonsmite.presentation.composable.LogLines
import com.arsylk.mammonsmite.presentation.composable.SurfaceBox
import com.arsylk.mammonsmite.presentation.dialog.result.unpacked.ResultUnpackedPckLive2DDialog
import com.arsylk.mammonsmite.presentation.nav
import com.arsylk.mammonsmite.presentation.screen.NavigableScreen
import com.arsylk.mammonsmite.presentation.screen.pck.unpacked.PckUnpackedScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

object PckSwapScreen : NavigableScreen {
    override val route = "/pck/swap"
    override val label = "Model Swapper"

    fun navigate(nav: Navigator, builder: NavOptionsBuilder.() -> Unit = {}) {
        nav.controller.navigate(route) {
            launchSingleTop = true
            apply(builder)
        }
    }

    @Composable
    override fun Compose(entry: NavBackStackEntry) {
        PckSwapScreen()
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PckSwapScreen(viewModel: PckSwapViewModel = getViewModel()) {
    val scope = rememberCoroutineScope()
    val fromItem by viewModel.fromItem.collectAsState()
    val toItem by viewModel.toItem.collectAsState()
    val resultItem by viewModel.resultItem.collectAsState()
    val log by viewModel.logLines.collectAsState()

    val nav = nav
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(8.dp)) {
                PckSwapSelect(
                    label = "From",
                    item = fromItem,
                    onSelected = viewModel::setFromItem,
                )
                Spacer(Modifier.height(8.dp))
                PckSwapSelect(
                    label = "To",
                    item = toItem,
                    onSelected = viewModel::setToItem,
                )
            }
            Row {
                Button(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    enabled = fromItem != null && toItem != null,
                    onClick = { use(fromItem, toItem, viewModel::swap) },
                ) {
                    Text("Swap")
                }
            }

            val offsetX by animateDpAsState(targetValue = if (resultItem == null) 0.dp else 54.dp)
            Box(Modifier.fillMaxWidth().weight(1.0f)) {
                LogLines(list = log)
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(offsetX),
                color = SnackbarDefaults.backgroundColor,
                contentColor = MaterialTheme.colors.surface,
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.body2,
                        text = "Swap Successful"
                    )
                    Spacer(modifier = Modifier.weight(1.0f))
                    TextButton(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        onClick = click@{
                            val item = resultItem ?: return@click
                            PckUnpackedScreen.navigate(nav, item.pck.folder)
                        },
                        content = { Text("Save") }
                    )
                }

            }
        }
    }

    viewModel.onEffect { effect ->
        when (effect) {
            is Effect.ParsingError -> {
                scope.launch {
                    scaffoldState.dismissAndShow(effect.throwable.toSnackbarMessage())
                }
            }
        }
    }
}

@Composable
internal fun PckSwapSelect(
    label: String,
    item: PckSwapItem?,
    onSelected: (UnpackedPckLive2D) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val dialogHost = nav.resultDialogHost
    fun select() {
        scope.launch {
            dialogHost.showResultDialog {
                ResultUnpackedPckLive2DDialog()
            }?.also {
                onSelected.invoke(it)
            }
        }
    }

    Column {
        Text(label)
        SurfaceBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(BorderStroke(1.dp, MaterialTheme.colors.primary))
                .clickable { select() }
        ) {
            if (item != null) {
                var hidePreview by remember(item.preview.absolutePath) { mutableStateOf(false) }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Box(Modifier.size(96.dp)) {
                        if (!hidePreview) {
                            Image(
                                painter = rememberImagePainter(data = item.preview) {
                                    listener(
                                        onError = { _, _ ->
                                            hidePreview = true
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxSize(),
                                contentDescription = null,
                            )
                        } else {
                            Text(
                                text = "No preview",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.subtitle2,
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(text = item.name)
                        Text(
                            text = "View Idx: ${item.viewIdx.string}",
                            fontSize = 14.sp,
                        )
                    }
                }
            } else {
                Text("Press to select", Modifier.align(Alignment.Center))
            }
        }
    }
}