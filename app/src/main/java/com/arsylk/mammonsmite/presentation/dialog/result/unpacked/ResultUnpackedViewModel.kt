package com.arsylk.mammonsmite.presentation.dialog.result.unpacked

import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.files.CommonFiles
import com.arsylk.mammonsmite.domain.live2d.L2DTools
import com.arsylk.mammonsmite.domain.pck.PckTools
import com.arsylk.mammonsmite.domain.safeListFiles
import com.arsylk.mammonsmite.model.common.LoadingList
import com.arsylk.mammonsmite.model.pck.UnpackedPckLive2D
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*

class ResultUnpackedViewModel(
    private val pckTools: PckTools,
    private val l2dTools: L2DTools,
) : EffectViewModel<Effect>() {
    private val folder = CommonFiles.External.appUnpackedFolder


    fun listUnpackedPckLive2D(): Flow<LoadingList<UnpackedPckLive2D>> {
        return channelFlow {
            val channel = Channel<UnpackedPckLive2D>()

            val job = launch(Dispatchers.IO) {
                folder.safeListFiles { it.isDirectory && it.exists() && it.canRead() }
                    .onEach { file ->
                        ensureActive()
                        launch {
                            runCatching {
                                val pck = pckTools.readUnpackedPck(file)
                                val l2d = l2dTools.readL2DFile(file)
                                channel.send(UnpackedPckLive2D(pck, l2d))
                            }
                        }
                    }
            }.invokeOnCompletion { channel.close() }

            val mutable = mutableListOf<UnpackedPckLive2D>()
            channel.consumeEach { item ->
                mutable.add(item)
                send(LoadingList(mutable, true))
            }
            send(LoadingList(mutable.toList(), false))
            close()

            awaitClose {
                job.dispose()
                channel.cancel()
            }
        }
        .flatMapLatest {
            flow {
                val sorted = it.run {
                    copy(items = items.sortedBy { it.key })
                }
                currentCoroutineContext().ensureActive()
                emit(sorted)
            }
        }
        .flowOn(Dispatchers.IO)
    }
}

sealed class Effect : UiEffect