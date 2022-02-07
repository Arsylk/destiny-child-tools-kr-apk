package com.arsylk.mammonsmite.presentation.screen.wiki.items

import androidx.annotation.DrawableRes
import com.arsylk.mammonsmite.R
import com.arsylk.mammonsmite.model.db.item.ItemDataCategory
import com.arsylk.mammonsmite.model.db.item.ItemDataCategory.*

enum class WikiItemsTab(@DrawableRes val iconRes: Int, val categories: Set<ItemDataCategory>) {
    All(R.drawable.ic_item_filter_all, ItemDataCategory.values().toSet()),
    Puppet(
        R.drawable.ic_item_filter_puppet,
        setOf(
            ItemDataCategory.Puppet,
            PuppetMaterial,
            PuppetExpMaterial,
        )
    ),
    Equipment(
        R.drawable.ic_item_filter_equipment,
        setOf(Weapon, Armor, Accessory, SoulCarta),
    ),
    Etc(
        R.drawable.ic_item_filter_etc,
        setOf(
            Unknown,
            ItemDataCategory.Etc,
            Material,
            Money,
            WebEvent,
            CharacterDungeonItem,
            RecipeMaterial,
        )
    ),
    Spa(
        R.drawable.ic_item_filter_spa,
        setOf(SpaTowel, SpaMeltItem, SpaLantern, SpaPresent, SpaObject, SpaSticker)
    ),
    Ignition(
        R.drawable.ic_item_filter_ignition,
        setOf(
            IgnitionMaterialAmpAtk,
            IgnitionMaterialAmpDef,
            IgnitionMaterialAmpAgi,
            IgnitionMaterialAmpCri,
            IgnitionCoreAmpAtk,
            IgnitionCoreAmpDef,
            IgnitionCoreAmpAgi,
            IgnitionCoreAmpCri
        )
    ),
    Summon(
        R.drawable.ic_item_filter_summon,
        setOf(),
    ),
    Skin(
        R.drawable.ic_item_filter_spa_skin,
        setOf(CharacterDungeonItem),
    ),
}