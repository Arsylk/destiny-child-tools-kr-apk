package com.arsylk.mammonsmite.domain.destinychild

import com.arsylk.mammonsmite.domain.db.AppDatabase
import com.arsylk.mammonsmite.model.db.item.ItemDataCategory
import com.arsylk.mammonsmite.model.db.item.ItemDataType
import com.arsylk.mammonsmite.model.destinychild.ItemData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class ItemRepository(
    private val scope: CoroutineScope,
    private val database: AppDatabase,
    private val engLocaleRepository: EngLocaleRepository,
) {
    private val itemDataDao by lazy(database::itemDataDao)

    private val _itemData = MutableStateFlow<Map<String, ItemData>>(emptyMap())
    val itemData by lazy(_itemData::asStateFlow)
    val test = combine(itemData, engLocaleRepository.engLocale) { items, locale ->
        val x = items.entries.map { (idx, item) ->
            val name = locale?.itemNamesFile?.dict?.get(idx)?.substringBefore('\t')
            item to name
        }.sortedBy { it.first.idx.toIntOrNull() ?: 0 }
        .groupBy { it.first.viewIdx }
        x
    }
    .flowOn(Dispatchers.Default)
    .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    fun itemsByCategories(collection: Collection<ItemDataCategory>): Flow<List<com.arsylk.mammonsmite.model.db.item.ItemData>> {
        return itemDataDao.selectCategoriesFlow(collection.toSet())
    }
}