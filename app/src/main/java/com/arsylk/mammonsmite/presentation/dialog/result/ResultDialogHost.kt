package com.arsylk.mammonsmite.presentation.dialog.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.navigation.compose.*
import com.arsylk.mammonsmite.domain.noRippleClickable
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.experimental.ExperimentalTypeInference

class ResultDialogHostState {
    private val mutex = Mutex()
    var current by mutableStateOf<ResultDialog<*>?>(null)
        private set

    @OptIn(ExperimentalTypeInference::class)
    suspend fun <T: Any> showResultDialog(
        @BuilderInference content: @Composable ResultDialogAction<T>.() -> Unit,
    ): T? = mutex.withLock {
        return try {
            suspendCancellableCoroutine<T?> { continuation ->
                current = ResultDialogImpl(
                    continuation = continuation,
                    content = content,
                )
            }
        }
        finally {
            current = null
        }
    }
}

private class ResultDialogImpl<T: Any>(
    private val continuation: CancellableContinuation<T?>,
    private val content: @Composable (ResultDialogAction<T>) -> Unit
) : ResultDialog<T> {

    @Composable
    override fun DialogContent() {
        content.invoke(this)
    }

    override fun select(value: T) {
        if (continuation.isActive) continuation.resume(value)
    }

    override fun dismiss() {
        if (continuation.isActive) continuation.resume(null)
    }
}

interface ResultDialog<T: Any>: ResultDialogAction<T> {

    @Composable
    fun DialogContent()
}

interface ResultDialogAction<T: Any> {
    fun select(value: T)
    fun dismiss()
}

@ExperimentalComposeUiApi
@Composable
fun ResultDialogHost(hostState: ResultDialogHostState) {
    val controller = rememberNavController()
    val current = hostState.current
    val systemUi = rememberSystemUiController()

    val statusBar = MaterialTheme.colors.primaryVariant
    val scrim = MaterialTheme.colors.surface.copy(alpha = 0.5f)

    NavHost(navController = controller, startDestination = "nothing") {
        composable("nothing") {
            SideEffect {
                systemUi.setStatusBarColor(statusBar)
            }
        }
        composable("dialog") {
            current ?: return@composable
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(scrim)
                    .noRippleClickable { current.dismiss() },
            ) {
                with (current) { DialogContent() }
                SideEffect {
                    systemUi.setStatusBarColor( scrim.compositeOver(statusBar))
                }
            }
        }
    }
    LaunchedEffect(current) {
        if (current != null) controller.navigate("dialog") { launchSingleTop = true }
        else controller.popBackStack("nothing", inclusive = false)
    }
}