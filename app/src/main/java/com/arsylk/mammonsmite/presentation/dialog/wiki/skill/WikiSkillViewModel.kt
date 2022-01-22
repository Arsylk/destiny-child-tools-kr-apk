package com.arsylk.mammonsmite.presentation.dialog.wiki.skill

import com.arsylk.mammonsmite.domain.base.BaseViewModel
import com.arsylk.mammonsmite.domain.destinychild.SkillBuffRepository
import com.arsylk.mammonsmite.domain.sync.SyncService
import com.arsylk.mammonsmite.model.common.UiResult
import com.arsylk.mammonsmite.model.destinychild.FullSkill
import kotlinx.coroutines.flow.combineTransform
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WikiSkillViewModel(
    private val repo: SkillBuffRepository,
    private val json: Json,
    private val idx: String,
) : BaseViewModel() {
    val skill = prepareUiSkill()

    fun serializeSkill(skill: FullSkill): String {
        return json.encodeToString(skill.data)
    }

    private fun prepareUiSkill() =
        combineTransform(SyncService.isLoading, repo.fullSkills) { isLoading, skills ->
            val skill = skills[idx]
            when {
                skill != null -> emit(UiResult(skill))
                isLoading -> emit(UiResult.Loading())
                else -> emit(UiResult(SkillNotFoundException(idx)))
            }
        }
}

data class SkillNotFoundException(
    val idx: String,
) : Throwable(message = "Skill with idx: $idx could not be found")