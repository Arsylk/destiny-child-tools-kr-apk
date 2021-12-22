package com.arsylk.mammonsmite.model.pck

enum class PckEntryFileType(val byteInt: Int?, val extension: String) {
    UNKNOWN(null, "unk"),
    DAT(109, "dat"),
    MTN(35, "mtn"),
    PNG(137, "png"),
    JSON(123, "json"),
    LOCALE_DEF(null, "def"),
    LOCALE_TAB(null, "tab");

    companion object {

        fun fromByte(byte: Byte?): PckEntryFileType {
            byte ?: return UNKNOWN
            val i = byte.toInt() and 0xFF
            return values().firstOrNull { it.byteInt == i } ?: UNKNOWN
        }
    }
}