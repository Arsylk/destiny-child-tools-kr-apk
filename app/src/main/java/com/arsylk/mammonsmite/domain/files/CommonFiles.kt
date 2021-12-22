package com.arsylk.mammonsmite.domain.files

import android.os.Build
import android.os.Environment
import com.arsylk.mammonsmite.BuildConfig
import java.io.File

object CommonFiles {
    val storage by lazy(Environment::getExternalStorageDirectory)
    val data by lazy(Environment::getDataDirectory)

    val appDataFolder
        by lazy {
            if (Build.VERSION.SDK_INT <= 29) File(storage, "/Android/data/${BuildConfig.APPLICATION_ID}/files")
            else File(data, "/data/${BuildConfig.APPLICATION_ID}/files")
        }
    val appFilesFolder by lazy { File(storage, "DCUnpacker") }
}