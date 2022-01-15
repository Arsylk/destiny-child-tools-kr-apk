package com.arsylk.mammonsmite.domain.files

import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import com.arsylk.mammonsmite.domain.files.SafProvider.Companion.absoluteFilePath
import com.arsylk.mammonsmite.domain.files.SafProvider.Companion.asDocumentInData
import com.arsylk.mammonsmite.domain.files.SafProvider.Companion.context
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable


sealed class IFile : Serializable {
    abstract val name: String
    abstract val isFile: Boolean
    abstract val isDirectory: Boolean
    abstract val exists: Boolean
    abstract val parent: IFile?
    abstract val absolutePath: String
    abstract val size: Long

    abstract fun listFiles(): List<IFile>

    abstract fun inputStream(): InputStream

    abstract fun outputStream(): OutputStream

    abstract fun mkdir(name: String): IFile?

    override fun toString(): String {
        return "${this.javaClass.simpleName}(name=$name, path=$absolutePath)"
    }

    override fun hashCode(): Int {
        return absolutePath.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false
        val cast = other as? IFile ?: return false
        return hashCode() == cast.hashCode()
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
    override val name: String get() = file.name
    override val isFile: Boolean get() = file.isFile
    override val isDirectory: Boolean get() = file.isDirectory
    override val exists: Boolean get() = file.exists()
    override val parent: IFile? get() = file.parentFile?.let(::NormalFile)
    override val absolutePath: String get() = file.absolutePath
    override val size: Long get() = file.length()

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

    override fun mkdir(name: String): IFile {
        val new = File(file, name)
        if (!new.isDirectory && !new.exists()) new.mkdirs()
        return NormalFile(new)
    }
}

class DocFile(val doc: DocumentFile) : IFile() {
    override val name: String by lazy { doc.name ?: "" }
    override val isFile: Boolean by lazy { doc.isFile }
    override val isDirectory: Boolean by lazy { doc.isDirectory }
    override val exists: Boolean by lazy { doc.exists() }
    override val parent: IFile by lazy { doc.parentFile?.let(::DocFile)
        ?: SafProvider.baseFolder.let(::NormalFile) }
    override val absolutePath: String by lazy { absoluteFilePath() }
    override val size: Long get() = doc.length()

    override fun listFiles(): List<IFile> {
        return kotlin.runCatching {
            doc.listFiles().map(::DocFile)
        }.getOrNull().orEmpty()
    }

    override fun inputStream(): InputStream {
        return context.contentResolver.openInputStream(doc.uri) ?: throw IllegalStateException()
    }

    override fun outputStream(): OutputStream {
        return context.contentResolver.openOutputStream(doc.uri) ?: throw IllegalStateException()
    }

    override fun mkdir(name: String): IFile? {
        val new = doc.createDirectory(name)
        return new?.let(::DocFile)
    }
}

fun IFile(file: File) = IFile.parse(file)
fun IFile(path: String) = IFile.parse(File(path))
fun IFile(parent: String, child: String) = IFile.parse(File(parent, child))
fun IFile(parent: File, child: String) = IFile.parse(File(parent, child))
fun IFile(parent: IFile, child: String) = IFile.parse(File(parent.absolutePath, child))
val IFile.nameWithoutExtension: String
    get() = name.substringBeforeLast(".")