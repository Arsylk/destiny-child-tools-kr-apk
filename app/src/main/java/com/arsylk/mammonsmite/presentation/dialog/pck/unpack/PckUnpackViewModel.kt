package com.arsylk.mammonsmite.presentation.dialog.pck.unpack

import com.arsylk.mammonsmite.domain.asSuccess
import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.CommonFiles
import com.arsylk.mammonsmite.domain.pck.PckTools
import com.arsylk.mammonsmite.model.common.OperationProgress
import com.arsylk.mammonsmite.model.pck.packed.PackedPckFile
import com.arsylk.mammonsmite.model.pck.packed.PackedPckEntry
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckEntry
import com.arsylk.mammonsmite.presentation.dialog.pck.unpack.PckUnpackDialog.Tab.*
import kotlinx.coroutines.flow.*
import java.io.File

class PckUnpackViewModel(
    private val pckTools: PckTools,
    private val file: File,
) : EffectViewModel<Effect>() {
    private val _packedPck = MutableStateFlow<PackedPckFile?>(null)
    private val _unpackedPck = MutableStateFlow<UnpackedPckFile?>(null)
    private val _unpackProgress = MutableStateFlow(OperationProgress.Initial)
    private val _tab = MutableStateFlow(PACKED)
    val items = _tab.toItemList()
    val unpackProgress by lazy(_unpackProgress::asStateFlow)
    val tab by lazy(_tab::asStateFlow)

    // TODO fixme
    val folder = File(CommonFiles.appFilesFolder, "test/${file.nameWithoutExtension}")

    init {
        withLoading {

            val packedPckFile = pckTools
                .readPackedPckAsFlow(file)
                .onEach { _unpackProgress.value = it.asOperationProgress().endAt(50.0f) }
                .asSuccess()
            _packedPck.value = packedPckFile
            val unpackedPckFile = pckTools
                .unpackAsFlow(packedPckFile, folder)
                .onEach { _unpackProgress.value = it.asOperationProgress().startAt(50.0f) }
                .asSuccess()
            _unpackedPck.value = unpackedPckFile

            pckTools.unpackedPckFileToModel(unpackedPckFile)
                .catch { it.printStackTrace() }
                .collect { info ->
                    println(info)
                    _unpackedPck.value = info
                }
        }
    }

    fun selectTab(tab: PckUnpackDialog.Tab) {
        _tab.value = tab
    }


    private fun Flow<PckUnpackDialog.Tab>.toItemList() =
        flatMapLatest { tab ->
            when (tab) {
                PACKED -> _packedPck.mapNotNull {
                    it?.header?.entries?.map(PckHeaderItem::Packed)
                }
                UNPACKED -> _unpackedPck.mapNotNull {
                    it?.header?.entries?.map(PckHeaderItem::Unpacked)
                }
            }
        }
}

sealed class Effect : UiEffect

sealed class PckHeaderItem {
    data class Unpacked(val entry: UnpackedPckEntry) : PckHeaderItem()
    data class Packed(val entry: PackedPckEntry) : PckHeaderItem()
}