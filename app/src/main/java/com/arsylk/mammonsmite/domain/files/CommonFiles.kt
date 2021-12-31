package com.arsylk.mammonsmite.domain.files

import android.os.Build
import android.os.Environment
import com.arsylk.mammonsmite.BuildConfig
import com.arsylk.mammonsmite.domain.koin.KoinAndroidContext
import java.io.File

object CommonFiles {
    val storage by lazy(Environment::getExternalStorageDirectory)
    val data by lazy(Environment::getDataDirectory)

    val appDataFolder by lazy { KoinAndroidContext.filesDir }
    val cache by lazy { File(KoinAndroidContext.cacheDir, "cache") }

    object External {
        val appFilesFolder by lazy { File(storage, "DCUnpacker") }
        val appUnpackedFolder by lazy { File(appFilesFolder, "Unpacked") }
    }

    object Internal {
        val englishPatchFile by lazy { File(appDataFolder, "english_patch.json")}
    }
}