package com.arsylk.mammonsmite.presentation.dialog.pck.unpack

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.lifecycle.viewModelScope
import com.arsylk.mammonsmite.domain.asResult
import com.arsylk.mammonsmite.domain.asSuccess
import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.CommonFiles
import com.arsylk.mammonsmite.domain.live2d.L2DTools
import com.arsylk.mammonsmite.domain.pck.PckTools
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.model.common.LogLineChannel
import com.arsylk.mammonsmite.model.common.OperationProgress
import com.arsylk.mammonsmite.model.common.stateIn
import com.arsylk.mammonsmite.model.live2d.L2DFile
import com.arsylk.mammonsmite.model.live2d.L2DFileLoaded
import com.arsylk.mammonsmite.model.pck.packed.PackedPckFile
import com.arsylk.mammonsmite.model.pck.packed.PackedPckEntry
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckEntry
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckHeader
import com.arsylk.mammonsmite.presentation.dialog.pck.unpack.PckUnpackDialog.*
import com.arsylk.mammonsmite.presentation.dialog.pck.unpack.PckUnpackDialog.Tab.*
import com.arsylk.mammonsmite.presentation.fragment.pck.unpacked.PckUnpackedSaveRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File

@ExperimentalFoundationApi
@ExperimentalSerializationApi
class PckUnpackViewModel(
    private val pckTools: PckTools,
    private val l2dTools: L2DTools,
    private val prefs: AppPreferences,
    private val file: File,
) : EffectViewModel<Effect>() {
    private val _packedPck = MutableStateFlow<PackedPckFile?>(null)
    private val _unpackedPck = MutableStateFlow<UnpackedPckFile?>(null)
    private val _unpackProgress = MutableStateFlow(OperationProgress.Initial)
    private val _l2dFile = MutableStateFlow<L2DFile?>(null)
    private val _loadedL2dFile = MutableStateFlow<L2DFileLoaded?>(null)
    private val _logChannel = LogLineChannel()
    private val _tab = MutableStateFlow(FILES)
    private val _actionSet = MutableStateFlow(emptySet<Action>())
    val items by lazy(::itemListFlow)
    val unpackProgress by lazy(_unpackProgress::asStateFlow)
    val tab by lazy(_tab::asStateFlow)
    val log = _logChannel.stateIn(viewModelScope)
    val actionSet by lazy(_actionSet::asStateFlow)
    val loadedL2dFile by lazy(_loadedL2dFile::asStateFlow)

    // TODO fixme
    val folder = File(CommonFiles.External.appFilesFolder, "test/${file.nameWithoutExtension}")

    init {
        withLoading {
            _actionSet.update { it + Action.OPEN_PACKED }
            val packedPckFile = pckTools
                .readPackedPckAsFlow(file, _logChannel)
                .onEach { _unpackProgress.value = it.asOperationProgress().endAt(50.0f) }
                .asSuccess()
            _packedPck.value = packedPckFile

            var unpackedPckFile = pckTools
                .unpackAsFlow(packedPckFile, folder, _logChannel)
                .onEach { _unpackProgress.value = it.asOperationProgress().startAt(50.0f) }
                .asSuccess()
            unpackedPckFile = unpackedPckFile.run {
                copy(
                    header = header.copy(
                        gameRelativePath = packedPckFile.file
                            .toRelativeString(File(prefs.destinychildFilesPath))
                    )
                )
            }
            pckTools.writeUnpackedPckFileHeader(unpackedPckFile)

            _unpackedPck.value = unpackedPckFile
            _actionSet.update { it + Action.OPEN_UNPACKED + Action.CLEAN_UP + Action.SAVE_MODEL }

            val (modelPckFile, l2dFile) = pckTools
                .unpackedPckFileToModel(unpackedPckFile, _logChannel)
                .asResult()
                .getOrNull()
                ?: return@withLoading
            _unpackedPck.value = modelPckFile
            _l2dFile.value = l2dFile

            val modelInfo = l2dTools
                .runCatching { readModelInfo(l2dFile) }
                .getOrNull()
                ?: return@withLoading
            _loadedL2dFile.value = L2DFileLoaded(l2dFile, modelInfo)
        }
    }

    fun selectTab(tab: Tab) {
        _tab.value = tab
    }

    fun onActionClick(action: Action) {
        withLoading(tag = "action") {
            when (action) {
                Action.CLEAN_UP -> {
                    cleanup()
                    setEffect(Effect.Dismiss)
                }
                Action.OPEN_PACKED ->
                    setEffect(Effect.OpenFile(file))
                Action.OPEN_UNPACKED ->
                    setEffect(Effect.OpenFile(folder))
                Action.SAVE_MODEL -> {
                    val request = prepareSaveRequest() ?: return@withLoading
                    setEffect(Effect.SaveUnpacked(request))
                }
            }
        }
    }

    private suspend fun cleanup() {
        withContext(Dispatchers.IO) {
            folder.delete()
        }
    }

    private fun prepareSaveRequest(): PckUnpackedSaveRequest? {
        val pck = _unpackedPck.value ?: return null
        val l2dFile = _l2dFile.value
        val viewIdx = _loadedL2dFile.value?.inferredViewIdx
        return PckUnpackedSaveRequest(pck, l2dFile, viewIdx)
    }

    private fun itemListFlow() =
        combineTransform(_packedPck, _unpackedPck) { packed, unpacked ->
            val list = when {
                unpacked != null -> unpacked.header.entries.map(PckHeaderItem::Unpacked)
                packed != null -> packed.header.entries.map(PckHeaderItem::Packed)
                else -> emptyList()
            }
            emit(list)
        }
}

sealed class Effect : UiEffect {
    data class OpenFile(val file: File) : Effect()
    data class SaveUnpacked(val request: PckUnpackedSaveRequest) : Effect()
    object Dismiss : Effect()
}

sealed class PckHeaderItem {
    data class Unpacked(val entry: UnpackedPckEntry) : PckHeaderItem()
    data class Packed(val entry: PackedPckEntry) : PckHeaderItem()
}