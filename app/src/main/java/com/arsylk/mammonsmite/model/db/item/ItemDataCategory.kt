package com.arsylk.mammonsmite.model.db.item

enum class ItemDataCategory(val int: Int) {
    Unknown(-1),
    Etc(0),
    Weapon(1),
    Armor(2),
    Accessory(3),
    Material(4),
    Money(5),
    WebEvent(6),
    CharacterDungeonItem(7),
    SoulCarta(8),
    ExpBase(10),
    ExpWeapon(11),
    ExpArmor(12),
    ExpAccessory(13),
    ExpSoulCarta(18),
    SpaTowel(21),
    SpaMeltItem(22),
    SpaLantern(23),
    SpaPresent(24),
    SpaObject(25),
    SpaSticker(26),
    IgnitionMaterialAmpAtk(42),
    IgnitionMaterialAmpDef(43),
    IgnitionMaterialAmpAgi(44),
    IgnitionMaterialAmpCri(45),
    IgnitionCoreAmpAtk(52),
    IgnitionCoreAmpDef(53),
    IgnitionCoreAmpAgi(54),
    IgnitionCoreAmpCri(55),
    RecipeMaterial(61),
    Puppet(101001),
    PuppetMaterial(101011),
    PuppetExpMaterial(101021);

    companion object {

        fun fromInt(int: Int) = values().firstOrNull { it.int == int } ?: Unknown
    }
}