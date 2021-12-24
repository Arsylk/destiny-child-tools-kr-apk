package com.arsylk.mammonsmite.model.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

object LogLineChannel {
    val Default: SendChannel<LogLine> = Channel(capacity = Channel.CONFLATED, onBufferOverflow = BufferOverflow.DROP_LATEST)
}

fun LogLineChannel() = Channel<LogLine>(
    capacity = 5,
    onBufferOverflow = BufferOverflow.SUSPEND,
    onUndeliveredElement = {},
)

fun ReceiveChannel<LogLine>.stateIn(coroutineScope: CoroutineScope): StateFlow<List<LogLine>> {
    return channelFlow {
        val mutableList = mutableListOf<LogLine>()
        val job = launch {
            this@stateIn.receiveAsFlow().collect {
                mutableList += it
                send(mutableList.toList())
            }
        }
        awaitClose { job.cancel() }
    }.stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())
}

suspend fun SendChannel<LogLine>.info(msg: String, tag: String? = null) {
    send(LogLine(msg, tag, null, LogLine.Type.INFO))
}

suspend fun SendChannel<LogLine>.error(throwable: Throwable, tag: String? = null) {
    send(LogLine(throwable.javaClass.simpleName, tag, throwable, LogLine.Type.ERROR))
}
