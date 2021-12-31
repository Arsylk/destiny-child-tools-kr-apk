package com.arsylk.mammonsmite.domain.base

import com.arsylk.mammonsmite.domain.safeListFiles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.io.File
import kotlin.reflect.KProperty

class FileListFlow(
    scope: CoroutineScope,
    folder: File,
    filter: (File) -> Boolean = { true },
    ) {
    private val updateChannel = Channel<Unit>(Channel.CONFLATED)
    val flow = updateChannel.receiveAsFlow()
        .onStart { emit(Unit) }
        .mapLatest { folder.safeListFiles(filter) }
        .catch { }
    val shared = flow.stateIn(
        scope, SharingStarted.WhileSubscribed(), emptyList()
    )

    suspend fun update() = updateChannel.send(Unit)

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = shared
}