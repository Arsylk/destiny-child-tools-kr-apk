package com.arsylk.mammonsmite.presentation.screen.pck.unpacked

import androidx.lifecycle.viewModelScope
import com.arsylk.mammonsmite.domain.asSuccess
import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.CommonFiles
import com.arsylk.mammonsmite.domain.live2d.L2DTools
import com.arsylk.mammonsmite.domain.pck.PckTools
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.domain.repo.CharacterRepository
import com.arsylk.mammonsmite.domain.base.FileListFlow
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.domain.toSnackbarMessage
import com.arsylk.mammonsmite.model.common.*
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import com.arsylk.mammonsmite.presentation.dialog.pck.unpacked.PckUnpackedConfigState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File
import java.util.concurrent.ConcurrentHashMap

@ExperimentalSerializationApi
class PckUnpackedViewModel(
    private val pckTools: PckTools,
    private val l2dTools: L2DTools,
    private val repo: CharacterRepository,
    private val prefs: AppPreferences,
) : EffectViewModel<Effect>() {
    private val folder = CommonFiles.External.appUnpackedFolder
    private val foldersFlow = FileListFlow(viewModelScope, folder) { it.isDirectory && it.exists() && it.canRead() }
    private val foldersChannel = Channel<File>()
    private val updateChannel = Channel<Unit>(Channel.CONFLATED)
    private val cacheMap = ConcurrentHashMap<String, UnpackedLive2DItem>()
    private val _drawerItem = MutableStateFlow<UnpackedLive2DItem?>(null)
    val items = updateChannel.toItemList()
    val drawerItem by lazy(_drawerItem::asStateFlow)


    init {
        // folders flow to cache
        viewModelScope.launch(Dispatchers.IO) {
            foldersFlow.flow.collectLatest { list ->
                val keys = list.map(File::getName).toSet()
                val remove = cacheMap.keys.toSet() - keys
                remove.onEach(cacheMap::remove)
                list.forEach { foldersChannel.send(it) }
            }
        }

        // folders to items
        viewModelScope.launch(Dispatchers.IO) {
            val gameFolder = File(prefs.destinychildFilesPath)
            foldersChannel.receiveAsFlow()
                .flatMapMerge { file ->
                    channelFlow {
                        var job: Job? = null

                        val cache = cacheMap[file.name]
                        val pck = pckTools.readUnpackedPck(file)
                        val l2d = l2dTools.readL2DFile(file)

                        when {
                            cache == null -> {
                                // create item & enqueue file check
                                val item = UnpackedLive2DItem(pck, l2d, InGame.UNDETERMINED, pck.backupFile.exists(), 0)
                                send(item)
                                job = launch(Dispatchers.IO) {
                                    val isInGame = IFile(gameFolder, pck.header.gameRelativePath)
                                        .run { isFile }
                                    send(
                                        item.run {
                                            copy(
                                                inGame = if (isInGame) InGame.PRESENT else InGame.MISSING,
                                                updated = updated + 1,
                                            )
                                        }
                                    )
                                }
                            }
                            cache.pck != pck || cache.l2dFile != l2d -> {
                                // update item & enqueue file check if dirty
                                val isDirty = cache.pck.header.gameRelativePath != pck.header.gameRelativePath
                                val inGame = if (isDirty) InGame.UNDETERMINED else cache.inGame
                                val item = UnpackedLive2DItem(pck, l2d, inGame, pck.backupFile.exists(), cache.updated + 1)
                                send(item)
                                if (isDirty) job = launch(Dispatchers.IO) {
                                    val isInGame = IFile(gameFolder, pck.header.gameRelativePath)
                                        .run { isFile }
                                    send(
                                        item.run {
                                            copy(
                                                inGame = if (isInGame) InGame.PRESENT else InGame.MISSING,
                                                updated = updated + 1,
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        awaitClose { job?.cancel() }
                    }
                    .map { file.name to it }
                    .catch {}
                    .flowOn(Dispatchers.IO)
                }
                .onEach { (key, value) -> cacheMap[key] = value }
                .mapLatest { updateChannel.send(Unit) }
                .collect()
        }
    }

    suspend fun prepareSaveState(save: File): PckUnpackedConfigState? {
        return kotlin.runCatching {
            val pck = pckTools.readUnpackedPck(save)
            val l2d = l2dTools.runCatching { readL2DFile(save) }
                .getOrNull()
            val viewIdx = l2d?.header?.viewIdx
            val name = repo.viewIdxNames.value[viewIdx]

            PckUnpackedConfigState(
                pck = pck,
                l2d = l2d,
                type = PckUnpackedConfigState.Type.SAVE,
                name = InputField(
                    name ?: save.name,
                    { it.isNotBlank() },
                    {
                        when {
                            it.isBlank() -> "Name can't be blank"
                            else -> null
                        }
                    }
                ),
                folder = InputField(
                    save.name,
                    { !File(folder, it).exists() && it.isNotBlank() },
                    {
                        when {
                            File(folder, it).exists() -> "Folder already exists"
                            it.isBlank() -> "Folder can't be blank"
                            else -> null
                        }
                    }
                ),
                viewIdx = InputField(
                    viewIdx?.string ?: "",
                    { it.isBlank() || ViewIdx.parse(it) != null },
                    {
                        when {
                            it.isBlank() -> null
                            ViewIdx.parse(it) == null -> "Invalid View Idx"
                            else -> null
                        }
                    }
                ),
                gameRelativePath = InputField(pck.header.gameRelativePath)
            )
        }.getOrNull()
    }

    fun prepareLive2DConfigState(item: UnpackedLive2DItem): PckUnpackedConfigState {
        return PckUnpackedConfigState(
            pck = item.pck,
            l2d = item.l2dFile,
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
            viewIdx = InputField(
                item.l2dFile.header.viewIdx?.string ?: "",
                { it.isBlank() || ViewIdx.parse(it) != null },
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
        state: PckUnpackedConfigState,
        moveToUnpacked: Boolean = false,
    ) {
        withLoading {
            var (pck, l2d) = state
            val dst = File(folder, state.folder.value)


            pck = pck.copy(
                header = pck.header
                    .copy(
                        name = state.name.value,
                        gameRelativePath = state.gameRelativePath.value,
                    )
            )

            if (moveToUnpacked) pck = pckTools.saveUnpackedPck(pck, dst)
            pckTools.writeUnpackedPckHeader(pck)

            l2d = l2d?.run {
                copy(
                    folder = dst.takeIf { moveToUnpacked } ?: folder,
                    header = header.copy(
                        viewIdx = ViewIdx.parse(state.viewIdx.value),
                    )
                )
            }
            l2d?.also { pckTools.writeL2DFileHeader(it) }
            foldersChannel.send(pck.folder)
        }
    }

    fun setActionDrawerItem(item: UnpackedLive2DItem?) {
        _drawerItem.value = item
    }

    fun deleteLive2DItem(item: UnpackedLive2DItem) {
        withLoading(tag = "delete") {
            pckTools.deleteUnpackedPck(item.pck)
            cacheMap.remove(item.pck.folder.name)
            updateChannel.send(Unit)
        }
    }

    fun packUnpackedPck(pck: UnpackedPckFile, load: Boolean): StateFlow<List<LogLine>> {
        val log = LogLineChannel()
        val dst = File(pck.folder, "${pck.folder.nameWithoutExtension}.pck")
        val flow = pckTools.packAsFlow(pck, dst, log)
        withLoading(tag = "pack") {
            flow.asSuccess()
            try {
                if (load) {
                    log.info("Copying file ...", "Load")
                    val gameFolder = File(prefs.destinychildFilesPath)
                    val gameDst = IFile(gameFolder, pck.header.gameRelativePath)
                    val backup = pck.backupFile
                    if (!backup.exists()) {
                        log.info("Backup to: ${backup.name}", "Backup")
                        pck.backupFile.writeBytes(gameDst.inputStream().use { it.readBytes() })
                        launch {
                            val cached = cacheMap[pck.folder.name]
                            if (cached != null) {
                                cacheMap[pck.folder.name] = cached.run {
                                    copy(
                                        isBackedUp = backup.exists(),
                                        updated = updated + 1,
                                    )
                                }
                                updateChannel.send(Unit)
                            }
                        }
                    } else {
                        log.info("Backup already exists", "Backup")
                    }

                    log.info("Moving to:", "Load")
                    log.info(gameDst.absolutePath, "Load")

                    gameDst.outputStream().use { it.write(dst.readBytes()) }
                    log.info("Loaded successfully", "Load")
                }
            }catch (t: Throwable) {
                log.error(t, "Load")
            }

        }

        return log.stateIn(viewModelScope)
    }

    fun restorePckBackup(pck: UnpackedPckFile) {
        withLoading(tag = "restore") {
            val gameFolder = File(prefs.destinychildFilesPath)
            val gameDst = IFile(gameFolder, pck.header.gameRelativePath)
            val backup = pck.backupFile

            kotlin.runCatching {
                if (backup.exists()) {
                    gameDst.outputStream().use { it.write(backup.readBytes()) }
                    setEffect(Effect.ShowSnackbar("Backup restored"))
                } else {
                    setEffect(Effect.ShowSnackbar("Backup doesn't exist"))
                }
            }.getOrElse {
                setEffect(Effect.ShowSnackbar(it.toSnackbarMessage()))
            }
        }
    }

    private fun Channel<Unit>.toItemList(): StateFlow<List<UnpackedLive2DItem>> {
        return receiveAsFlow()
            .mapLatest { cacheMap.entries.sortedBy { it.key }.map { it.value } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    }
}

sealed class Effect : UiEffect {
    data class ShowSnackbar(val text: String, val action: String? = null) : Effect()
    data class PackPck(val pck: UnpackedPckFile, val load: Boolean) : Effect()
}