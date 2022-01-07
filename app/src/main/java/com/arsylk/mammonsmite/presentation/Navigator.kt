package com.arsylk.mammonsmite.presentation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.navigation.*
import androidx.navigation.compose.rememberNavController
import com.arsylk.mammonsmite.presentation.dialog.result.ResultDialogHostState


class Navigator(
    val controller: NavHostController,
    val resultDialogHost: ResultDialogHostState
) {

    companion object {
        const val DialogHostRoute = "/dialog-host"
    }
}

interface Navigable {
    val route: String
    val label: String
    val baseArgs: List<NamedNavArgument> get() = listOf(
        navArgument("label") {
            type = NavType.StringType
            defaultValue = label
        }
    )
    val args: List<NamedNavArgument> get() = emptyList()

    infix fun describes(entry: NavBackStackEntry?): Boolean {
        return entry?.destination?.route == route
    }

    infix fun prepare(builder: NavGraphBuilder)

    @Composable
    fun Compose(entry: NavBackStackEntry)

    companion object {
        private fun String.replaceArg(key: String, value: String): String =
            replaceFirst("{$key}", value)

        fun String.putArg(key: String, value: String?): String =
            replaceArg(key, Uri.encode(value))

        fun String.putArg(key: String, value: Boolean): String =
            replaceArg(key, "$value")
    }
}

@Composable
fun rememberNavigator(): Navigator {
    val controller = rememberNavController()
    val resultDialogHost = remember { ResultDialogHostState() }
    return remember(controller) { Navigator(controller, resultDialogHost) }
}

val LocalNavigator = compositionLocalOf<Navigator> {
    error("Navigator not provided")
}

inline val nav: Navigator
    @Composable get() = LocalNavigator.current

inline val NavBackStackEntry?.label get() = this?.arguments
    ?.getString("label", null)

inline val NavBackStackEntry?.isFullscreen get() = this?.arguments
    ?.getBoolean("fullscreen", false) ?: false
