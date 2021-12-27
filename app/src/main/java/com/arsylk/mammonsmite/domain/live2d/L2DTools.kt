package com.arsylk.mammonsmite.domain.live2d

import com.arsylk.mammonsmite.domain.decodeFromFile
import com.arsylk.mammonsmite.domain.encodeToFile
import com.arsylk.mammonsmite.model.live2d.L2DFile
import com.arsylk.mammonsmite.model.live2d.L2DHeader
import com.arsylk.mammonsmite.model.live2d.L2DModelInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

@ExperimentalSerializationApi
class L2DTools(private val json: Json) {

    @Throws(IOException::class)
    suspend fun readModelInfo(l2dFile: L2DFile): L2DModelInfo {
        return withContext(Dispatchers.IO) {
            json.decodeFromFile(l2dFile.modelInfoFile)
        }
    }

    @Throws(IOException::class, SerializationException::class)
    suspend fun readL2DFile(folder: File): L2DFile {
        return withContext(Dispatchers.IO) {
            val file = File(folder, L2DFile.MODEL_FILENAME)
            val header = json.decodeFromFile<L2DHeader>(file)
            L2DFile(folder, header)
        }
    }
}