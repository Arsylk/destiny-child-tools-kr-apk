package com.arsylk.mammonsmite.domain.sync

import com.arsylk.mammonsmite.domain.sumOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeout

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
object SyncBuilder {

    fun sync(block: RootSyncGroup.Builder.() -> Unit): RootSyncGroup {
        return RootSyncGroup.Builder().apply(block).build()
    }

    fun execute(root: RootSyncGroup): Flow<Pair<ProgressStore, Int>> {
        val p = ProgressStore()
        return flow { emit(p) }
            .flatMapLatest { progress ->
                prepareGroupFlow(root.store, root, root.weight, false)
                    .onEach(progress::setProgress)
                    .mapLatest { progress to progress.getProgress().toInt() }
            }
            .catch {
                p.setProgress(ComponentResult(root, Result.Error(it), root.weight))
                it.printStackTrace()
                throw it
            }
            .onCompletion {
                p.iterateMap { syncComponent, simpleProgress ->
                    println("${syncComponent.javaClass.simpleName}(${syncComponent.tag}), ${simpleProgress.result}, ${simpleProgress.weight}", )
                }
            }
    }

    private suspend fun prepareGroupFlow(
        store: SyncGroupStore,
        group: ISyncGroup,
        weight: Float,
        skip: Boolean,
    ): Flow<ComponentResult> {
        val componentList = group.components.sortedBy(SyncComponent::order).toList()
        val weightSum = componentList.sumOf(SyncComponent::weight)

        return (if (skip) emptyFlow() else componentList.asFlow())
            .flatMapMerge(group.concurrency) { component ->
                val componentWeight = weight * (component.weight / weightSum)
                val componentSkip = component
                    .runCatching { !condition.invoke(SyncComponentContext(store)) }
                    .getOrElse { t -> throw SyncComponentConditionException(component, t) }

                when {
                    componentSkip -> {
                        flow {
                            emit(ComponentResult(component, Result.Success(skipped = componentSkip), componentWeight))
                        }
                    }
                    component is ISyncModule -> {
                        flow { emit(component) }
                            .flatMapConcat { module ->
                                channelFlow<Result> {
                                    val context = SyncModuleContext(this, group, store)
                                    withTimeout(module.timeout) {
                                        module.internalFlow(context).collect()
                                    }
                                }
                            }
                            .onCompletion { t ->
                                if (t == null) emit(Result.Success(skipped = componentSkip))
                            }
                            .catch { t ->
                                emit(Result.Error(t))
                                if (component.required) throw SyncModuleException(component, t)
                            }
                            .map { progress ->
                                ComponentResult(component, progress, componentWeight)
                            }
                    }
                    component is ISyncGroup -> {
                        prepareGroupFlow(store, component, componentWeight, componentSkip)
                    }
                    else -> emptyFlow() // compiler type inference problem ?
                }
            }
            .onCompletion { t ->
                if (t == null) emit(ComponentResult(group, Result.Success(skipped = skip), weight * group.weight))
            }
            .catch { t ->
                emit(ComponentResult(group, Result.Error(t), weight * group.weight))
                if (group.required) throw SyncGroupException(group, t)
            }
    }
}