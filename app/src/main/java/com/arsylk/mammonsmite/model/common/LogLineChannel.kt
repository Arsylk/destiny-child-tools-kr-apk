package com.arsylk.mammonsmite.model.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap

interface LogLineChannel {

    fun success(msg: String) { success(msg, null) }
    fun success(msg: String, tag: String?)

    fun info(msg: String) { info(msg, null) }
    fun info(msg: String, tag: String?)

    fun warn(msg: String) { warn(msg, null) }
    fun warn(msg: String, tag: String?)

    fun warn(throwable: Throwable) { warn(throwable, null) }
    fun warn(throwable: Throwable, tag: String?)

    fun error(throwable: Throwable) { error(throwable, null) }
    fun error(throwable: Throwable, tag: String?)

    fun put(line: LogLine)

    fun clear()

    fun stateIn(coroutineScope: CoroutineScope): StateFlow<List<LogLine>>
}

private class LogLineChannelImpl(val onLine: (LogLine) -> Unit) : LogLineChannel {
    private val lines = MutableStateFlow(0 to ConcurrentHashMap<Int, LogLine>())

    override fun success(msg: String, tag: String?) {
        put(LogLine(msg, tag, null, LogLine.Type.SUCCESS))
    }

    override fun info(msg: String, tag: String?) {
        put(LogLine(msg, tag, null, LogLine.Type.INFO))
    }

    override fun warn(msg: String, tag: String?) {
        put(LogLine(msg, tag, null, LogLine.Type.WARN))
    }

    override fun warn(throwable: Throwable, tag: String?) {
        put(LogLine(throwable.javaClass.simpleName, tag, throwable, LogLine.Type.WARN))
    }

    override fun error(throwable: Throwable, tag: String?) {
        put(LogLine(throwable.javaClass.simpleName, tag, throwable, LogLine.Type.ERROR))
    }

    override fun put(line: LogLine) {
        onLine.invoke(line)
        lines.update { (id, map) ->
            map[id] = line
            (id + 1) to map
        }
    }

    override fun clear() {
        lines.update { (id, _) ->
            (id + 1) to ConcurrentHashMap<Int, LogLine>()
        }
    }

    override fun stateIn(coroutineScope: CoroutineScope): StateFlow<List<LogLine>> {
        return lines
            .mapLatest { (_, map) ->
                map.map { it.value }
            }
            .stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())
    }
}

private object EmptyLogLineChannelImpl : LogLineChannel {

    override fun success(msg: String, tag: String?) {
    }

    override fun info(msg: String, tag: String?) {
    }

    override fun warn(msg: String, tag: String?) {
    }

    override fun warn(throwable: Throwable, tag: String?) {
    }

    override fun error(throwable: Throwable, tag: String?) {
    }

    override fun put(line: LogLine) {
    }

    override fun clear() {
    }

    override fun stateIn(coroutineScope: CoroutineScope): StateFlow<List<LogLine>> {
        return MutableStateFlow(emptyList<LogLine>()).asStateFlow()
    }

}

fun LogLineChannel(): LogLineChannel = LogLineChannelImpl {}

fun LogLineChannel(onLine: (LogLine) -> Unit): LogLineChannel = LogLineChannelImpl(onLine)

val EmptyLogLineChannel: LogLineChannel get() = EmptyLogLineChannelImpl