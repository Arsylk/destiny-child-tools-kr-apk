package com.arsylk.mammonsmite.model.live2d

import java.io.File

data class L2DFileLoaded(
    val l2dFile: L2DFile,
    val modelInfo: L2DModelInfo
) {
    val characterFile = File(l2dFile.folder, modelInfo.model)
    val textureFiles = modelInfo.textures.map { File(l2dFile.folder, it) }
    val motionIdleFile = modelInfo.motionMap["idle"]
        ?.firstOrNull()?.let { File(l2dFile.folder, it.filename) }
    val motionAttackFile = modelInfo.motionMap["attack"]
        ?.firstOrNull()?.let { File(l2dFile.folder, it.filename) }
}