package com.arsylk.mammonsmite.presentation.fragment.pck.unpacked

import androidx.annotation.Keep
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import com.arsylk.mammonsmite.model.live2d.L2DFile
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import java.io.Serializable

@Keep
data class PckUnpackedSaveRequest(
    val unpackedPck: UnpackedPckFile,
    val l2dFile: L2DFile? = null,
    val inferredViewIdx: ViewIdx? = null,
): Serializable