package com.arsylk.mammonsmite.domain.sync

import com.arsylk.mammonsmite.domain.putIfAbsentCompat
import com.arsylk.mammonsmite.domain.sumOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


sealed class Result {
    abstract val percentage: Float

    data class Success(
        val skipped: Boolean = false,
    ) : Result() {
        override val percentage = 100.0f
    }
    class Progress(
        override val percentage: Float,
        val message: String? = null,
    ) : Result()
    data class Error(
        val throwable: Throwable,
    ) : Result() {
        override val percentage = 100.0f
    }
}

data class ComponentResult(
    val component: SyncComponent,
    val result: Result,
    val weight: Float,
)

data class SimpleProgress(
    val result: Result,
    val weight: Float,
)
class ProgressStore {
    private val mapMutex = Mutex()
    private val map = mutableMapOf<SyncComponent, SimpleProgress>()
    private val messagesMutex = Mutex()
    private val messages = mutableListOf<String>()

    suspend fun setProgress(componentResult: ComponentResult) {
        val component = componentResult.component
        mapMutex.withLock(map) {
            map[component] = SimpleProgress(componentResult.result, componentResult.weight)
        }
        when (component) {
            is ISyncModule -> {
                val progress = componentResult.result as? Result.Progress
                if (progress?.message != null) {
                    messagesMutex.withLock(messages) {
                        messages += progress.message
                    }
                }
            }
            is ISyncGroup -> {
                setGroupProgress(component, componentResult.result, componentResult.weight)
            }
        }
    }

    private suspend fun setGroupProgress(group: ISyncGroup, result: Result, weight: Float) {
        val fillerResult = when (result) {
            is Result.Success -> Result.Success(skipped = true)
            is Result.Error -> Result.Error(result.throwable)
            else -> return
        }
        group.components.forEach { component ->
            val weightSum = group.components.sumOf { it.weight }
            val componentWeight = (component.weight / weightSum) * weight

            mapMutex.withLock(map) {

                map.putIfAbsentCompat(component, SimpleProgress(fillerResult, componentWeight))
            }

            if (component is ISyncGroup)
                setGroupProgress(component, fillerResult, componentWeight)
        }
    }

    suspend fun getProgress(): Float {
        mapMutex.withLock(map) {
            return map.filter { (k, _) -> k is ISyncModule }
                .values
                .sumOf { it.weight * it.result.percentage }
        }
    }

    suspend fun iterateMap(onEach: (SyncComponent, SimpleProgress) -> Unit) {
        mapMutex.withLock(map) {
            for ((component, progress) in map) {
                onEach.invoke(component, progress)
            }
        }
    }
}