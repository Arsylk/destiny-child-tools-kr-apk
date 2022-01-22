package com.arsylk.mammonsmite.presentation.dialog.wiki.buff

import com.arsylk.mammonsmite.domain.base.BaseViewModel
import com.arsylk.mammonsmite.domain.destinychild.SkillBuffRepository
import com.arsylk.mammonsmite.domain.sync.SyncService
import com.arsylk.mammonsmite.model.common.UiResult
import kotlinx.coroutines.flow.combineTransform

class WikiBuffViewModel(
    private val repo: SkillBuffRepository,
    private val idx: String,
) : BaseViewModel() {
    val buff = prepareUiBuff()

    private fun prepareUiBuff() =
        combineTransform(SyncService.isLoading, repo.fullBuffs) { isLoading, buffs ->
            val buff = buffs[idx]
            when {
                buff != null -> emit(UiResult(buff))
                isLoading -> emit(UiResult.Loading())
                else -> emit(UiResult(BuffNotFoundException(idx)))
            }
        }
}

data class BuffNotFoundException(
    val idx: String,
) : Throwable(message = "Buff with idx: $idx could not be found")