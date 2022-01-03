package com.arsylk.mammonsmite.presentation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.*
import androidx.navigation.compose.composable


class Navigator(private val provideController: () -> NavHostController) {
    val controller: NavHostController get() = provideController.invoke()
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

val LocalNavigator = compositionLocalOf<Navigator> {
    error("Navigator not provided")
}

inline val nav: Navigator
    @Composable get() = LocalNavigator.current

inline val NavBackStackEntry?.label get() = this?.arguments
    ?.getString("label", null)

inline val NavBackStackEntry?.isFullscreen get() = this?.arguments
    ?.getBoolean("fullscreen", false) ?: false
