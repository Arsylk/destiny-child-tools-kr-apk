package com.arsylk.mammonsmite.presentation.dialog.pck.pack

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.arsylk.mammonsmite.domain.common.IntentUtils
import com.arsylk.mammonsmite.domain.dismissAndShow
import com.arsylk.mammonsmite.domain.onEffect
import com.arsylk.mammonsmite.model.common.collectAsState
import com.arsylk.mammonsmite.presentation.Navigable.Companion.putArg
import com.arsylk.mammonsmite.presentation.Navigator
import com.arsylk.mammonsmite.presentation.composable.LogLines
import com.arsylk.mammonsmite.presentation.composable.UiResultBox
import com.arsylk.mammonsmite.presentation.dialog.NavigableDialog
import com.arsylk.mammonsmite.presentation.nav
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

object PckPackDialog : NavigableDialog {
    override val route = "/pck/pack/{path}"
    override val label = "Pack Pck"
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
        val viewModel by viewModel<PckPackViewModel> { parametersOf(file) }

        PckPackDialog(viewModel)
    }
}

@Composable
fun PckPackDialog(viewModel: PckPackViewModel) {
    val title by viewModel.title.collectAsState()
    val uiResult by viewModel.uiResult.collectAsState()

    val scaffoldState = rememberScaffoldState()
    Scaffold(
        modifier = Modifier.fillMaxSize(0.9f),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                backgroundColor = MaterialTheme.colors.primaryVariant,
            )
        },
        scaffoldState = scaffoldState,
    ) {
        UiResultBox(uiResult = uiResult) { lines ->
            LogLines(lines)
        }
    }

    val context = LocalContext.current
    val nav = nav
    viewModel.onEffect { effect ->
        when (effect) {
            is Effect.OpenPacked -> {
                val action = scaffoldState.dismissAndShow(
                    message = "Packed successfully !",
                    actionLabel = "Open",
                    duration = SnackbarDuration.Indefinite,
                )
                if (action == SnackbarResult.ActionPerformed) {
                    IntentUtils.openFile(context, effect.file)
                }
                nav.controller.popBackStack()
            }
        }
    }
}