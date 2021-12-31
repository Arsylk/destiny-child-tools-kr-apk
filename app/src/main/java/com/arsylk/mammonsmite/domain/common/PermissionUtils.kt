package com.arsylk.mammonsmite.domain.common

import android.Manifest.permission.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import androidx.core.content.ContextCompat
import com.arsylk.mammonsmite.BuildConfig
import com.arsylk.mammonsmite.domain.isAndroid11

object PermissionUtils {
    val base = arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
    val manage = if (isAndroid11) arrayOf(MANAGE_EXTERNAL_STORAGE) else emptyArray()


    fun hasBasePermissions(context: Context) =
        base.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

    fun hasManagePermission() =
        if (isAndroid11) Environment.isExternalStorageManager() else true

    fun managePermissionIntent(): Intent? {
        return if (isAndroid11) {
            try {
                val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
            } catch (_: Throwable){
                Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            }
        } else null
    }
}