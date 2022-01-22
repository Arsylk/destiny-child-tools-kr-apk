package com.arsylk.mammonsmite.model.destinychild

data class FullBuff(
    val text: String?,
    val data: SkillBuffData,
    val categories: Map<Int, String>,
) {

    val name by lazy { text?.substringBefore('\t') }
    val contents by lazy {
        text?.substringAfterLast('\t')
            ?.replace("\\", "\n")
    }
}