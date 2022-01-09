package com.arsylk.mammonsmite.presentation.screen.pck.swap

import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import com.arsylk.mammonsmite.model.live2d.L2DFile
import com.arsylk.mammonsmite.model.live2d.L2DFileLoaded
import com.arsylk.mammonsmite.model.live2d.L2DModelInfo
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckEntry
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile

data class PckSwapItem(
    val pck: UnpackedPckFile,
    val loaded: L2DFileLoaded,
    val viewIdx: ViewIdx,
) {

    val l2d: L2DFile get() = loaded.l2dFile
    val info: L2DModelInfo get() = loaded.modelInfo

    val name: String get() = pck.header.name

    @Throws(IndexOutOfBoundsException::class)
    fun entryForFilename(filename: String): UnpackedPckEntry {
        return pck.header.entries.first {
            it.filename == filename
        }
    }

    fun entryOrNullForFilename(filename: String): UnpackedPckEntry? {
        return pck.header.entries.firstOrNull {
            it.filename == filename
        }
    }
}