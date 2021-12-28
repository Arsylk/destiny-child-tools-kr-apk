package com.arsylk.mammonsmite.domain.files

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import java.io.File
import kotlin.system.exitProcess

class SafProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val context = context ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isNeeded) {
            contextProvider = { context }
            val uriPermissions: List<UriPermission> = context.contentResolver.persistedUriPermissions
            if (uriPermissions.isEmpty()) {
                requiresPermission = true
            }else {
                set(uriPermissions.first().uri, context)
                requiresPermission = false
                isInitialized = true
            }
        }
        return true
    }


    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    companion object {
        // instance config
        val isNeeded = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        var isInitialized = false
        var requiresPermission = false; get() = isNeeded && field
        val baseFolder = File(Environment.getExternalStorageDirectory(), "Android")
        val dataPath = File(baseFolder, "data").absolutePath
        var safRootPath: String = ""
        var safRootUri: Uri = Uri.EMPTY
        var safDoc: DocumentFile? = null
        var contextProvider: () -> Context = { throw IllegalStateException("Saf not initialized") }

        fun set(uri: Uri, context: Context) {
            safRootPath = uri.toString()
            safRootUri = uri
            safDoc = DocumentFile.fromTreeUri(context, uri)
        }

        private fun File.isInData(): Boolean = absolutePath.startsWith(dataPath)

        private fun File.relativeToData(): String = absolutePath.replaceFirst(dataPath, "")

        fun File.asDocumentInData(): DocumentFile? {
            if (!isNeeded) return null
            if (!isInData()) return null
            val relativePath = relativeToData()
            var current = safDoc
            relativePath.split("/").filterNot(String::isBlank).forEach {
                current = current?.findFile(it)
            }

            return current
        }

        fun DocFile.absoluteFilePath(): String {
            return doc.uri.runCatching {
                val relative = pathSegments[3]
                    .replaceFirst(pathSegments[1], "")
                    .replaceFirst("/", "")
                File(dataPath, relative).absolutePath
            }.getOrNull() ?: ""
        }

        inline val DocFile.context get() = contextProvider.invoke()


        /**
         * Look up Amaze File Explorer github page
         */
        private const val STORAGE_PRIMARY = "primary"
        private const val COM_ANDROID_EXTERNALSTORAGE_DOCUMENTS = "com.android.externalstorage.documents"
        private val EXCLUDED_DIRS = arrayOf(
            File(Environment.getExternalStorageDirectory(), "Android/data").absolutePath,
            File(Environment.getExternalStorageDirectory(), "Android/obb").absolutePath
        )
        private fun remapPathForApi30OrAbove(path: String, openDocumentTree: Boolean = false): String {
            return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q && EXCLUDED_DIRS.contains(path)) {
                val suffix = path.substringAfter(Environment.getExternalStorageDirectory().absolutePath)
                val documentId = "${STORAGE_PRIMARY}:${suffix.substring(1)}"
                if (openDocumentTree) {
                    DocumentsContract.buildDocumentUri(
                        COM_ANDROID_EXTERNALSTORAGE_DOCUMENTS,
                        documentId
                    ).toString()
                } else {
                    DocumentsContract.buildTreeDocumentUri(
                        COM_ANDROID_EXTERNALSTORAGE_DOCUMENTS,
                        documentId
                    ).toString()
                }
            } else {
                path
            }
        }

        /**
         * Returns null if not required
         */
        fun permissionRequestIntent(): Intent? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isNeeded) {
                val uri = Uri.parse(remapPathForApi30OrAbove(EXCLUDED_DIRS[0], true))
                return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
                }
            }
            return null
        }

        /**
         * Restarts application on permission persisted to reinitialize
         */
        fun persistUriPermission(uri: Uri) {
            contextProvider.invoke()
                .contentResolver.takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            exitProcess(0)
        }
    }
}