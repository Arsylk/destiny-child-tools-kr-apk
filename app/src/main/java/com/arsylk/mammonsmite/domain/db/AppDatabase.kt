package com.arsylk.mammonsmite.domain.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.arsylk.mammonsmite.domain.db.converter.ViewIdxConverter
import com.arsylk.mammonsmite.domain.db.dao.ItemDataDao
import com.arsylk.mammonsmite.model.db.item.ItemData

@Database(
    entities = [
        ItemData::class
    ],
    exportSchema = false,
    version = 1,
)
@TypeConverters(ViewIdxConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDataDao(): ItemDataDao
}