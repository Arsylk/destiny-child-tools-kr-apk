package com.arsylk.mammonsmite.domain.live2d

import com.arsylk.mammonsmite.domain.decodeFromFile
import com.arsylk.mammonsmite.domain.encodeToFile
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import okio.use
import java.io.IOException

@ExperimentalSerializationApi
class L2DPositionTools(private val json: Json, private val prefs: AppPreferences) {
    private val mutex = Mutex()

    @Throws(IOException::class, SerializationException::class)
    private suspend fun readGamePositions(): JsonObject {
        val file = IFile(prefs.destinychildModelInfoPath)
        return withContext(Dispatchers.IO) {
            file.inputStream().use {
                json.decodeFromStream(it)
            }
        }
    }

    @Throws(IOException::class, SerializationException::class)
    private suspend fun writeGamePositions(all: JsonObject) {
        val file = IFile(prefs.destinychildModelInfoPath)
        withContext(Dispatchers.IO) {
            file.outputStream().use {
                json.encodeToStream(all, it)
            }
        }
    }

    @Throws(IOException::class, SerializationException::class)
    suspend fun readGamePositions(idx: ViewIdx): JsonObject? {
        val file = IFile(prefs.destinychildModelInfoPath)
        val json = withContext(Dispatchers.IO) {
            file.inputStream().use {
                json.decodeFromStream<JsonObject>(it)
            }
        }
        return json[idx.string] as? JsonObject
    }

    @Throws(IOException::class, SerializationException::class)
    suspend fun updateGamePositions(idx: ViewIdx, pos: JsonObject) {
        mutex.withLock {
            val current = readGamePositions()
            val map = current.toMutableMap()
            map[idx.string] = pos
            val new = JsonObject(map)
            writeGamePositions(new)
        }
    }

    @Throws(IOException::class)
    suspend fun writePositions(pck: UnpackedPckFile, pos: JsonObject) {
        withContext(Dispatchers.IO) {
            json.encodeToFile(pos, pck.positionsFile)
        }
    }


    @Throws(IOException::class, SerializationException::class)
    suspend fun readPositions(pck: UnpackedPckFile): JsonObject {
        return withContext(Dispatchers.IO) {
            json.decodeFromFile(pck.positionsFile)
        }
    }
}