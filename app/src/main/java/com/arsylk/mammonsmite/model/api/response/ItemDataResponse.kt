package com.arsylk.mammonsmite.model.api.response

import com.arsylk.mammonsmite.Cfg
import com.arsylk.mammonsmite.model.db.item.ItemData
import com.arsylk.mammonsmite.model.db.item.ItemDataCategory
import com.arsylk.mammonsmite.model.db.item.ItemDataType
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias ItemDataResponse = Map<String, ItemDataDto>

fun ItemDataResponse.toDatabase(): List<ItemData> {
    return values.mapNotNull { dto ->
        dto.run {
            ItemData(
                idx = idx,
                name = name,
                grade = grade,
                type = ItemDataType.fromInt(type),
                category = ItemDataCategory.fromInt(category),
                viewIdx = ViewIdx.parse(viewIdx) ?: return@mapNotNull null,
                hp = hp,
                atk = atk,
                def = def,
                agi = agi,
                cri = cri,
                addOption = addOption,
                option1GroupIdx = option1GroupIdx,
                option2GroupIdx = option2GroupIdx,
                option3GroupIdx = option3GroupIdx,
                buyArenaCoin = buyArenaCoin,
                buyDungeonCoin = buyDungeonCoin,
                buySpaCoin = buySpaCoin,
                enableLibrary = enableLibrary,
                enhancement = enhancement,
                enhancementStatusIdx = enhancementStatusIdx,
                ignitionTreeIdx = ignitionTreeIdx,
                isStack = isStack,
                libraryOpenDate = libraryOpenDate,
                newTag = newTag,
                overLimitStatusIdx = overLimitStatusIdx,
                rareLevel = rareLevel,
                sellPriceCount = sellPriceCount,
                sellPriceType = sellPriceType
            )
        }
    }
}

@Serializable
data class ItemDataDto(
    @SerialName("add_option")
    val addOption: String,
    @SerialName("agi")
    val agi: String,
    @SerialName("atk")
    val atk: String,
    @SerialName("buy_arena_coin")
    val buyArenaCoin: String,
    @SerialName("buy_dungeon_coin")
    val buyDungeonCoin: String,
    @SerialName("buy_spa_coin")
    val buySpaCoin: String,
    @SerialName("category")
    val category: Int,
    @SerialName("cri")
    val cri: String,
    @SerialName("def")
    val def: String,
    @SerialName("enable_library")
    val enableLibrary: String,
    @SerialName("enhancement")
    val enhancement: String,
    @SerialName("enhancement_status_idx")
    val enhancementStatusIdx: String,
    @SerialName("grade")
    val grade: String,
    @SerialName("hp")
    val hp: String,
    @SerialName("idx")
    val idx: String,
    @SerialName("ignition_tree_idx")
    val ignitionTreeIdx: String,
    @SerialName("is_stack")
    val isStack: String,
    @SerialName("library_open_date")
    val libraryOpenDate: String,
    @SerialName("name")
    val name: String,
    @SerialName("new_tag")
    val newTag: String,
    @SerialName("option_1_group_idx")
    val option1GroupIdx: String,
    @SerialName("option_2_group_idx")
    val option2GroupIdx: String,
    @SerialName("option_3_group_idx")
    val option3GroupIdx: String,
    @SerialName("over_limit_status_idx")
    val overLimitStatusIdx: String,
    @SerialName("rare_level")
    val rareLevel: String,
    @SerialName("sell_price_count")
    val sellPriceCount: String,
    @SerialName("sell_price_type")
    val sellPriceType: String,
    @SerialName("type")
    val type: Int,
    @SerialName("view_idx")
    val viewIdx: String
) {

    val iconUrl get() = "${Cfg.API_URL}static/icons/${viewIdx}.png"
}