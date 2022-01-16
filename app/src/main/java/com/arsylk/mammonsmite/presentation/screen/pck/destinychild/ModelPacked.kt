package com.arsylk.mammonsmite.presentation.screen.pck.destinychild

import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.model.destinychild.CharData
import com.arsylk.mammonsmite.model.destinychild.FullCharacter
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import com.arsylk.mammonsmite.model.destinychild.ViewIdx.Companion.preferred


data class ModelPacked(
    val key: String,
    val files: List<IFile>,
    val viewIdxList: List<ViewIdx>,
    val char: FullCharacter?,
    val resolvedName: String?,
) {
    val primaryText: String by lazy {
        val viewIdx = primaryViewIdx
        when {
            resolvedName != null -> resolvedName
            char != null -> char.data.koreanName
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
            char?.data?.idx?.contains(string, ignoreCase = true) == true -> true
            char?.data?.koreanName?.contains(string, ignoreCase = true) == true -> true
            viewIdxList.any { it.string.contains(string, ignoreCase = true) } -> true
            files.any { it.name.contains(string, ignoreCase = true) } -> true
            else -> false
        }
    }
}