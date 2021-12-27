package com.arsylk.mammonsmite.presentation.fragment.pck.unpacked

import java.io.Serializable


data class PckUnpackedSaveInput(
    val folderName: String,
    val name: String,
    val isL2d: Boolean,
    val viewIdxText: String,
    val gameRelativePath: String,
): Serializable