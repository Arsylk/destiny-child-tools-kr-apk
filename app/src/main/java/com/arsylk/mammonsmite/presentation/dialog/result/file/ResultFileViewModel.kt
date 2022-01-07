package com.arsylk.mammonsmite.presentation.dialog.result.file

import androidx.lifecycle.viewModelScope
import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.CommonFiles
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.model.common.UiResult
import com.arsylk.mammonsmite.model.common.uiResultOf
import com.arsylk.mammonsmite.model.file.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ResultFileViewModel(
    private val type: FileSelect,
    startIn: IFile?,
) : EffectViewModel<Effect>() {
    private val current = MutableStateFlow(startIn ?: IFile(CommonFiles.storage))
    private val _selectedItem = MutableStateFlow<ResultFileItem?>(null)
    private val updateItems = Channel<Unit>(Channel.CONFLATED)
    val title = current.prepareTitle()
    val items = current.prepareItemList()
    val selectedItem by lazy(_selectedItem::asStateFlow)


    init {
        viewModelScope.launch(Dispatchers.IO) {
            current.collectLatest { file ->
                _selectedItem.value = when (type) {
                    FileSelect.FILE -> null
                    FileSelect.FOLDER, FileSelect.ANY -> ResultFileItem(
                        label = file.name,
                        enabled = type.validate(file),
                        type = if (file.isDirectory) FileTypeFolder else FileTypeFile,
                        file = file,
                    )
                }
            }
        }
    }

    fun onItemClick(item: ResultFileItem) {
        val selectedNow =  _selectedItem.value
        if (item == selectedNow) return enqueueEffect(Effect.FileSelected(item.file))
        when (item.type) {
            is FileType.File -> _selectedItem.value = item
            is FileType.Folder -> current.value = item.file
        }
    }

    fun navigateUp() {
        withLoading(tag = "up") {
            val parent = current.value.parent
            if (parent != null && parent.listFiles().isNotEmpty()) {
                current.value = parent
            }
            else setEffect(Effect.Dismiss)
        }
    }

    fun requestAction(action: Action) {
        withLoading(tag = "action") {
            setEffect(Effect.ActionRequested(action))
        }
    }

    fun validateNewFile(name: String, type: FileType): NewFileError? {
        val items = (items.value as? UiResult.Success)?.value
            ?: return null
        return when {
            name.isBlank() -> NewFileError.InvalidName
            items.any { it.type == type && it.file.name == name } -> NewFileError.AlreadyExists
            else -> null
        }
    }

    fun createNewFile(name: String, type: FileType) {
        when (type) {
            is FileType.File -> enqueueEffect(Effect.FileSelected(IFile(current.value, name)))
            is FileType.Folder -> withLoading(tag = "create") {
                current.value.mkdir(name)
                updateItems.send(Unit)
            }
        }
    }

    private fun Flow<IFile>.prepareTitle() =
        mapLatest { it.absolutePath }
        .map { it.replaceFirst(CommonFiles.storage.absolutePath, "") }
        .map { if (it.isEmpty()) "Storage" else it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    private fun Flow<IFile>.prepareItemList() =
        flatMapLatest { folder ->
            updateItems.receiveAsFlow()
                .onStart { emit(Unit) }
                .flatMapLatest {
                    uiResultOf {
                        folder.listFiles().sortedWith(comparator)
                            .map { file ->
                                ResultFileItem(
                                    label = file.name,
                                    enabled = type != FileSelect.FOLDER || type.validate(file),
                                    type = if (file.isDirectory) FileTypeFolder else FileTypeFile,
                                    file = file,
                                )
                            }
                    }
                }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UiResult.Loading())

    companion object {
        private val comparator = Comparator<IFile> { f1, f2 ->
            when {
                f1.isFile && f2.isFile -> f1.name.compareTo(f2.name)
                f1.isDirectory && f2.isDirectory -> f1.name.compareTo(f2.name)
                else -> f1.isFile.compareTo(f2.isFile)
            }
        }
    }
}

sealed class Effect : UiEffect {
    data class FileSelected(val file: IFile) : Effect()
    object Dismiss : Effect()
    data class ActionRequested(val action: Action) : Effect()
}