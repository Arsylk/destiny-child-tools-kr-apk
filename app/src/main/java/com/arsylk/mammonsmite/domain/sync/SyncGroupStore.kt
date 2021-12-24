package com.manimani.app.domain.sync

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass


@Suppress("UNCHECKED_CAST")
data class SyncGroupStore(
    private val dict: ConcurrentHashMap<Any, Any> = ConcurrentHashMap<Any, Any>()
) {
    private val _updates = MutableSharedFlow<Pair<Any, Any>>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )
    val updates = _updates.asSharedFlow()

    fun <T: Any> putByClassInstance(kClass: KClass<T>, value: T?) {
        dict[kClass] = value ?: return
        _updates.tryEmit(kClass to dict[kClass] as T)
    }

    fun <T: Any> getByClassInstance(kClass: KClass<T>): T? = dict[kClass] as? T

    inline fun <reified T: Any> put(value: T?) = putByClassInstance(T::class, value)

    inline operator fun <reified T: Any> plusAssign(value: T?) = putByClassInstance(T::class, value)

    inline fun <reified T : Any> get(): T? = getByClassInstance(T::class)

    operator fun <T: Any> set(key: String, value: T?) {
        dict[key] = value ?: return
        _updates.tryEmit(key to dict[key] as T)
    }

    operator fun <T: Any> get(key: String) = dict[key] as? T

    suspend fun <T: Any> await(timeout: Long, key: String): kotlin.Result<T> {
        val entry = dict[key] as? T
        if (entry != null) return kotlin.Result.success(entry)

        val result = kotlin.runCatching {
            withTimeout(timeout) {
                updates.filter { (k, _) -> (k as? String) == key }
                    .map { (_, v) -> v as? T }
                    .filterNotNull()
                    .first()
            }
        }

        return if (result.isFailure) {
            (dict[key] as? T)
                ?.let { kotlin.Result.success(it) }
                ?: result
        } else result
    }

    suspend fun <T: Any> awaitOrNull(timeout: Long, key: String): T? = await<T>(timeout, key).getOrNull()

    suspend inline fun <reified T : Any> await(timeout: Long): kotlin.Result<T> {
        val entry = getByClassInstance(T::class)
        if (entry != null) return kotlin.Result.success(entry)

        val result = kotlin.runCatching {
            withTimeout(timeout) {
                updates.filter { (k, _) -> (k as? KClass<T>) == T::class }
                    .map { (_, v) -> v as? T }
                    .filterNotNull()
                    .first()
            }
        }

        return if (result.isFailure) {
            getByClassInstance(T::class)
                ?.let { kotlin.Result.success(it) }
                ?: result
        } else result
    }

    suspend inline fun <reified T: Any> awaitOrNull(timeout: Long): T? = await<T>(timeout).getOrNull()

    suspend fun <T: Any> awaitForever(key: String): T {
        val entry = dict[key] as? T
        if (entry != null) return entry

        return updates
            .onStart { key to dict[key] as? T }
            .filter { (k, _) -> (k as? String) == key }
            .map { (_, v) -> v as? T }
            .filterNotNull()
            .take(1)
            .first()
    }

    suspend inline fun <reified T : Any> awaitForever(): T {
        val entry = getByClassInstance(T::class)
        if (entry != null) return entry

        return updates
            .onStart { T::class to getByClassInstance(T::class) }
            .filter { (k, _) -> k as? KClass<T> == T::class }
            .map { (_, v) -> v as? T }
            .filterNotNull()
            .take(1)
            .first()
    }
}