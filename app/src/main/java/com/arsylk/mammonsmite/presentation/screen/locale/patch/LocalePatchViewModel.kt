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
import com.arsylk.mammonsmite.model.common.LogLineChannel
import com.arsylk.mammonsmite.model.common.stateIn
import kotlinx.coroutines.Dispatchers
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
    private val log = LogLineChannel()
    private val _itemSource = MutableStateFlow<PatchItem?>(null)
    private val _itemPatch = MutableStateFlow<PatchItem?>(null)
    val logLines = log.stateIn(viewModelScope)
    val itemSource by lazy(_itemSource::asStateFlow)
    val itemPatch by lazy(_itemPatch::asStateFlow)

    init {
        val localeFile = File(prefs.destinychildLocalePath)
        loadLocalePatchSource(PatchSource.Local(localeFile, LocalLocalePatch.PCK))
    }


    fun loadLocalePatchSource(source: PatchSource) {
        loadLocalePatch(source).onEach { _itemSource.value = it }
            .launchIn(viewModelScope)
    }

    fun loadLocalePatchPatch(source: PatchSource) {
        loadLocalePatch(source).onEach { _itemPatch.value = it }
            .launchIn(viewModelScope)
    }

    private fun loadLocalePatch(source: PatchSource): Flow<PatchItem> {
        return when (source) {
            is PatchSource.Local -> loadLocalePatch(source.file, source.type)
            is PatchSource.Remote -> loadLocalePatch(source.type)
        }
    }

    private fun loadLocalePatch(file: File, type: LocalLocalePatch): Flow<PatchItem> {
        val item = PatchItem(
            source = PatchSource.Local(file, type),
            isLoading = true,
            patch = null,
            throwable = null,
        )
        return flow {
            emit(item)
            val actualFile = prepareActualFile(IFile.parse(file))
            val patch = when (type) {
                LocalLocalePatch.JSON -> pckTools.jsonFileToLocalePatch(actualFile)
                LocalLocalePatch.PCK -> {
                    val packed = pckTools
                        .readPackedPck(actualFile, log)
                        .asSuccess()
                    val unpacked = pckTools
                        .unpackAsFlow(packed, unpackTo(file), log)
                        .asSuccess()
                    val patch = pckTools
                        .unpackedPckToLocalePatch(unpacked, log)
                        .asSuccess()
                    patch
                }
            }
            emit(
                item.copy(
                    isLoading = false,
                    patch = patch,
                    throwable = null,
                )
            )
        }
        .catch { t -> emit(item.copy(isLoading = false, throwable = t)) }
        .flowOn(Dispatchers.IO)
    }

    private fun loadLocalePatch(type: RemoteLocalePatch): Flow<PatchItem> {
        val item = PatchItem(
            source = PatchSource.Remote(type),
            isLoading = true,
            patch = null,
            throwable = null,
        )
        return flow {
            emit(item)
            val patch = when (type) {
                RemoteLocalePatch.ENGLISH -> service.getEnglishPatch()
                RemoteLocalePatch.RUSSIAN -> service.getRussianPatch()
            }

            emit(
                item.copy(
                    isLoading = false,
                    patch = patch,
                    throwable = null,
                )
            )
        }
        .catch { t -> emit(item.copy(isLoading = false, throwable = t)) }
        .flowOn(Dispatchers.IO)
    }

    private fun unpackTo(file: File): File {
        val tempFolder = CommonFiles.cache
            .apply { kotlin.runCatching { if (!exists()) mkdirs() } }
        return File(tempFolder, file.nameWithoutExtension)
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
}

sealed class Effect : UiEffect