package com.arsylk.mammonsmite.model.pck.packed

import java.io.File

data class PackedPckFile(
    val file: File,
    val header: PackedPckHeader,
)