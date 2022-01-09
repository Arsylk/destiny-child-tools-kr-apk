package com.arsylk.mammonsmite.domain

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.TypedArray
import android.graphics.Paint
import android.os.Build
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.model.common.OperationStateResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.last
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.IOException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


fun File?.safeListFiles(filter: (file: File) -> Boolean = { true }): List<File> {
    return kotlin.runCatching {
        this?.listFiles()?.filter(filter)?.toList()
    }.getOrNull().orEmpty()
}

fun TypedArray.tryUse(block: TypedArray.() -> Unit) {
    try {
        block.invoke(this)
    } finally {
        recycle()
    }
}

inline var TextView.underline: Boolean
    set(visible) {
        paintFlags = if (visible) paintFlags or Paint.UNDERLINE_TEXT_FLAG
        else paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
    }
    get() = paintFlags and Paint.UNDERLINE_TEXT_FLAG == Paint.UNDERLINE_TEXT_FLAG

@Throws(Throwable::class)
suspend fun <T> Flow<OperationStateResult<T>>.asSuccess(): T {
    val result = this.last()
    when (result) {
        is OperationStateResult.Success -> return result.result
        is OperationStateResult.Failure -> throw result.t
        else -> throw IllegalStateException()
    }
}
@Throws(Throwable::class)
suspend fun <T> Flow<OperationStateResult<T>>.asResult(): Result<T> {
    val result = this.last()
    when (result) {
        is OperationStateResult.Success -> return Result.success(result.result)
        is OperationStateResult.Failure -> return Result.failure(result.t)
        else -> return Result.failure(IllegalStateException())
    }
}

fun Fragment.launchWhenResumed(block: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.lifecycleScope.launchWhenResumed(block)
}

@ExperimentalSerializationApi
@Throws(IOException::class, SerializationException::class)
inline fun <reified T> Json.decodeFromFile(file: File): T {
    file.inputStream().buffered().use {
        return decodeFromStream(it)
    }
}

@ExperimentalSerializationApi
@Throws(IOException::class, SerializationException::class)
inline fun <reified T> Json.encodeToFile(value: T, file: File) {
    file.outputStream().buffered().use {
        encodeToStream(value, it)
    }
}


inline fun <T> Iterable<T>.sumOf(selector: (T) -> Float): Float {
    var sum: Float = 0.toFloat()
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

fun <K, V> MutableMap<K, V>.putIfAbsentCompat(key: K, value: V): V? {
    var v = get(key)
    if (v == null) {
        v = put(key, value)
    }
    return v
}

fun String.capitalizeFirstOnly(): String =
    this.lowercase().replaceFirstChar { it.uppercase() }

@Suppress("DEPRECATION")
fun Activity.setFullscreenCompat(fullscreen: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val types = WindowInsets.Type.statusBars()
        if (fullscreen) window.insetsController?.hide(types)
        else window.insetsController?.show(types)
    } else {
        val flag = WindowManager.LayoutParams.FLAG_FULLSCREEN
        if (fullscreen) window.setFlags(flag, flag)
        else window.clearFlags(flag)
    }
}

@OptIn(ExperimentalContracts::class)
inline fun<T>  use(value1: T?, block: (T) -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    if (value1 != null) block.invoke(value1)
}

@OptIn(ExperimentalContracts::class)
inline fun<T, R>  use(value1: T?, value2: R?, block: (T, R) -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    if (value1 != null && value2 != null)
        block.invoke(value1, value2)
}

inline val isAndroid11 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

inline fun Modifier.noRippleClickable(crossinline onClick: ()->Unit): Modifier = composed {
    clickable(indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}

@SuppressLint("ComposableNaming")
@Composable
fun <T: UiEffect> EffectViewModel<T>.onEffect(key: Any? = Unit, action: suspend (effect: T) -> Unit) {
    LaunchedEffect(key) {
        effect.collect(action)
    }
}

fun Throwable.toSnackbarMessage(): String = "${javaClass.simpleName}: $cause"

suspend fun ScaffoldState.dismissAndShow(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    snackbarHostState.run {
        currentSnackbarData?.dismiss()
        showSnackbar(message, actionLabel, duration)
    }
}