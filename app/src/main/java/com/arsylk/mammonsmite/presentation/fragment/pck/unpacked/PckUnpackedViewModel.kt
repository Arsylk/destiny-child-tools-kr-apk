package com.arsylk.mammonsmite.presentation.fragment.pck.unpacked

import androidx.lifecycle.viewModelScope
import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.CommonFiles
import com.arsylk.mammonsmite.domain.live2d.L2DTools
import com.arsylk.mammonsmite.domain.pck.PckTools
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.domain.repo.CharacterRepository
import com.arsylk.mammonsmite.model.common.FileListFlow
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import com.arsylk.mammonsmite.model.live2d.L2DFile
import com.arsylk.mammonsmite.presentation.fragment.pck.unpacked.items.UnpackedLive2DItem
import kotlinx.coroutines.flow.*
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File

@ExperimentalSerializationApi
class PckUnpackedViewModel(
    private val pckTools: PckTools,
    private val l2dTools: L2DTools,
    private val repo: CharacterRepository,
    private val prefs: AppPreferences,
    private val saveRequest: PckUnpackedSaveRequest? = null,
) : EffectViewModel<Effect>() {
    private val folder = CommonFiles.External.appUnpackedFolder
    private val _saveInput = MutableStateFlow<PckUnpackedSaveInput?>(null)
    val saveInput by lazy(_saveInput::asStateFlow)
    val saveInputFolderExists = _saveInput.toFolderExists()

    private val foldersFlow = FileListFlow(viewModelScope, folder) { it.isDirectory && it.exists() && it.canRead() }
    val live2dItems = foldersFlow.toLive2DItems()


    init {
        // show save unpacked pck dialog if requested
        saveRequest?.also(::prepareSaveInput)
    }

    fun updateSaveInput(block: PckUnpackedSaveInput.() -> PckUnpackedSaveInput) {
        _saveInput.update { if (it != null) block(it) else null }
    }

    fun saveUnpacked() {
        withLoading {
            val input = _saveInput.value ?: return@withLoading
            var (pck, l2d) = saveRequest ?: return@withLoading

            val dst = File(folder, input.folderName)
            pck = pck.copy(
                header = pck.header
                    .copy(
                        name = input.name,
                        gameRelativePath = input.gameRelativePath,
                    )
            )
            pck = pckTools.saveUnpackedPckFile(pck, dst)
            pckTools.writeUnpackedPckFileHeader(pck)

            l2d = l2d?.run {
                copy(
                    folder = dst,
                    header = header.copy(
                        viewIdx = ViewIdx.parse(input.viewIdxText),
                    )
                )
            }
            l2d?.also { pckTools.writeL2DFileHeader(it) }
            foldersFlow.update()
        }
    }

    private fun prepareSaveInput(request: PckUnpackedSaveRequest) {
        val folder = File(folder, request.unpackedPck.folder.name)
        val viewIdx = saveRequest?.inferredViewIdx
        val name = repo.viewIdxNames.value[viewIdx]
        _saveInput.value =  PckUnpackedSaveInput(
            folderName = folder.name,
            name = name ?: folder.name,
            isL2d = request.l2dFile != null,
            viewIdxText = viewIdx?.string ?: "",
            gameRelativePath = request.unpackedPck.header.gameRelativePath
        )
    }

    private fun Flow<PckUnpackedSaveInput?>.toFolderExists() =
        mapLatest {
            if (it != null) File(folder, it.folderName).run { isDirectory && exists() } else false
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    private fun FileListFlow.toLive2DItems(): StateFlow<List<UnpackedLive2DItem>> {
        return shared
            .flatMapLatest { folders ->
                val gameFolder = File(prefs.destinychildFilesPath)
                val list = mutableListOf<UnpackedLive2DItem>()
                folders.asFlow()
                    .filter { File(it, L2DFile.MODEL_FILENAME).exists() }
                    .flatMapMerge(5) {
                        flow {
                            val pck = pckTools.readUnpackedPckFile(it)
                            val l2d = l2dTools.readL2DFile(it)
                            val isInGame = File(gameFolder, pck.header.gameRelativePath)
                                .run { exists() && isFile }
                            emit(UnpackedLive2DItem(pck, l2d, isInGame, false))
                        }
                            .catch {}
                    }
                    .map { item ->
                        list += item
                        list
                    }
                    .mapLatest { list.sortedBy { it.name }.toList() }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    }
}

sealed class Effect : UiEffect