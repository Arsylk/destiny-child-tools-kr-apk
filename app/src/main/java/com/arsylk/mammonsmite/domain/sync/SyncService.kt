package com.arsylk.mammonsmite.domain.sync

import com.arsylk.mammonsmite.domain.files.CommonFiles
import com.arsylk.mammonsmite.domain.repo.CharacterRepository
import com.arsylk.mammonsmite.domain.retrofit.RetrofitApiService
import com.arsylk.mammonsmite.domain.sumOf
import com.arsylk.mammonsmite.domain.sync.module.cacheableModule
import com.arsylk.mammonsmite.model.common.HashUtils
import com.arsylk.mammonsmite.model.destinychild.LocalePatch
import kotlinx.coroutines.flow.*
import com.manimani.app.domain.sync.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SyncService(
    private val json: Json,
    private val service: RetrofitApiService,
    private val characterRepository: CharacterRepository,
) {

    fun getSyncFlow(): Flow<Pair<ProgressStore, Int>> {
        val sync = sync {
            group {
                tag = "dump data"
                required = true
                concurrency = 5

                module {
                    tag = "char data"
                    action {
                        characterRepository.fetchCharData()
                    }
                }
                module {
                    tag = "character skin data"
                    action {
                        characterRepository.fetchCharacterSkinData()
                    }
                }
                module {
                    tag = "english patch data"
                    action {
                        characterRepository.fetchEngLocale()
                    }
                }

                group {
                    tag = "skill data"
                    concurrency = 3
                    required = false

                    module {
                        tag = "skill active data"
                        action {
                            characterRepository.fetchSkillActiveData()
                        }
                    }

                    module {
                        tag = "skill buff data"
                        action {
                            characterRepository.fetchSkillBuffData()
                        }
                    }

                    module {
                        tag = "skill ignition data"
                        action {
                            characterRepository.fetchIgnitionCharacterSkillData()
                        }
                    }
                }
            }

//            group {
//                tag = "locale group"
//                required = true
//
//                cacheableModule<LocalePatch> {
//                    tag = "english patch module"
//                    file = CommonFiles.Internal.englishPatchFile
//                    fetch { service.getEnglishPatch(it) }
//                    serialize { json.encodeToString(it) }
//                    deserialize { json.decodeFromString(it) }
//                }
//            }
        }
        return execute(sync)
    }

    private fun sync(block: RootSyncGroup.Builder.() -> Unit): RootSyncGroup {
        return RootSyncGroup.Builder().apply(block).build()
    }


    private fun execute(root: RootSyncGroup): Flow<Pair<ProgressStore, Int>> {
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