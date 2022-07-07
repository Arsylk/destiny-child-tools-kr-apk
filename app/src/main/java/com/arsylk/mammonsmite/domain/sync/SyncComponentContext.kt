package com.arsylk.mammonsmite.domain.sync

import androidx.annotation.IntRange
import kotlinx.coroutines.channels.SendChannel

interface ISyncComponentContext {
    val store: SyncGroupStore
}

data class SyncComponentContext(
    override val store: SyncGroupStore
) : ISyncComponentContext

data class SyncModuleContext(
    private val progressChannel: SendChannel<Result.Progress>,
    val group: ISyncGroup,
    override val store: SyncGroupStore,
) : ISyncComponentContext {

    suspend fun postProgress(@IntRange(from = 0, to = 100) progress: Int, message: String? = null) {
        progressChannel.send(Result.Progress(progress.toFloat(), message))
    }
}