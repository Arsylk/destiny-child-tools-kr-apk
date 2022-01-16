package com.arsylk.mammonsmite.presentation.screen.pck.destinychild

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.arsylk.mammonsmite.Cfg
import com.arsylk.mammonsmite.domain.base.BaseViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.domain.repo.CharacterRepository
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import com.arsylk.mammonsmite.model.destinychild.ViewIdx.Companion.naturalOrder
import com.arsylk.mammonsmite.model.destinychild.ViewIdx.Companion.ordered
import com.arsylk.mammonsmite.presentation.composable.Autocomplete
import com.arsylk.mammonsmite.presentation.screen.pck.destinychild.PckDestinyChildScreen.Tab
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicInteger

@ExperimentalCoroutinesApi
class PckDestinyChildViewModel(
    private val prefs: AppPreferences,
    private val repo: CharacterRepository,
    private val handle: SavedStateHandle,
) : BaseViewModel() {
    private val _files = MutableStateFlow(emptyList<IFile>())
    private val _viewIdxFiles = MutableStateFlow(emptyMap<ViewIdx?, List<IFile>>())
    private val destinychildModels = prepareDestinyChildModels()
    private val allModels = prepareAllModels()
    private val fileModels = prepareFileModels()
    private val _searchState = MutableStateFlow(SearchState.Default)
    private val _selectedTab = MutableStateFlow(Tab.ALl)
    val filteredModels = _selectedTab.toFilteredModels()
    val searchState by lazy(_searchState::asStateFlow)
    val selectedTab by lazy(_selectedTab::asStateFlow)
    val skillLogicSuggestions = searchState.prepareSkillLogicSuggestions()
    val charIdxSuggestions = searchState.prepareCharIdxSuggestions()
    val skillIdxSuggestions = searchState.prepareSkillIdxSuggestions()
    val buffIdxSuggestions = searchState.prepareBuffIdxSuggestions()


    init {
        handle.get<Tab>(TAB_KEY)?.also(::selectTab)
        reloadFiles()
    }

    fun updateSearchState(modifier: SearchState.() -> SearchState) {
        _searchState.update(modifier)
    }

    fun selectTab(tab: Tab) {
        _selectedTab.value = tab
        handle.set(TAB_KEY, tab)
    }

    private fun reloadFiles() {
        withLoading(tag = "list files") {
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
            _files.value = files
            _viewIdxFiles.value = viewIdxFiles
        }
    }

    private fun prepareDestinyChildModels() =
        combine(repo.fullCharacters, _viewIdxFiles) { characters, files ->
            characters.values.sortedBy { it.data.idx }.map { char ->
                val viewIdxList = char.viewIdxNames.keys.ordered()
                val fileList = viewIdxList
                    .flatMap { viewIdx -> files[viewIdx].orEmpty() }
                    .distinct()
                ModelPacked(
                    key = "dc:${char.data.idx}",
                    files = fileList,
                    viewIdxList = viewIdxList.naturalOrder(),
                    char = char,
                    resolvedName = viewIdxList
                        .map { char.viewIdxNames[it] }
                        .firstOrNull(),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private fun prepareAllModels() =
        combine(
            repo.fullCharacters,
            repo.viewIdxChars,
            repo.viewIdxNames,
            _viewIdxFiles,
        ) { characters, viewIdxChars, viewIdxNames, files ->
            (files.keys + viewIdxChars.keys)
                .flatMap { viewIdx ->
                    if (viewIdx == null)
                        return@flatMap files[viewIdx].orEmpty().map { file ->
                            ModelPacked(
                                key = "all:empty:${file.name}",
                                files = listOf(file),
                                viewIdxList = emptyList(),
                                char = null,
                                resolvedName = null,
                            )
                        }
                    listOf(
                        ModelPacked(
                            key = "all:view_idx:${viewIdx.string}",
                            files = files[viewIdx].orEmpty(),
                            viewIdxList = listOf(viewIdx),
                            char = characters[viewIdxChars[viewIdx]?.idx],
                            resolvedName = viewIdxNames[viewIdx],
                        )
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private fun prepareFileModels() =
        combine(
            repo.fullCharacters,
            repo.viewIdxChars,
            repo.viewIdxNames,
            _files,
        ) { characters, viewIdxChars, viewIdxNames, files ->
            files.map { file ->
                val viewIdx = ViewIdx.parse(file.name)
                ModelPacked(
                    key = "file:${file.name}",
                    files = listOf(file),
                    viewIdxList = listOfNotNull(viewIdx),
                    char = characters[viewIdxChars[viewIdx]?.idx],
                    resolvedName = viewIdxNames[viewIdx],
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private fun Flow<Tab>.toFilteredModels() =
        flatMapLatest { tab ->
            searchState
                .flatMapLatest { search ->
                    val source = when (tab) {
                        Tab.DESTINYCHILD -> destinychildModels.deepSearch(search)
                        Tab.ALl -> allModels
                        Tab.FILES -> fileModels
                    }
                    source.mapLatest { list ->
                        list.filter { item ->
                            item.matchesQuery(search.text)
                        }
                    }
                }
        }

    private fun Flow<List<ModelPacked>>.deepSearch(search: SearchState) =
        mapLatest { list ->
            list.asSequence()
                .filter { item ->
                    // search char idx
                    search.charIdx.isBlank() || item.char?.data?.idx
                        ?.contains(search.charIdx, ignoreCase = true) == true
                }
                .filter { item ->
                    // search attributes
                    search.attributes.isEmpty() || item.char?.data?.attribute in search.attributes
                }
                .filter { item ->
                    // search roles
                    search.roles.isEmpty() || item.char?.data?.role in search.roles
                }
                .filter { item ->
                    // search stars
                    search.stars == 0 || item.char?.data?.stars ?: 0 >= search.stars
                }
                .filter { item ->
                    // search skin name
                    val resolvedNames = listOfNotNull(
                        *item.char?.viewIdxNames?.values.orEmpty().toTypedArray(),
                        item.char?.data?.koreanName,
                    )
                    search.skin.isBlank() || resolvedNames.any {
                        it.contains(search.skin, ignoreCase = true)
                    }
                }
                .filter { item ->
                    // search view idx
                    search.viewIdx.isBlank() || item.viewIdxList.any {
                        it.string.contains(search.viewIdx, ignoreCase = true)
                    }
                }
                .filter { item ->
                    // search skills
                    val charSkills = item.char?.allSkills.orEmpty()
                    val skillBuffs = charSkills.flatMap { it.buffs }

                    // search logic
                    val matchLogic = search.skillLogic.isBlank() || skillBuffs.any {
                        it.data.logic.contains(search.skillLogic, ignoreCase = true)
                    }
                    if (!matchLogic) return@filter false

                    // search skill idx
                    val matchSkillIdx = search.skillIdx.isBlank() || charSkills.any {
                        it.data?.idx?.contains(search.skillIdx, ignoreCase = true) == true
                    }
                    if (!matchSkillIdx) return@filter false

                    // search buff idx
                    val matchBuffIdx = search.buffIdx.isBlank() || skillBuffs.any {
                        it.data.idx.contains(search.buffIdx, ignoreCase = true)
                    }
                    if (!matchBuffIdx) return@filter false

                    return@filter true
                }
                .filter {  item ->
                    // search skill contents
                    search.skillsFuzzy.isBlank() ||item.char?.allSkills.orEmpty().any {
                        it.text?.contains(search.skillsFuzzy, ignoreCase = true) == true
                    }
                }
                .toList()
        }

    private fun Flow<SearchState>.prepareSkillLogicSuggestions() =
        repo.skillBuffData
            .map { data -> data.values.distinctBy { it.logic } }
            .flatMapLatest { list ->
                map { search ->
                    list.filter {
                        it.logic.contains(search.skillLogic, ignoreCase = true)
                    }
                }
            }
            .map { list -> list.sortedBy { it.logic } }
            .map { list ->
                list.map { data ->
                    Autocomplete(
                        text = data.logic,
                        iconSrc = data.iconUrl
                    )
                }
            }

    private fun Flow<SearchState>.prepareCharIdxSuggestions() =
        repo.charData
            .map { data -> data.distinctBy { it.idx } }
            .flatMapLatest { list ->
                map { search ->
                    list.filter {
                        it.idx.contains(search.charIdx, ignoreCase = true)
                    }
                }
            }
            .map { list -> list.sortedBy { it.idx } }
            .map { list ->
                list.map { data ->
                    Autocomplete(
                        text = data.idx,
                    )
                }
            }

    private fun Flow<SearchState>.prepareSkillIdxSuggestions() =
        repo.skillActiveData
            .map { data -> data.keys }
            .flatMapLatest { list ->
                map { search ->
                    list.filter {
                        it.contains(search.skillIdx, ignoreCase = true)
                    }
                }
            }
            .map { list -> list.sorted() }
            .map { list ->
                list.map { data ->
                    Autocomplete(
                        text = data,
                    )
                }
            }

    private fun Flow<SearchState>.prepareBuffIdxSuggestions() =
        repo.skillBuffData
            .map { data -> data.values.distinctBy { it.idx } }
            .flatMapLatest { list ->
                map { search ->
                    list.filter {
                        it.idx.contains(search.buffIdx, ignoreCase = true)
                    }
                }
            }
            .map { list -> list.sortedBy { it.idx } }
            .map { list ->
                list.map { data ->
                    Autocomplete(
                        text = data.idx,
                    )
                }
            }

    companion object {
        private const val TAB_KEY = "tab"
        private const val SEARCH_KEY = "search"
    }
}

sealed class Effect : UiEffect
