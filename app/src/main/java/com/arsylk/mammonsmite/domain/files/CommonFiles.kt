package com.arsylk.mammonsmite.domain.files

import android.os.Environment
import com.arsylk.mammonsmite.domain.koin.KoinAndroidContext
import java.io.File

object CommonFiles {
    val storage by lazy(Environment::getExternalStorageDirectory)
    val data by lazy(Environment::getDataDirectory)

    val appDataFolder by lazy { KoinAndroidContext.filesDir }
    val cache by lazy {
        File(KoinAndroidContext.cacheDir, "cache")
            .apply { runCatching { if (!exists()) mkdirs() } }
    }

    object External {
        val appFilesFolder by lazy {
            File(storage, "DCUnpacker")
                .apply { runCatching { if (!exists()) mkdirs() } }
        }
        val appUnpackedFolder by lazy {
            File(appFilesFolder, "Unpacked")
                .apply { runCatching { if (!exists()) mkdirs() } }
        }
        val appWorkspaceFolder by lazy {
            File(appFilesFolder, "Workspace")
                .apply { runCatching { if (!exists()) mkdirs() } }
        }
    }

    object Internal {
        val englishPatchFile by lazy { File(appDataFolder, "english_patch.json")}
    }
}