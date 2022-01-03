package com.arsylk.mammonsmite.presentation.screen.pck.destinychild

import com.arsylk.mammonsmite.domain.base.BaseViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.domain.repo.CharacterRepository
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import com.arsylk.mammonsmite.model.destinychild.ViewIdx.Companion.ordered
import com.arsylk.mammonsmite.presentation.screen.pck.destinychild.PckDestinyChildScreen.Tab
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

@ExperimentalCoroutinesApi
class PckDestinyChildViewModel(
    private val prefs: AppPreferences,
    private val repo: CharacterRepository,
) : BaseViewModel() {
    private val _destinychildModels = MutableStateFlow(emptyList<ModelPacked>())
    private val _allModels = MutableStateFlow(emptyList<ModelPacked>())
    private val _fileModels = MutableStateFlow(emptyList<ModelPacked>())
    private val _searchQuery = MutableStateFlow("")
    private val _selectedTab = MutableStateFlow(Tab.ALl)

    val filteredModels = _selectedTab.toFilteredModels()
    val searchQuery by lazy(_searchQuery::asStateFlow)
    val selectedTab by lazy(_selectedTab::asStateFlow)

    init {
        listPackedModels()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectTab(tab: Tab) {
        _selectedTab.value = tab
    }

    private fun listPackedModels() {
        val _id = AtomicInteger(0)
        withLoading {
            val files = IFile(prefs.destinychildModelsPath)
                .listFiles()
                .filter { it.name.endsWith(".pck") }
            val viewIdxFiles = buildMap<ViewIdx?, List<IFile>> {
                files.forEach { file ->
                    val viewIdx = ViewIdx.parse(file.name)
                    val entry = get(viewIdx).orEmpty()
                    put(viewIdx, entry + file)
                }
            }
            val viewIdxChars = repo.viewIdxChars.value
            val viewIdxNames = repo.viewIdxNames.value

            withLoading(tag = "destinychild") {
                val x = measureTimeMillis {
                    val destinychildModels = repo.charData.value
                        .map { charData ->
                            val viewIdxList = viewIdxChars
                                .mapNotNull { (k, v) ->
                                    if (v.idx == charData.idx) k else null
                                }
                            val fileList = viewIdxList
                                .flatMap { viewIdx -> viewIdxFiles[viewIdx].orEmpty() }
                                .distinct()
                            ModelPacked(
                                _id = _id.getAndIncrement(),
                                files = fileList,
                                viewIdxList = viewIdxList,
                                char = charData,
                                resolvedName = viewIdxList.ordered()
                                    .map { viewIdxNames[it] }
                                    .firstOrNull()
                            )
                        }
                    _destinychildModels.value = destinychildModels
                }
                println("destinychild took: ${x}ms")
            }
            withLoading(tag = "all") {
                val x = measureTimeMillis {
                    val allModels = (viewIdxFiles.keys + viewIdxChars.keys)
                        .flatMap { viewIdx ->
                            if (viewIdx == null)
                                return@flatMap viewIdxFiles[viewIdx].orEmpty().map { file ->
                                    ModelPacked(
                                        _id = _id.getAndIncrement(),
                                        files = listOf(file),
                                        viewIdxList = emptyList(),
                                        char = null,
                                        resolvedName = null,
                                    )
                                }
                            listOf(
                                ModelPacked(
                                    _id = _id.getAndIncrement(),
                                    files = viewIdxFiles[viewIdx].orEmpty(),
                                    viewIdxList = listOf(viewIdx),
                                    char = viewIdxChars[viewIdx],
                                    resolvedName = viewIdxNames[viewIdx],
                                )
                            )
                        }
                    _allModels.value = allModels.sortedBy { it.primaryText }
                }
                println("all took: ${x}ms")
            }
            withLoading(tag = "files") {
                val x = measureTimeMillis {
                    val fileModels = files.map { file ->
                        val viewIdx = ViewIdx.parse(file.name)
                        ModelPacked(
                            _id = _id.getAndIncrement(),
                            files = listOf(file),
                            viewIdxList = listOfNotNull(viewIdx),
                            char = viewIdxChars[viewIdx],
                            resolvedName = viewIdxNames[viewIdx],
                        )
                    }
                    _fileModels.value = fileModels
                }
                println("files took: ${x}ms")
            }
        }
    }

    private fun Flow<Tab>.toFilteredModels() =
        flatMapLatest { tab ->
            _searchQuery
                .flatMapLatest { query ->
                    val source = when (tab) {
                        Tab.DESTINYCHILD -> _destinychildModels
                        Tab.ALl -> _allModels
                        Tab.FILES -> _fileModels
                    }
                    source.mapLatest { list ->
                        list.filter { item ->
                            item.matchesQuery(query)
                        }
                    }
                }
        }
}

sealed class Effect : UiEffect
