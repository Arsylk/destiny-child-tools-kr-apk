package com.arsylk.mammonsmite.model.db.item

enum class ItemDataType(val int: Int) {
    Unknown(-1),
    Etc(0),
    Normal(1),
    SoulCarta(2),
    ItemSellable(3),
    IgnitionMaterial(4),
    IgnitionCore(5),
    RecipeMaterial(6),
    ItemOptionAttachCountReset(11),
    ItemEnhancementReinforce(12),
    ItemOptionAttachMaterial(13),
    ItemOptionAttach(14),
    Spa(21),
    Puppet(101);

    companion object {

        fun fromInt(int: Int) = values().firstOrNull { it.int == int } ?: Unknown
    }
}