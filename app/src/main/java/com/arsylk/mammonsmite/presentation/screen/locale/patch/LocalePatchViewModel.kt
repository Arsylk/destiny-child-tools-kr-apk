package com.arsylk.mammonsmite.presentation.screen.locale.patch

import androidx.lifecycle.viewModelScope
import com.arsylk.mammonsmite.domain.asSuccess
import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.CommonFiles
import com.arsylk.mammonsmite.domain.files.DocFile
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.domain.files.NormalFile
import com.arsylk.mammonsmite.domain.pck.PckTools
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.domain.retrofit.RetrofitApiService
import com.arsylk.mammonsmite.model.common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File

@ExperimentalSerializationApi
class LocalePatchViewModel(
    private val pckTools: PckTools,
    private val service: RetrofitApiService,
    private val prefs: AppPreferences,
) : EffectViewModel<Effect>() {
    private val gameSource = PatchSource.Local(IFile(prefs.destinychildLocalePath), LocalLocalePatch.PCK)
    private val log = LogLineChannel()
    private val updateSrc = Channel<Unit>(Channel.CONFLATED)
    private val _sourceSrc = MutableStateFlow<PatchSource>(PatchSource.Game)
    private val _patchSrc = MutableStateFlow<PatchSource>(PatchSource.Remote(RemoteLocalePatch.ENGLISH))
    private val _destination = MutableStateFlow<PatchDestination>(PatchDestination.Game)
    val logLines = log.stateIn(viewModelScope)
    val sourceSrc by lazy(_sourceSrc::asStateFlow)
    val sourcePatch = _sourceSrc.preparePatchSource(updateSrc)
    val patchSrc by lazy(_patchSrc::asStateFlow)
    val patchPatch = _patchSrc.preparePatchSource()
    val itemApplied = prepareItemApplied()
    val destination by lazy(_destination::asStateFlow)


    fun setSourceSrc(source: PatchSource) {
        _sourceSrc.value = source
    }

    fun setPatchSrc(source: PatchSource) {
        _patchSrc.value = source
    }

    fun setDestination(destination: PatchDestination) {
        _destination.value = destination
    }

    fun savePatchTo(applied: PatchAppliedItem, destination: PatchDestination) {
        withLoading(tag = "save") {
            val iFile = when(destination) {
                PatchDestination.Game -> gameSource.file
                is PatchDestination.Pck -> destination.file
                is PatchDestination.Json -> destination.file
            }
            val result = kotlin.runCatching {
                when (destination) {
                    PatchDestination.Game, is PatchDestination.Pck -> {
                        val temp = File(CommonFiles.cache, "temp_locale.pck")
                        pckTools.localePatchToPck(applied.applied, temp)
                        iFile.outputStream().use { it.write(temp.readBytes()) }
                        temp.delete()
                    }
                    is PatchDestination.Json -> {
                        val temp = File(CommonFiles.cache, "temp_locale.json")
                        pckTools.localePatchToJsonFile(applied.applied, temp)
                        iFile.outputStream().use { it.write(temp.readBytes()) }
                        temp.delete()
                    }
                }
            }

            // update source if same as destination
            val update = when (val src = _sourceSrc.value) {
                PatchSource.Game -> gameSource.file == iFile
                is PatchSource.Local -> src.file == iFile
                is PatchSource.Remote -> false
            }
            if (update) updateSrc.send(Unit)

            result.onSuccess { setEffect(Effect.Success(iFile.absolutePath)) }
            result.onFailure { setEffect(Effect.Failure(it)) }
        }
    }


    private fun unpackTo(name: String): File {
        val tempFolder = CommonFiles.cache
            .apply { kotlin.runCatching { if (!exists()) mkdirs() } }
        return File(tempFolder, name.substringBeforeLast("."))
    }

    private suspend fun prepareActualFile(iFile: IFile): File {
        return when (iFile) {
            is NormalFile -> iFile.file
            is DocFile -> {
                withContext(Dispatchers.IO) {
                    CommonFiles.cache.run { if (!exists()) mkdirs() }
                    val file = File(CommonFiles.cache, iFile.name)
                    file.writeBytes(iFile.inputStream().use { it.readBytes() })
                    file
                }
            }
        }
    }

    private fun Flow<PatchSource>.preparePatchSource(channel: Channel<Unit> = Channel(Channel.CONFLATED)) =
        flatMapLatest { src ->
            channel.receiveAsFlow()
                .onStart { emit(Unit) }
                .mapAsUiResult {
                    when (src) {
                        PatchSource.Game, is PatchSource.Local -> {
                            val file =
                                if (src is PatchSource.Local) src.file else gameSource.file
                            val type =
                                if (src is PatchSource.Local) src.type else gameSource.type

                            val actualFile = prepareActualFile(file)
                            when (type) {
                                LocalLocalePatch.JSON -> pckTools.jsonFileToLocalePatch(
                                    actualFile
                                )
                                LocalLocalePatch.PCK -> {
                                    val packed = pckTools
                                        .readPackedPck(actualFile, log)
                                        .asSuccess()
                                    val unpacked = pckTools
                                        .unpackAsFlow(packed, unpackTo(file.name), log)
                                        .asSuccess()
                                    val patch = pckTools
                                        .unpackedPckToLocalePatch(unpacked, log)
                                        .asSuccess()
                                    patch
                                }
                            }
                        }
                        is PatchSource.Remote -> {
                            when (src.type) {
                                RemoteLocalePatch.ENGLISH -> service.getEnglishPatch()
                                RemoteLocalePatch.RUSSIAN -> service.getRussianPatch()
                            }
                        }
                    }
                }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UiResult.Loading())

    private fun prepareItemApplied(): Flow<UiResult<PatchAppliedItem>> =
        combineTransform(sourcePatch, patchPatch) { s, p ->
            when {
                s is UiResult.Failure -> emit(flowOf(UiResult(s.throwable)))
                p is UiResult.Failure -> emit(flowOf(UiResult(p.throwable)))
                s is UiResult.Loading || p is UiResult.Loading -> emit(flowOf(UiResult.Loading()))
                s is UiResult.Success && p is UiResult.Success -> emit(
                    uiResultOf {
                        PatchAppliedItem(s.value, p.value)
                    }
                )
            }
        }
        .flatMapLatest { it }
        .flowOn(Dispatchers.IO)
}

sealed class Effect : UiEffect {
    data class Success(val path: String) : Effect()
    data class Failure(val throwable: Throwable) : Effect()
}