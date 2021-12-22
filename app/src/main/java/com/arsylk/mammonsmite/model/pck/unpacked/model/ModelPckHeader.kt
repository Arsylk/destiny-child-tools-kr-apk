package com.arsylk.mammonsmite.model.pck.unpacked.model

import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckHeader

class ModelPckHeader(
    override val entries: List<ModelPckEntry>
) : UnpackedPckHeader(entries) {
}