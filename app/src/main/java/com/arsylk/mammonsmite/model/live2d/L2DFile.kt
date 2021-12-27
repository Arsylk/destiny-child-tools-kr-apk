package com.arsylk.mammonsmite.model.live2d

import java.io.File
import java.io.Serializable

data class L2DFile(
    val folder: File,
    val header: L2DHeader,
) : Serializable {
    val modelFile = File(folder, MODEL_FILENAME)
    val modelInfoFile = File(folder, header.modelInfoFilename)
    val previewFile = File(folder, PREVIEW_FILENAME)

    companion object {
        const val MODEL_FILENAME = "_model"
        const val PREVIEW_FILENAME = "_preview.png"
    }
}