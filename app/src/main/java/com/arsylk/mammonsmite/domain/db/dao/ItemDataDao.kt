package com.arsylk.mammonsmite.domain.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.arsylk.mammonsmite.model.db.item.ItemData
import com.arsylk.mammonsmite.model.db.item.ItemDataCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDataDao {

    @Insert(entity = ItemData::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(collection: Collection<ItemData>)

    @Query("SELECT * FROM item_data;")
    fun selectAllFlow(): Flow<List<ItemData>>

    @Query("""
        SELECT * FROM item_data 
        WHERE category IN (:categories)
        ORDER BY category, type, view_idx, idx;
    """)
    fun selectCategoriesFlow(categories: Set<ItemDataCategory>): Flow<List<ItemData>>
}