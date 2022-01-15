package com.arsylk.mammonsmite.presentation.activity.main

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.arsylk.mammonsmite.domain.asResult
import com.arsylk.mammonsmite.domain.asSuccess
import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.CommonFiles
import com.arsylk.mammonsmite.domain.files.DocFile
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.domain.files.NormalFile
import com.arsylk.mammonsmite.domain.live2d.L2DTools
import com.arsylk.mammonsmite.domain.pck.PckTools
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.domain.repo.CharacterRepository
import com.arsylk.mammonsmite.domain.safeListFiles
import com.arsylk.mammonsmite.domain.sync.SyncService
import com.arsylk.mammonsmite.domain.toSnackbarMessage
import com.arsylk.mammonsmite.model.common.LogLine
import com.arsylk.mammonsmite.model.common.LogLineChannel
import com.arsylk.mammonsmite.model.live2d.L2DFileLoaded
import com.arsylk.mammonsmite.presentation.screen.pck.swap.PckSwapItem
import com.arsylk.mammonsmite.presentation.screen.pck.swap.PckSwapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.get
import java.io.File

class MainViewModel(
    private val syncService: SyncService,
): EffectViewModel<Effect>() {
    private val _progress = MutableStateFlow(0)
    val progress by lazy(_progress::asStateFlow)

    init { load(); yukine() }

    fun load() {
        withLoading(tag = "sync") {
            syncService.getSyncFlow()
                .collectLatest { (store, int) ->
                    _progress.value = int
                }
        }
    }

    private fun yukine() {
        return
        val vm = get<PckSwapViewModel>(PckSwapViewModel::class.java)
        val prefs = get<AppPreferences>(AppPreferences::class.java)
        val tools = get<PckTools>(PckTools::class.java)
        val l2dTools = get<L2DTools>(L2DTools::class.java)
        val base by lazy { IFile(prefs.destinychildModelsPath) }

        val logfile = File(CommonFiles.External.appUnpackedFolder, "log.txt")
        val s = logfile.outputStream().writer()
        val log = LogLineChannel {
            val st = "[${it.tag?.let {"$it:"} ?: ""}${it.type}] ${it.msg} ${if (it.throwable != null) it.throwable.toSnackbarMessage() else ""}\n"
            if (it.type == LogLine.Type.ERROR) s.write(st)
            println(st)
        }

        viewModelScope.launch(Dispatchers.Default) { vm.effect.collect() }

        viewModelScope.launch(Dispatchers.IO) {
            val yukineFile = File(CommonFiles.External.appUnpackedFolder, "yukine")
            val yukine = tools.readUnpackedPck(yukineFile)
            val yukineL2d = l2dTools.readL2DFile(yukineFile)
            val yukineSwap = PckSwapItem(
                pck = yukine,
                loaded = L2DFileLoaded(
                    yukineL2d,
                    l2dTools.readModelInfo(yukineL2d)
                ),
                viewIdx = yukineL2d.header.viewIdx!!
            )

            val list = base.listFiles()
            log.info("list: ${list.size}")
            list.asFlow()
                .onEach { log.info("file: ${it.absolutePath}") }
                .flatMapMerge { ifile ->
                    val actual = prepareActualFile(ifile)
                    val folder = File(CommonFiles.External.appUnpackedFolder, actual.nameWithoutExtension)
                    flow {
                        val packed = tools.readPackedPck(actual).asSuccess()
                        val unpacked = tools.unpackAsFlow(packed, folder).asSuccess()
                            .run {
                                copy(
                                    header = header.copy(
                                        gameRelativePath = File(ifile.absolutePath)
                                            .toRelativeString(File(prefs.destinychildFilesPath))
                                    )
                                )
                            }
                        tools.writeUnpackedPckHeader(unpacked)
                        val (modelPckFile, l2dFile) = tools.unpackedPckToModel(unpacked).asResult()
                            .getOrThrow()
                        val modelInfo = l2dTools
                            .runCatching { readModelInfo(l2dFile) }
                            .getOrThrow()
                        val loaded = L2DFileLoaded(l2dFile, modelInfo)
                        emit(PckSwapItem(
                            pck = modelPckFile,
                            loaded = loaded,
                            viewIdx = loaded.inferredViewIdx!!
                        ))
                    }
//                    .onEach { log.info("swap with: $it") }
//                    .onEach { item ->
//                        log.info("swap start: $item")
//                        val result = vm.resultSwap(yukineSwap, item)
//                            .getOrElse { throw SwapException(it) }
//                        log.success("swap success: $result")
//
//                        val gameFolder = File(prefs.destinychildFilesPath)
//                        val gameDst = IFile(gameFolder, result.pck.header.gameRelativePath)
//
//                        val dst = File(result.pck.folder, "${result.pck.folder.nameWithoutExtension}.pck")
//                        tools.packAsFlow(result.pck, dst).asSuccess()
//                        gameDst.outputStream().use { it.write(dst.readBytes()) }
//                        log.success("Successfully saved: ${gameDst.absolutePath}")
//                        kotlin.runCatching {
//                            result.pck.folder.safeListFiles().onEach(File::delete)
//                            result.pck.folder.delete()
//                        }
//                    }
                    .catch { t -> log.error(t, actual.name) }
                    .onCompletion {
                        kotlin.runCatching {
                            actual.delete()
                            folder.safeListFiles().onEach(File::delete)
                            folder.delete()
                        }
                    }
                }
                .collect()
            s.close()
        }
    }
    internal class SwapException(cause: Throwable) : Throwable(cause = cause)

    private suspend fun prepareActualFile(iFile: IFile): File {
        return withContext(Dispatchers.IO) {
                    CommonFiles.cache.run { if (!exists()) mkdirs() }
                    val file = File(CommonFiles.cache, iFile.name)
                    file.writeBytes(iFile.inputStream().use { it.readBytes() })
                    file
                }
    }
}

sealed class Effect : UiEffect