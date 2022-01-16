package com.arsylk.mammonsmite.model.destinychild

data class FullCharacter(
    val data: CharData,
    val viewIdxNames: Map<ViewIdx, String?>,
    val baseSkills: SkillSet,
    val ignitedSkills: SkillSet?,
) {

    inline val allSkills get() = baseSkills.skills + ignitedSkills?.skills.orEmpty()
}

data class SkillSet(
    val default: SkillWithBuffs,
    val normal: SkillWithBuffs,
    val slide: SkillWithBuffs,
    val drive: SkillWithBuffs,
    val leader: SkillWithBuffs,
) {

    inline val skills get() = listOf(default, normal, slide, drive, leader)
}

data class SkillWithBuffs(
    val text: String?,
    val data: SkillActiveData?,
    val buffs: List<BuffData>,
) {

    val name by lazy { text?.substringBefore('\t') }
    val contents by lazy {
        text?.substringAfterLast('\t')
            ?.replace("\\", "\n")
            ?.replace("<color=.*?>(.*?)</color>".toRegex(), "$1")
    }
}

data class BuffData(
    val text: String?,
    val data: SkillBuffData,
)