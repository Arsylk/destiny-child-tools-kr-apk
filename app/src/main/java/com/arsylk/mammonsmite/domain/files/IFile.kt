package com.arsylk.mammonsmite.domain.files

import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import com.arsylk.mammonsmite.domain.files.SafProvider.Companion.absoluteFilePath
import com.arsylk.mammonsmite.domain.files.SafProvider.Companion.asDocumentInData
import com.arsylk.mammonsmite.domain.files.SafProvider.Companion.context
import java.io.File
import java.io.InputStream
import java.io.OutputStream

sealed class IFile {
    abstract val name: String
    abstract val isFile: Boolean
    abstract val isDirectory: Boolean
    abstract val parent: IFile?
    abstract val absolutePath: String
    abstract val size: Long

    abstract fun listFiles(): List<IFile>

    abstract fun inputStream(): InputStream

    abstract fun outputStream(): OutputStream

    override fun toString(): String {
        return "${this.javaClass.simpleName}(name=$name, path=$absolutePath)"
    }

    companion object {
        val Root: IFile = NormalFile(Environment.getExternalStorageDirectory())

        fun parse(file: File): IFile {
            val doc = file.asDocumentInData()
            return if (doc != null) DocFile(doc) else NormalFile(file)
        }
    }
}

class NormalFile(val file: File) : IFile() {
    override val name: String = file.name
    override val isFile: Boolean = file.isFile
    override val isDirectory: Boolean = file.isDirectory
    override val parent: IFile? = file.parentFile?.let(::NormalFile)
    override val absolutePath: String = file.absolutePath
    override val size: Long = file.length()

    override fun listFiles(): List<IFile> {
        val list = kotlin.runCatching {
            file.listFiles()?.toList()?.filterNotNull()
        }.getOrNull().orEmpty()
        return list.map { parse(it) }
    }

    override fun inputStream(): InputStream {
        return file.inputStream()
    }

    override fun outputStream(): OutputStream {
        return file.outputStream()
    }
}

class DocFile(val doc: DocumentFile) : IFile() {
    override val name: String = doc.name ?: ""
    override val isFile: Boolean = doc.isFile
    override val isDirectory: Boolean = doc.isDirectory
    override val parent: IFile = doc.parentFile?.let(::DocFile)
        ?: SafProvider.baseFolder.let(::NormalFile)
    override val absolutePath: String = absoluteFilePath()
    override val size: Long = doc.length()

    override fun listFiles(): List<IFile> {
        return kotlin.runCatching {
            doc.listFiles().toList().map(::DocFile)
        }.getOrNull().orEmpty()
    }

    override fun inputStream(): InputStream {
        return context.contentResolver.openInputStream(doc.uri) ?: throw IllegalStateException()
    }

    override fun outputStream(): OutputStream {
        return context.contentResolver.openOutputStream(doc.uri) ?: throw IllegalStateException()
    }
}

fun IFile(file: File) = IFile.parse(file)
fun IFile(path: String) = IFile.parse(File(path))
fun IFile(parent: String?, child: String) = IFile.parse(File(parent, child))
fun IFile(parent: File?, child: String) = IFile.parse(File(parent, child))