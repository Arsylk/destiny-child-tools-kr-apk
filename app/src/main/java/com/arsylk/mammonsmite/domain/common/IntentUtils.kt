package com.arsylk.mammonsmite.domain.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File

object IntentUtils {

    fun openFile(context: Context?, file: File) {
        context?.runCatching {
            val path = if (file.isFile) file.parentFile.absolutePath else file.absolutePath
            val uri = Uri.parse(path)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "resource/folder")
            startActivity(Intent.createChooser(intent, "Open Folder"))
        }
    }
}