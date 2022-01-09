package com.arsylk.mammonsmite.presentation.screen.pck.swap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import com.arsylk.mammonsmite.domain.dismissAndShow
import com.arsylk.mammonsmite.domain.onEffect
import com.arsylk.mammonsmite.domain.toSnackbarMessage
import com.arsylk.mammonsmite.domain.use
import com.arsylk.mammonsmite.model.common.LogLine
import com.arsylk.mammonsmite.model.pck.UnpackedPckLive2D
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckEntry
import com.arsylk.mammonsmite.presentation.Navigator
import com.arsylk.mammonsmite.presentation.composable.LogLines
import com.arsylk.mammonsmite.presentation.dialog.result.unpacked.ResultUnpackedPckLive2DDialog
import com.arsylk.mammonsmite.presentation.nav
import com.arsylk.mammonsmite.presentation.screen.NavigableScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import kotlin.math.max

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

@Composable
fun PckSwapScreen(viewModel: PckSwapViewModel = getViewModel()) {
    val scope = rememberCoroutineScope()
    val fromItem by viewModel.fromItem.collectAsState()
    val toItem by viewModel.toItem.collectAsState()
    val log by viewModel.logLines.collectAsState()

    val scaffoldState = rememberScaffoldState()
    var expanded by remember { mutableStateOf(true) }
    Scaffold(
        scaffoldState = scaffoldState,
    ) {
        Column {
            Row {
                Row(
                    modifier = Modifier.weight(1.0f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PckSwapSelect(
                        label = "From",
                        onSelected = viewModel::setFromItem,
                    )
                    Text(fromItem?.pck?.header?.name ?: "Nothing selected")
                }
                Row(
                    modifier = Modifier.weight(1.0f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PckSwapSelect(
                        label = "To",
                        onSelected = viewModel::setToItem,
                    )
                    Text(toItem?.pck?.header?.name ?: "Nothing selected")
                }
            }
            Row {
                Button(
                    enabled = fromItem != null && toItem != null,
                    onClick = {
                        use(fromItem, toItem, viewModel::swap)
                    },
                ) {
                    Text("Swap")
                }
                Button(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Collapse" else "Expand")
                }
            }

            Box(Modifier.fillMaxSize()) {
                if (expanded) use(fromItem, toItem) { f, t ->
                    MultiSwapList(f, t)
                } else LogLines(list = log)
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
    onSelected: (UnpackedPckLive2D) -> Unit
) {
    val scope = rememberCoroutineScope()
    val dialogHost = nav.resultDialogHost

    OutlinedButton(
        onClick = {
            scope.launch {
                dialogHost.showResultDialog {
                    ResultUnpackedPckLive2DDialog()
                }?.also {
                    onSelected.invoke(it)
                }
            }
        },
    ) {
        Text(label)
    }
}

@Composable
internal fun MultiSwapList(fromItem: PckSwapItem, toItem: PckSwapItem) {
    val scroll = rememberScrollState()
    val size = max(fromItem.pck.header.entries.size, toItem.pck.header.entries.size)

    Row {
        Column(Modifier.weight(1.0f)) {
            Text("[${fromItem.viewIdx.string}] ${fromItem.name}", fontSize = 12.sp)
            Column(Modifier.verticalScroll(scroll)) {
                repeat(size) {
                    SwapEntryItem(entry = fromItem.pck.header.entries.getOrNull(it))
                }
            }
        }
        Column(Modifier.weight(1.0f)) {
            Text("[${fromItem.viewIdx.string}] ${fromItem.name}", fontSize = 12.sp)
            Column(Modifier.verticalScroll(scroll)) {
                repeat(size) {
                    SwapEntryItem(entry = toItem.pck.header.entries.getOrNull(it))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun SwapEntryItem(entry: UnpackedPckEntry?) {
    ListItem(
        text = {
            if (entry != null) Text(entry.filename)
            else Text("Missing", color = MaterialTheme.colors.error)
        },
        secondaryText = {
            if (entry != null) Text(entry.hashString)
            else Text("")
        },
        singleLineSecondaryText = true,
    )
}