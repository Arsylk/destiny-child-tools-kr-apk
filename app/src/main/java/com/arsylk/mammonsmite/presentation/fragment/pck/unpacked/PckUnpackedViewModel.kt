package com.arsylk.mammonsmite.presentation.fragment.pck.unpacked

import androidx.lifecycle.viewModelScope
import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.CommonFiles
import com.arsylk.mammonsmite.domain.live2d.L2DTools
import com.arsylk.mammonsmite.domain.pck.PckTools
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.domain.repo.CharacterRepository
import com.arsylk.mammonsmite.domain.base.FileListFlow
import com.arsylk.mammonsmite.model.common.InputField
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import com.arsylk.mammonsmite.model.live2d.L2DFile
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import com.arsylk.mammonsmite.presentation.dialog.pck.unpacked.PckUnpackedConfigState
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

    private val foldersFlow =
        FileListFlow(viewModelScope, folder) { it.isDirectory && it.exists() && it.canRead() }
    val live2dItems = foldersFlow.toLive2DItems()


    fun prepareSaveState(request: PckUnpackedSaveRequest): PckUnpackedConfigState {
        val folder = File(folder, request.unpackedPck.folder.name)
        val viewIdx = saveRequest?.inferredViewIdx
        val name = repo.viewIdxNames.value[viewIdx]

        return PckUnpackedConfigState(
            type = PckUnpackedConfigState.Type.SAVE,
            name = InputField(
                name ?: folder.name,
                { it.isNotBlank() },
                {
                    when {
                        it.isBlank() -> "Name can't be blank"
                        else -> null
                    }
                }
            ),
            folder = InputField(
                folder.name,
                { !File(folder, it).exists() },
                {
                    when {
                        File(folder, it).exists() -> "Folder already exists"
                        it.isBlank() -> "Folder can't be blank"
                        else -> null
                    }
                }
            ),
            isL2d = request.l2dFile != null,
            viewIdx = InputField(
                viewIdx?.string ?: "",
                { ViewIdx.parse(it) != null },
                {
                    when {
                        it.isBlank() -> null
                        ViewIdx.parse(it) == null -> "Invalid View Idx"
                        else -> null
                    }
                }
            ),
            gameRelativePath = InputField(
                request.unpackedPck.header.gameRelativePath
            )
        )
    }

    fun prepareLive2DConfigState(item: UnpackedLive2DItem): PckUnpackedConfigState {
        return PckUnpackedConfigState(
            type = PckUnpackedConfigState.Type.CONFIG,
            name = InputField(
                item.pck.header.name,
                { it.isNotBlank() },
                {
                    when {
                        it.isBlank() -> "Name can't be blank"
                        else -> null
                    }
                }
            ),
            folder = InputField(item.pck.folder.name),
            isL2d = true,
            viewIdx = InputField(
                item.l2dFile.header.viewIdx?.string ?: "",
                { ViewIdx.parse(it) != null },
                {
                    when {
                        it.isBlank() -> null
                        ViewIdx.parse(it) == null -> "Invalid View Idx"
                        else -> null
                    }
                }
            ),
            gameRelativePath = InputField(
                item.pck.header.gameRelativePath,
            )
        )
    }

    fun saveUnpackedPckConfig(
        request: PckUnpackedSaveRequest,
        state: PckUnpackedConfigState
    ) {
        withLoading {
            var (pck, l2d) = request

            val dst = File(folder, state.folder.value)
            pck = pck.copy(
                header = pck.header
                    .copy(
                        name = state.name.value,
                        gameRelativePath = state.gameRelativePath.value,
                    )
            )
            pck = pckTools.saveUnpackedPckFile(pck, dst)
            pckTools.writeUnpackedPckFileHeader(pck)

            l2d = l2d?.run {
                copy(
                    folder = dst,
                    header = header.copy(
                        viewIdx = ViewIdx.parse(state.viewIdx.value),
                    )
                )
            }
            l2d?.also { pckTools.writeL2DFileHeader(it) }
            foldersFlow.update()
        }
    }

    fun saveUnpackedPckConfig(
        pck: UnpackedPckFile,
        l2d: L2DFile?,
        state: PckUnpackedConfigState,
    ) {
        withLoading {
            pckTools.writeUnpackedPckFileHeader(
                pck.run {
                    copy(
                        header = header
                            .copy(
                                name = state.name.value,
                                gameRelativePath = state.gameRelativePath.value,
                            )
                    )
                }
            )
            if (l2d != null) {
                pckTools.writeL2DFileHeader(
                    l2d.run {
                        copy(
                            header = header.copy(
                                viewIdx = ViewIdx.parse(state.viewIdx.value),
                            )
                        )
                    }
                )
            }
            foldersFlow.update()
        }
    }


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