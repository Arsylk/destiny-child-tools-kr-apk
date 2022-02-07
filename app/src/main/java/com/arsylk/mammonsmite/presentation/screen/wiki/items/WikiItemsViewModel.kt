package com.arsylk.mammonsmite.presentation.screen.wiki.items

import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.destinychild.ItemRepository
import com.arsylk.mammonsmite.model.common.asUiResultFlow
import com.arsylk.mammonsmite.model.common.mapAsUiResult
import com.arsylk.mammonsmite.model.common.uiResultOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class WikiItemsViewModel(
    private val repo: ItemRepository,
) : EffectViewModel<Effect>() {
    private val _selectedTab = MutableStateFlow(WikiItemsTab.All)
    val selectedTab by lazy(_selectedTab::asStateFlow)
    val items = selectedTab.prepareItems()

    fun selectTab(tab: WikiItemsTab) {
        _selectedTab.value = tab
    }

    private fun Flow<WikiItemsTab>.prepareItems() =
        flatMapLatest { tab ->
            repo.itemsByCategories(tab.categories)
        }.asUiResultFlow()
}
sealed class Effect : UiEffect