package com.arsylk.mammonsmite.presentation.dialog

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.arsylk.mammonsmite.presentation.Navigable

@ExperimentalComposeUiApi
interface NavigableDialog : Navigable {
    val usePlatformDefaultWidth: Boolean get() = false
    val dismissOnBackPress: Boolean get() = true
    val dismissOnClickOutside: Boolean get() = true

    override infix fun prepare(builder: NavGraphBuilder) {
        builder.dialog(
            route = route,
            arguments = baseArgs + args,
            dialogProperties = DialogProperties(
                usePlatformDefaultWidth = usePlatformDefaultWidth,
                dismissOnBackPress = dismissOnBackPress,
                dismissOnClickOutside = dismissOnClickOutside,
            )
        ) { entry ->
            Compose(entry)
        }
    }
}