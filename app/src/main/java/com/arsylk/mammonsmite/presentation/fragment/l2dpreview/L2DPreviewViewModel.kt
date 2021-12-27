package com.arsylk.mammonsmite.presentation.fragment.l2dpreview

import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.DestinyChildFiles
import com.arsylk.mammonsmite.domain.live2d.L2DTools
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.model.live2d.L2DFile
import com.arsylk.mammonsmite.model.live2d.L2DFileLoaded
import com.arsylk.mammonsmite.presentation.view.live2d.BackgroundScale
import com.arsylk.mammonsmite.presentation.view.live2d.Live2DSurfaceConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File

@ExperimentalSerializationApi
class L2DPreviewViewModel(
    private val l2dTools: L2DTools,
    private val l2dFile: L2DFile,
    private val prefs: AppPreferences,
) : EffectViewModel<Effect>() {
    private val _loadedL2dFile = MutableStateFlow<L2DFileLoaded?>(null)
    private val _surfaceConfig = MutableStateFlow(Live2DSurfaceConfig.Default)
    val loadedL2dFile by lazy(_loadedL2dFile::asStateFlow)
    val surfaceConfig by lazy(_surfaceConfig::asStateFlow)

    init {
        withLoading {
            val modelInfo = l2dTools.runCatching { readModelInfo(l2dFile) }
                .getOrElse { return@withLoading setEffect(Effect.FatalError(it)) }
            _loadedL2dFile.value = L2DFileLoaded(l2dFile, modelInfo)

            val bgFolder = File(prefs.destinychildBackgroundsPath)
            val bgFile = DestinyChildFiles.getRandomBackgroundFile(bgFolder)
            updateSurfaceConfig { copy(bgFile = bgFile) }
        }
    }

    fun updateSurfaceConfig(update: Live2DSurfaceConfig.() -> Live2DSurfaceConfig) {
        _surfaceConfig.update(update)
    }
}

sealed class Effect : UiEffect {
    data class FatalError(val t: Throwable) : Effect()
}