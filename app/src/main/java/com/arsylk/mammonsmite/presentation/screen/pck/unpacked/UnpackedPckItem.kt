package com.arsylk.mammonsmite.presentation.screen.pck.unpacked

import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile

data class UnpackedPckItem(
    val pck: UnpackedPckFile
) {
    val name = pck.header.name
}