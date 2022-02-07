package com.arsylk.mammonsmite.model.db.item

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arsylk.mammonsmite.Cfg
import com.arsylk.mammonsmite.model.destinychild.ViewIdx

@Entity(tableName = "item_data")
data class ItemData(
    @PrimaryKey
    @ColumnInfo(name = "idx")
    val idx: String,

    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "grade")
    val grade: String,
    @ColumnInfo(name = "type")
    val type: ItemDataType,
    @ColumnInfo(name = "category")
    val category: ItemDataCategory,
    @ColumnInfo(name = "view_idx")
    val viewIdx: ViewIdx,

    @ColumnInfo(name = "hp")
    val hp: String,
    @ColumnInfo(name = "atk")
    val atk: String,
    @ColumnInfo(name = "def")
    val def: String,
    @ColumnInfo(name = "agi")
    val agi: String,
    @ColumnInfo(name = "cri")
    val cri: String,

    @ColumnInfo(name = "add_option")
    val addOption: String,
    @ColumnInfo(name = "option_1_group_idx")
    val option1GroupIdx: String,
    @ColumnInfo(name = "option_2_group_idx")
    val option2GroupIdx: String,
    @ColumnInfo(name = "option_3_group_idx")
    val option3GroupIdx: String,

    @ColumnInfo(name = "buy_arena_coin")
    val buyArenaCoin: String,
    @ColumnInfo(name = "buy_dungeon_coin")
    val buyDungeonCoin: String,
    @ColumnInfo(name = "buy_spa_coin")
    val buySpaCoin: String,

    @ColumnInfo(name = "enable_library")
    val enableLibrary: String,
    @ColumnInfo(name = "enhancement")
    val enhancement: String,
    @ColumnInfo(name = "enhancement_status_idx")
    val enhancementStatusIdx: String,
    @ColumnInfo(name = "ignition_tree_idx")
    val ignitionTreeIdx: String,
    @ColumnInfo(name = "is_stack")
    val isStack: String,
    @ColumnInfo(name = "library_open_date")
    val libraryOpenDate: String,
    @ColumnInfo(name = "new_tag")
    val newTag: String,
    @ColumnInfo(name = "over_limit_status_idx")
    val overLimitStatusIdx: String,
    @ColumnInfo(name = "rare_level")
    val rareLevel: String,
    @ColumnInfo(name = "sell_price_count")
    val sellPriceCount: String,
    @ColumnInfo(name = "sell_price_type")
    val sellPriceType: String,
) {


    val iconUrl get() = viewIdx.iconUrl
}