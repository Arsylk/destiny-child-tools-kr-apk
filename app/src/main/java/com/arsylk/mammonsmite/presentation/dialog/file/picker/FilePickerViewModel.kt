package com.arsylk.mammonsmite.presentation.dialog.file.picker

import androidx.lifecycle.viewModelScope
import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.CommonFiles
import com.arsylk.mammonsmite.domain.safeListFiles
import com.arsylk.mammonsmite.model.common.FileTypeOld
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.io.File


class FilePickerViewModel(private val type: FileTypeOld) : EffectViewModel<Effect>() {
    private var _directory = MutableStateFlow(CommonFiles.storage)
    val _selectedItem = MutableStateFlow<FilePickerItem?>(null)
    val directory by lazy(_directory::asStateFlow)
    val items = _directory.toFilePickerItems()
    val selectedItem by lazy(_selectedItem::asStateFlow)

    fun prepareItems(file: File) {
        if (!file.exists()) return
        val folder = if (file.isDirectory) file else file.parentFile
        _selectedItem.value = null
        _directory.value = folder
    }

    fun selectCurrent() {
        viewModelScope.launch {
            val file = when (type) {
                FileTypeOld.FILE -> _selectedItem.value?.file ?: return@launch
                FileTypeOld.FOLDER -> _directory.value
                FileTypeOld.ANY -> _selectedItem.value?.file ?: _directory.value
            }
            setEffect(Effect.FileSelected(file))
            setEffect(Effect.Dismiss)
        }
    }

    fun selectItem(item: FilePickerItem) {
        val current = _selectedItem.value
        if (current != item) _selectedItem.value = item
        else selectCurrent()
    }

    private fun Flow<File>.toFilePickerItems() =
        mapLatest { dir ->
            val files = dir
                .safeListFiles { it.exists() && it.canRead() }
                .sortedWith(comparator)
                .map { FilePickerItem(file = it, label = it.name) }
            val parentItem = dir.parentFile?.takeIf { it.exists() && it.canRead() }
                ?.let { FilePickerItem(file = it, label = "..") }

            listOfNotNull(parentItem, *files.toTypedArray())
        }

    companion object {
        private val comparator = Comparator<File> { f1, f2 ->
            when {
                f1.isFile && f2.isFile -> f1.name.compareTo(f2.name)
                f1.isDirectory && f2.isDirectory -> f1.name.compareTo(f2.name)
                else -> f1.isFile.compareTo(f2.isFile)
            }
        }
    }
}

sealed class Effect : UiEffect {
    data class FileSelected(val file: File) : Effect()
    object Dismiss : Effect()
}