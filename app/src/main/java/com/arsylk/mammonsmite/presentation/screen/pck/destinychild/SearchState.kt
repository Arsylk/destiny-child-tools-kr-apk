package com.arsylk.mammonsmite.presentation.screen.pck.destinychild

import com.arsylk.mammonsmite.model.destinychild.ChildAttribute
import com.arsylk.mammonsmite.model.destinychild.ChildRole

data class SearchState(
    val text: String,
    val skin: String,
    val viewIdx: String,
    val skillsFuzzy: String,
    val skillLogic: String,
    val charIdx: String,
    val skillIdx: String,
    val buffIdx: String,
    val attributes: Set<ChildAttribute>,
    val roles: Set<ChildRole>,
    val stars: Int,
) {

    companion object {
        val Default = SearchState(
            text = "",
            skin = "",
            viewIdx = "",
            skillsFuzzy = "",
            skillLogic = "",
            charIdx = "",
            skillIdx = "",
            buffIdx = "",
            attributes = emptySet(),
            roles = emptySet(),
            stars = 0
        )
    }
}