package com.arsylk.mammonsmite.model.pck

import com.arsylk.mammonsmite.model.live2d.L2DFile
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import java.io.File

data class UnpackedPckLive2D(
    val pck: UnpackedPckFile,
    val l2d: L2DFile,
) {

    val key: String = pck.folder.name
    val preview: File = l2d.previewFile
    val name = pck.header.name
    val viewIdx = l2d.header.viewIdx
}