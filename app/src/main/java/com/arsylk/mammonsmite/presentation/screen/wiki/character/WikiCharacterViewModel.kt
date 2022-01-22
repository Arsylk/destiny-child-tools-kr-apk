package com.arsylk.mammonsmite.presentation.screen.wiki.character

import com.arsylk.mammonsmite.domain.base.EffectViewModel
import com.arsylk.mammonsmite.domain.base.UiEffect
import com.arsylk.mammonsmite.domain.destinychild.CharacterRepository
import com.arsylk.mammonsmite.domain.sync.SyncService
import com.arsylk.mammonsmite.model.common.UiResult
import kotlinx.coroutines.flow.combineTransform

class WikiCharacterViewModel(
    private val repo: CharacterRepository,
    private val idx: String,
) : EffectViewModel<Effect>() {
    val character = prepareUiCharacter()

    private fun prepareUiCharacter() =
        combineTransform(SyncService.isLoading, repo.fullCharacters) { isLoading, characters ->
            val char = characters[idx]
            when {
                char != null -> emit(UiResult(char))
                isLoading -> emit(UiResult.Loading())
                else -> emit(UiResult(CharacterNotFoundException(idx)))
            }
        }
}

data class CharacterNotFoundException(
    val idx: String,
) : Throwable(message = "Character with idx: $idx could not be found")

sealed class Effect : UiEffect