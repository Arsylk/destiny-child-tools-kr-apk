package com.arsylk.mammonsmite.presentation.dialog.pck.pack

import androidx.lifecycle.viewModelScope
import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.orUnknown
import com.arsylk.mammonsmite.domain.pck.PckTools
import com.arsylk.mammonsmite.model.common.LogLineChannel
import com.arsylk.mammonsmite.model.common.asUiResultFlow
import com.arsylk.mammonsmite.model.common.uiResultOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import java.io.File

class PckPackViewModel(
    private val pckTools: PckTools,
    private val file: File,
) : EffectViewModel<Effect>() {
    val title = flow { emit(file.parentFile?.nameWithoutExtension.orUnknown()) }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, "")
    val uiResult = channelFlow {
        val pck = pckTools.readUnpackedPck(file.parentFile!!)

        val log = LogLineChannel()
        val dst = File(pck.folder, "${pck.folder.nameWithoutExtension}.pck")

        send(log.stateIn(this))
        pckTools.packAsFlow(pck, dst, log).collect()

        setEffect(Effect.OpenPacked(pck.folder))
        awaitCancellation()
    }
    .flatMapLatest { it }
    .flowOn(Dispatchers.IO)
    .asUiResultFlow()
}

sealed class Effect : UiEffect {
    data class OpenPacked(val file: File) : Effect()
}