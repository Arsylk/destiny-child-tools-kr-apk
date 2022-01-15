package com.arsylk.mammonsmite.presentation.screen.pck.destinychild

import com.arsylk.mammonsmite.Cfg
import com.arsylk.mammonsmite.domain.base.BaseViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.domain.repo.CharacterRepository
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
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
) : BaseViewModel() {
    private val _destinychildModels = MutableStateFlow(emptyList<ModelPacked>())
    private val _allModels = MutableStateFlow(emptyList<ModelPacked>())
    private val _fileModels = MutableStateFlow(emptyList<ModelPacked>())
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
        listPackedModels()
    }

    fun updateSearchState(modifier: SearchState.() -> SearchState) {
        _searchState.update(modifier)
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
            searchState
                .flatMapLatest { search ->
                    val source = when (tab) {
                        Tab.DESTINYCHILD -> _destinychildModels.deepSearch(search)
                        Tab.ALl -> _allModels
                        Tab.FILES -> _fileModels
                    }
                    source.mapLatest { list ->
                        list.filter { item ->
                            item.matchesQuery(search.text)
                        }
                    }
                }
        }

    private fun Flow<List<ModelPacked>>.deepSearch(search: SearchState) =
        combine(this, repo.viewIdxNames, repo.skillActiveData, repo.skillBuffData) { list, names, skills, buffs ->
            list.asSequence()
                .filter { item ->
                    // search char idx
                    search.charIdx.isBlank() || item.char?.idx
                        ?.contains(search.charIdx, ignoreCase = true) == true
                }
                .filter { item ->
                    // search attributes
                    search.attributes.isEmpty() || item.char?.attribute in search.attributes
                }
                .filter { item ->
                    // search roles
                    search.roles.isEmpty() || item.char?.role in search.roles
                }
                .filter { item ->
                    // search stars
                    search.stars == 0 || item.char?.stars ?: 0 >= search.stars
                }
                .filter { item ->
                    // search skin name
                    val resolvedNames = listOfNotNull(
                        *item.viewIdxList.mapNotNull { names[it] }.toTypedArray(),
                        item.char?.koreanName,
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
                    val charSkills = item.char?.skills
                        .orEmpty().mapNotNull(skills::get)
                    val skillBuffs = charSkills.flatMap {
                        it.buffs.mapNotNull(buffs::get)
                    }

                    // search logic
                    val matchLogic = search.skillLogic.isBlank() || skillBuffs.any {
                        it.logic.contains(search.skillLogic, ignoreCase = true)
                    }
                    if (!matchLogic) return@filter false

                    // search skill idx
                    val matchSkillIdx = search.skillIdx.isBlank() || charSkills.any {
                        it.idx.contains(search.skillIdx, ignoreCase = true)
                    }
                    if (!matchSkillIdx) return@filter false

                    // search buff idx
                    val matchBuffIdx = search.buffIdx.isBlank() || skillBuffs.any {
                        it.idx.contains(search.buffIdx, ignoreCase = true)
                    }
                    if (!matchBuffIdx) return@filter false

                    return@filter true
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
                        iconSrc = "${Cfg.API_URL}static/icons/buff/${data.idx}.png"
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
}

sealed class Effect : UiEffect
