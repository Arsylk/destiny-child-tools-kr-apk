package com.arsylk.mammonsmite.presentation.fragment.pck.destinychild

import androidx.lifecycle.viewModelScope
import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.domain.repo.CharacterRepository
import com.arsylk.mammonsmite.domain.safeListFiles
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import com.arsylk.mammonsmite.model.destinychild.ViewIdx.Companion.ordered
import com.arsylk.mammonsmite.presentation.fragment.pck.destinychild.PckDestinyChildFragment.Tab
import com.arsylk.mammonsmite.presentation.fragment.pck.destinychild.adapter.ModelPacked
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

@ExperimentalCoroutinesApi
class ModelsDestinyChildViewModel(
    private val prefs: AppPreferences,
    private val repo: CharacterRepository,
) : EffectViewModel<Effect>() {
    private val _destinychildModels = MutableStateFlow(emptyList<ModelPacked>())
    private val _allModels = MutableStateFlow(emptyList<ModelPacked>())
    private val _fileModels = MutableStateFlow(emptyList<ModelPacked>())
    private val _searchQuery = MutableStateFlow("")
    private val _searchTab = MutableStateFlow(Tab.ALl)

    val filteredModels = _searchTab.toFilteredModels()
    val searchQuery by lazy(_searchQuery::asStateFlow)
    val searchTab by lazy(_searchTab::asStateFlow)

    init {
        listPackedModels()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchTab(tab: Tab) {
        _searchTab.value = tab
    }

    private fun listPackedModels() {
        val _id = AtomicInteger(0)
        viewModelScope.launch(Dispatchers.IO) {
            val files = File(prefs.destinychildModelsPath)
                .safeListFiles { it.name.endsWith(".pck") && it.isFile }
            val viewIdxFiles = buildMap<ViewIdx?, List<File>> {
                files.forEach { file ->
                    val viewIdx = ViewIdx.parse(file.name)
                    val entry = get(viewIdx).orEmpty()
                    put(viewIdx, entry + file)
                }
            }
            val viewIdxChars = repo.viewIdxChars.value
            val viewIdxNames = repo.viewIdxNames.value

            withLoading(tag = "destinychild") {
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
            withLoading(tag = "all") {
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
            withLoading(tag = "files") {
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
