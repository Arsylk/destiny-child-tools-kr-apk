package com.arsylk.mammonsmite.presentation.screen.pck.swap

import androidx.lifecycle.viewModelScope
import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.encodeToFile
import com.arsylk.mammonsmite.domain.files.CommonFiles
import com.arsylk.mammonsmite.domain.live2d.L2DTools
import com.arsylk.mammonsmite.domain.pck.PckTools
import com.arsylk.mammonsmite.domain.repo.CharacterRepository
import com.arsylk.mammonsmite.model.common.*
import com.arsylk.mammonsmite.model.live2d.L2DExpressionInfo
import com.arsylk.mammonsmite.model.live2d.L2DFile
import com.arsylk.mammonsmite.model.live2d.L2DFileLoaded
import com.arsylk.mammonsmite.model.pck.UnpackedPckLive2D
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckEntry
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


@OptIn(ExperimentalSerializationApi::class)
class PckSwapViewModel(
    private val pckTools: PckTools,
    private val l2dTools: L2DTools,
    private val json: Json,
) : EffectViewModel<Effect>() {
    private val folder = CommonFiles.External.appUnpackedFolder
    private val _fromItem = MutableStateFlow<PckSwapItem?>(null)
    private val _toItem = MutableStateFlow<PckSwapItem?>(null)
    private val log = LogLineChannel()
    val fromItem by lazy(_fromItem::asStateFlow)
    val toItem by lazy(_toItem::asStateFlow)
    val logLines = log.stateIn(viewModelScope)

    fun setFromItem(item: UnpackedPckLive2D) {
        withLoading(tag = "parse-from") {
            val result = item.runCatching { parseToSwapItem(this) }
            result.onSuccess { _fromItem.value = it }
            result.onFailure { setEffect(Effect.ParsingError(it)) }
        }
    }

    fun setToItem(item: UnpackedPckLive2D) {
        withLoading(tag = "parse-to") {
            val result = item.runCatching { parseToSwapItem(this) }
            result.onSuccess { _toItem.value = it }
            result.onFailure { setEffect(Effect.ParsingError(it)) }
        }
    }

    fun swap(fromItem: PckSwapItem, toItem: PckSwapItem) {
        log.clear()
        withLoading(tag = "swap") {
            log.info("[${fromItem.pck.header.name}] -> [${toItem.pck.header.name}]")

            // store matches & problems
            val matches = LinkedHashMap<Match, Match>()
            val problems = mutableListOf<Problem>()
            val actions = mutableListOf<Action>()

            // check textures
            val matchingTextures = fromItem.info.textures.size == toItem.info.textures.size

            // iterate slots
            log.info("------------------------  files  ------------------------")
            for (slot in listEntriesIgnoreTextures(toItem)) {
                //direct & replace idx match
                val fromMatch = fromItem.pck.header.entries.firstOrNull {
                    it.filename == slot.filename.replace(toItem.viewIdx.string, fromItem.viewIdx.string)
                }
                if (fromMatch != null) {
                    log.info("(${fromItem.pck.folder.name}/${fromMatch.filename}) -> (swap/${slot.filename})")
                    matches[Match(fromItem, fromMatch)] = Match(toItem, slot)
                } else {
                    problems.add(Problem(null, slot, false))
                }
            }

            log.info("------------------------  textures  ------------------------")
            // matching textures
            if (matchingTextures) {
                // same => same
                for (i in fromItem.info.textures.indices) {
                    val fromTexture = fromItem.info.textures[i]
                    val fromEntry = fromItem.entryForFilename(fromTexture)
                    val toTexture = toItem.info.textures[i]
                    val toEntry = toItem.entryForFilename(toTexture)
                    log.info("(${fromItem.pck.folder.name}/$fromTexture) -> (swap/$toTexture)")

                    matches[Match(fromItem, fromEntry)] = Match(toItem, toEntry)

                    // fix textures from other swaps
                    if (!fromTexture.contains("texture_")) {
                        for (toFixEntry in listEntriesIgnoreTextures(toItem)) {
                            if (fromTexture == toFixEntry.filename) {
                                log.warn("! ! ! Swapping already swapped models ! ! !")
                                problems.add(Problem(null, toFixEntry, false))
                            }
                        }
                    }
                }
            } else {
                // more => less
                for (i in fromItem.info.textures.indices) {
                    val fromTexture = fromItem.info.textures[i]
                    val fromEntry = fromItem.entryForFilename(fromTexture)

                    // texture slots available
                    if (i < toItem.info.textures.size) {
                        val toTexture = toItem.info.textures[i]
                        val toEntry = toItem.entryForFilename(toTexture)
                        log.info("(${fromItem.pck.folder.name}/$fromTexture) -> (swap/$toTexture)")

                        matches[Match(fromItem, fromEntry)] = Match(toItem, toEntry)
                    } else {
                        problems.add(Problem(fromEntry, null, true))
                    }
                }
                // less => more
                for (i in fromItem.info.textures.size until toItem.info.textures.size) {
                    val toTexture = toItem.info.textures[i]
                    val toEntry = toItem.entryForFilename(toTexture)

                    // texture slots available
                    if (i < fromItem.info.textures.size) {
                        val fromTexture = fromItem.info.textures[i]
                        val fromEntry = fromItem.entryForFilename(fromTexture)
                        log.info("(${fromItem.pck.folder.name}/$fromTexture) -> (swap/$toTexture)")

                        matches[Match(fromItem, fromEntry)] = Match(toItem, toEntry)
                    } else {
                        problems.add(Problem(null, toEntry, true))
                    }
                }
            }

            // auto-resolve problems
            if (problems.isNotEmpty()) {
                log.info("------------------------  resolved  ------------------------")
                for (problem in problems.toList()) {
                    when {
                        //copy to file
                        !problem.important && problem.fromFile == null && problem.toFile != null -> {
                            log.info("(${toItem.pck.folder.name}/${problem.toFile.filename}) -> (swap/${problem.toFile.filename})")

                            // remove problem & add match
                            problems.remove(problem)
                            matches[Match(toItem, problem.toFile)] = Match(toItem, problem.toFile)
                        }
                        // replace file with texture
                        problem.important && problem.fromFile != null && problem.toFile == null -> {
                            val pair = getMostUnimportantEntry(toItem)
                            if (pair != null) {
                                val (unimportant, target) = pair
                                log.info("(${fromItem.pck.folder.name}/${problem.fromFile.filename}) -> (swap/${unimportant.filename})")

                                // load texture instead
                                problems.remove(problem)
                                actions.add(Action(target, unimportant.filename, Action.Kind.REMOVE))
                                actions.add(Action(Action.Target.TEXTURE, unimportant.filename, Action.Kind.ADD))

                                matches[Match(fromItem, problem.fromFile)] = Match(toItem, unimportant)
                            }
                        }
                        // ignore file with texture
                        problem.important && problem.fromFile == null && problem.toFile != null -> {
                            log.info("(${toItem.pck.folder.name}/${problem.toFile.filename}) -> (swap/${problem.toFile.filename})")

                            problems.remove(problem)
                            actions.add(Action(Action.Target.TEXTURE, problem.toFile.filename, Action.Kind.REMOVE))
                            matches[Match(toItem, problem.toFile)] = Match(toItem, problem.toFile)
                        }
                    }
                }
            }

            // iterate unresolved
            if (problems.isNotEmpty()) {
                log.info("------------------------  unmatched  ------------------------")
                for (problem in problems) {
                    val stringFrom = if (problem.fromFile != null)
                        "${fromItem.pck.folder.name}/${problem.fromFile.filename}"
                    else "???"
                    val stringTo = if (problem.toFile != null)
                        "swap/${problem.toFile.filename}"
                    else "???"
                    log.warn("($stringFrom) ${if (problem.important) "=" else "-"}> ($stringTo)")
                }
            }

            // iterate actions taken
            if (actions.isNotEmpty()) {
                log.info("------------------------  actions  ------------------------")
                for (action in actions) {
                    log.info("${action.kind.name.lowercase()} ${action.target.name.lowercase()} ${action.value}")
                }
            }

            // perform swap proper
            val result = kotlin.runCatching { performSwap(fromItem, toItem, matches, actions) }
            result.onSuccess { log.success("Successfully swapped to: ${it.pck.folder.name}") }
            result.onFailure { log.error(it) }
        }
    }

    private suspend fun performSwap(
        fromItem: PckSwapItem,
        toItem: PckSwapItem,
        matches: LinkedHashMap<Match, Match>,
        actions: List<Action>,
    ): PckSwapItem {
        // prepare swap output folder
        val output = prepareOutput(fromItem, toItem)

        // copy header file
        toItem.pck.headerFile.copyTo(File(output, UnpackedPckFile.HEADER_FILENAME))

        // copy matched files
        for ((key, value) in matches) {
            val keyFile = key.getFile()
            keyFile.copyTo(File(output, value.entry.filename))
        }

        // update model info
        var info = toItem.info
        for (action in actions) {
            // TODO really only perform actions on textures ?
            if (action.target == Action.Target.TEXTURE) {
                val textures = info.textures.toMutableList()
                var motionMap = info.motionMap
                when (action.kind) {
                    Action.Kind.ADD -> {
                        textures.add(action.value)

                        //if its a motion change file replace with idle mtn
                        motionMap = motionMap.mapValues { (_, list) ->
                            list.map { item ->
                                if (item.filename == action.value) item.copy(filename = "${toItem.viewIdx.string}_idle.mtn")
                                else item
                            }
                        }
                    }
                    Action.Kind.REMOVE -> {
                        textures.removeLastOrNull()
                    }
                }

                info = info.copy(textures = textures.toList(), motionMap = motionMap)
            }
        }

        // string replace all view_idx
        // TODO heavy handed but works ?
        var stringInfo = json.encodeToString(info)
        stringInfo = stringInfo.replace(fromItem.viewIdx.string, toItem.viewIdx.string)
        info = json.decodeFromString(stringInfo)

        // create final unpacked pck file
        val pck = UnpackedPckFile(
            folder = output,
            header = toItem.pck.header.copy(
                name = "${fromItem.name} ~> ${toItem.name}"
            ),
        )
        pckTools.writeUnpackedPckHeader(pck)

        // create final l2d file
        val l2d = L2DFile(
            folder = output,
            header =  toItem.l2d.header.copy(
                viewIdx = toItem.viewIdx,
            ),
        )
        pckTools.writeL2DFileHeader(l2d)

        // write model info to file
        json.encodeToFile(info, l2d.modelInfoFile)

        return PckSwapItem(
            pck = pck,
            loaded = L2DFileLoaded(
                l2dFile = l2d,
                modelInfo = info,
            ),
            viewIdx = toItem.viewIdx,
        )
    }

    @Throws(Throwable::class)
    private suspend fun parseToSwapItem(item: UnpackedPckLive2D): PckSwapItem {
        return PckSwapItem(
            pck = item.pck,
            loaded = L2DFileLoaded(
                l2dFile = item.l2d,
                modelInfo = l2dTools.readModelInfo(item.l2d),
            ),
            viewIdx = item.viewIdx ?: throw IllegalArgumentException("View Idx can't be null")
        )
    }

    private fun listEntriesIgnoreTextures(item: PckSwapItem): List<UnpackedPckEntry> {
        return item.pck.header.entries.filterNot {
            it.filename in item.info.textures
        }.sortedWith(Comparator { o1, o2 ->
            // TODO legacy code & should be improved
            if (o1.filename.contains(".") && o2.filename.contains("."))
                return@Comparator o1.filename.substring(o1.filename.lastIndexOf("."))
                    .compareTo(o2.filename.substring(o2.filename.lastIndexOf(".")))
            return@Comparator o1.filename.compareTo(o2.filename)
        })
    }

    private fun getMostUnimportantEntry(item: PckSwapItem): Pair<UnpackedPckEntry, Action.Target>? {
        val exp = item.info.expressions.firstOrNull()
            ?.let(L2DExpressionInfo::filename)
            ?.let(item::entryOrNullForFilename)
        if (exp != null) return exp to Action.Target.EXPRESSION

        val hitMtn = item.info.motionMap["hit"]
            ?.let { it.firstOrNull()?.filename }
            ?.let(item::entryOrNullForFilename)
        if (hitMtn != null) return hitMtn to Action.Target.MOTION

        val bannerMtn = item.info.motionMap["banner"]
            ?.let { it.firstOrNull()?.filename }
            ?.let(item::entryOrNullForFilename)
        if (bannerMtn != null) return bannerMtn to Action.Target.MOTION

        item.info.getSortedMotions().forEach { (name, info) ->
            if (name != "idle") {
                val mtn = item.entryOrNullForFilename(info.filename)
                if (mtn != null) return mtn to Action.Target.MOTION
            }
        }

        return null
    }

    private fun prepareOutput(fromItem: PckSwapItem, toItem: PckSwapItem): File {
        val basename = "${fromItem.pck.folder.name}_${toItem.pck.folder.name}_swap"
        var folder = File(folder, basename)
        var i = 0
        while (folder.exists()) {
            folder = File(folder, "${basename}_$i")
            i += 1
        }
        return folder.apply { runCatching { mkdirs() } }
    }

    internal data class Match(
        val item: PckSwapItem,
        val entry: UnpackedPckEntry,
    ) {

        fun getFile(): File = item.pck.getEntryFile(entry)
    }

    internal data class Problem(
        val fromFile: UnpackedPckEntry?,
        val toFile: UnpackedPckEntry?,
        val important: Boolean,
    )

    internal data class Action(
        val target: Target,
        val value: String,
        val kind: Kind,
    ) {
        enum class Target { TEXTURE, EXPRESSION, MOTION }
        enum class Kind { ADD, REMOVE }
    }

}
sealed class Effect : UiEffect {
    data class ParsingError(val throwable: Throwable) : Effect()
}