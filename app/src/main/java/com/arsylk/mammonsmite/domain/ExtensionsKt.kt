package com.arsylk.mammonsmite.domain

import android.content.res.TypedArray
import android.graphics.Paint
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.arsylk.mammonsmite.model.common.OperationStateResult
import com.arsylk.mammonsmite.model.pck.packed.PackedPckFile
import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.last
import java.io.File
import java.io.FileReader
import java.io.IOException


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

fun Fragment.launchWhenResumed(block: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.lifecycleScope.launchWhenResumed(block)
}

@Throws(IOException::class, JsonSyntaxException::class, JsonIOException::class)
fun <T> Gson.fromJson(file: File, clazz: Class<T>): T {
    FileReader(file).use {
        return this.fromJson(it, clazz) as T
    }
}