package com.arsylk.mammonsmite.presentation.screen

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.arsylk.mammonsmite.presentation.Navigable

interface NavigableScreen : Navigable {
    val fullscreen: Boolean get() = false

    override infix fun prepare(builder: NavGraphBuilder) {
        builder.composable(
            route = route,
            arguments = baseArgs + listOf(
                navArgument("fullscreen") {
                    type = NavType.BoolType
                    defaultValue = fullscreen
                },
            ) + args,
        ) { entry ->
            Compose(entry)
        }
    }
}