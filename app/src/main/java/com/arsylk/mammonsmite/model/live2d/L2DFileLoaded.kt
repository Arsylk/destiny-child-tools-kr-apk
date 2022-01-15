package com.arsylk.mammonsmite.model.live2d

import com.arsylk.mammonsmite.model.destinychild.ViewIdx
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

    val inferredViewIdx: ViewIdx? by lazy {
        modelInfo.motionMap.values
            .firstNotNullOfOrNull { list ->
                list.firstNotNullOfOrNull { item ->
                    ViewIdx.parse(item.filename)
                }
            }
            ?: ViewIdx.parse(l2dFile.folder.name)
    }
}