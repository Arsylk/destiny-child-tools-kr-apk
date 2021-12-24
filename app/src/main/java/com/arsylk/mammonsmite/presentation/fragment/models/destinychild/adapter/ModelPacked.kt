package com.arsylk.mammonsmite.presentation.fragment.models.destinychild.adapter

import com.arsylk.mammonsmite.model.destinychild.CharData
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import com.arsylk.mammonsmite.model.destinychild.ViewIdx.Companion.preferred
import java.io.File


data class ModelPacked(
    val _id: Int,
    val files: List<File>,
    val viewIdxList: List<ViewIdx>,
    val char: CharData?,
    val resolvedName: String?,
) {
    val primaryText: String by lazy {
        val viewIdx = primaryViewIdx
        when {
            resolvedName != null -> resolvedName
            char != null -> char.koreanName
            viewIdx != null -> viewIdx.string
            else -> filesText
        }
    }
    val secondaryText: String by lazy {
        if (primaryText != filesText) filesText else ""
    }
    val filesText: String by lazy {
        files.map { it.name }.sorted().joinToString(separator = ", ")
    }
    val primaryViewIdx by lazy { viewIdxList.preferred() }


    fun matchesQuery(string: String): Boolean {
        return when {
            string.isBlank() -> true
            resolvedName?.contains(string, ignoreCase = true) == true -> true
            char?.idx?.contains(string, ignoreCase = true) == true -> true
            char?.koreanName?.contains(string, ignoreCase = true) == true -> true
            viewIdxList.any { it.string.contains(string, ignoreCase = true) } -> true
            files.any { it.name.contains(string, ignoreCase = true) } -> true
            else -> false
        }
    }
}