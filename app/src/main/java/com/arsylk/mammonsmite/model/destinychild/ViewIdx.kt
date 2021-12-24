package com.arsylk.mammonsmite.model.destinychild

import com.arsylk.mammonsmite.Cfg
import com.arsylk.mammonsmite.domain.serializer.ViewIdxSerializer
import kotlinx.serialization.Serializable


@Serializable(with = ViewIdxSerializer::class)
data class ViewIdx(
    val type: String,
    val number: Int,
    val variant: Int,
) {
    val string = "${type.lowercase()}${number.toString().padStart(3, '0')}_${variant.toString().padStart(2, '0')}"
    val iconUrl get() = "${Cfg.API_URL.trimEnd('/')}/static/icons/${string}.png"

    companion object {
        val regex = Regex("([A-z]+)([0-9]{3})_([0-9]{2})", RegexOption.IGNORE_CASE)

        fun parse(string: String): ViewIdx? {
            return kotlin.runCatching {
                regex.find(string)?.let { result ->
                    val groups = result.groupValues
                    ViewIdx(
                        type = groups[1],
                        number = groups[2].toInt(),
                        variant = groups[3].toInt(),
                    )
                }
            }.getOrNull()
        }

        fun Collection<ViewIdx>.preferred(): ViewIdx? {
            return firstOrNull { it.variant == 2 }
                ?: firstOrNull { it.variant == 1 }
                ?: firstOrNull()
        }

        fun Collection<ViewIdx>.ordered(): List<ViewIdx> {
            return sortedByDescending {
                when (it.variant) {
                    2 -> 1001
                    1 -> 1000
                    else -> 100 - it.variant
                }
            }
        }
    }
}