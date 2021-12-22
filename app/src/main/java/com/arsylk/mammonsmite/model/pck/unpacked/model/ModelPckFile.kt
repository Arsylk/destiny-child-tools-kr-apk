package com.arsylk.mammonsmite.model.pck.unpacked.model

import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckHeader
import java.io.File

class ModelPckFile(
    folder: File,
    header: UnpackedPckHeader,
) : UnpackedPckFile(folder, header) {

}