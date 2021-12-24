package com.arsylk.mammonsmite.presentation.dialog.pck.unpack

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.lifecycle.viewModelScope
import com.arsylk.mammonsmite.domain.asResult
import com.arsylk.mammonsmite.domain.asSuccess
import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.CommonFiles
import com.arsylk.mammonsmite.domain.pck.PckTools
import com.arsylk.mammonsmite.model.common.LogLineChannel
import com.arsylk.mammonsmite.model.common.OperationProgress
import com.arsylk.mammonsmite.model.common.stateIn
import com.arsylk.mammonsmite.model.live2d.L2DFile
import com.arsylk.mammonsmite.model.pck.packed.PackedPckFile
import com.arsylk.mammonsmite.model.pck.packed.PackedPckEntry
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckEntry
import com.arsylk.mammonsmite.presentation.dialog.pck.unpack.PckUnpackDialog.*
import com.arsylk.mammonsmite.presentation.dialog.pck.unpack.PckUnpackDialog.Tab.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File

@ExperimentalFoundationApi
@ExperimentalSerializationApi
class PckUnpackViewModel(
    private val pckTools: PckTools,
    private val file: File,
) : EffectViewModel<Effect>() {
    private val _packedPck = MutableStateFlow<PackedPckFile?>(null)
    private val _unpackedPck = MutableStateFlow<UnpackedPckFile?>(null)
    private val _unpackProgress = MutableStateFlow(OperationProgress.Initial)
    private val _l2dFile = MutableStateFlow<L2DFile?>(null)
    private val _logChannel = LogLineChannel()
    private val _tab = MutableStateFlow(FILES)
    private val _actionSet = MutableStateFlow(emptySet<Action>())
    val items by lazy(::itemListFlow)
    val unpackProgress by lazy(_unpackProgress::asStateFlow)
    val tab by lazy(_tab::asStateFlow)
    val log = _logChannel.stateIn(viewModelScope)
    val actionSet by lazy(_actionSet::asStateFlow)

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

            val unpackedPckFile = pckTools
                .unpackAsFlow(packedPckFile, folder, _logChannel)
                .onEach { _unpackProgress.value = it.asOperationProgress().startAt(50.0f) }
                .asSuccess()
            _unpackedPck.value = unpackedPckFile
            _actionSet.update { it + Action.OPEN_UNPACKED + Action.CLEAN_UP }

            val (modelPckFile, l2dFile) = pckTools
                .unpackedPckFileToModel(unpackedPckFile, _logChannel)
                .asResult()
                .getOrNull()
                ?: return@withLoading
            _unpackedPck.value = modelPckFile
            _l2dFile.value = l2dFile
            _actionSet.update { it + Action.PREVIEW_MODEL + Action.SAVE_MODEL }
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
                Action.OPEN_PACKED -> setEffect(Effect.OpenFile(file))
                Action.OPEN_UNPACKED -> setEffect(Effect.OpenFile(folder))
                Action.PREVIEW_MODEL ->
                    setEffect(Effect.PreviewModel(_l2dFile.value ?: return@withLoading))
                Action.SAVE_MODEL ->
                    setEffect(Effect.SaveUnpacked(_unpackedPck.value ?: return@withLoading))
            }
        }
    }

    private suspend fun cleanup() {
        withContext(Dispatchers.IO) {
            folder.delete()
        }
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
    data class SaveUnpacked(val pck: UnpackedPckFile) : Effect()
    data class PreviewModel(val l2dFile: L2DFile) : Effect()
    object Dismiss : Effect()
}

sealed class PckHeaderItem {
    data class Unpacked(val entry: UnpackedPckEntry) : PckHeaderItem()
    data class Packed(val entry: PackedPckEntry) : PckHeaderItem()
}