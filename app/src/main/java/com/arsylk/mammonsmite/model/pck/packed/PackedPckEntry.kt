package com.arsylk.mammonsmite.model.pck.packed

import com.arsylk.mammonsmite.utils.Utils

data class PackedPckEntry(
    val index: Int,
    val hash: ByteArray,
    val flag: Int,
    val offset: Int,
    val sizeOriginal: Int,
    val sizeCompressed: Int,
) {
    val hashString: String = Utils.bytesToHex(hash).lowercase()
}