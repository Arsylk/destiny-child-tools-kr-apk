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
    val default: FullSkill,
    val normal: FullSkill,
    val slide: FullSkill,
    val drive: FullSkill,
    val leader: FullSkill,
) {

    inline val skills get() = listOf(default, normal, slide, drive, leader)
    inline val skillMap get() = mapOf(
        SkillActiveData.Type.DEFAULT to default,
        SkillActiveData.Type.NORMAL to normal,
        SkillActiveData.Type.SLIDE to slide,
        SkillActiveData.Type.DRIVE to drive,
        SkillActiveData.Type.LEADER to leader,
    )

    operator fun get(type: SkillActiveData.Type): FullSkill? = when (type) {
        SkillActiveData.Type.DEFAULT -> default
        SkillActiveData.Type.NORMAL -> normal
        SkillActiveData.Type.SLIDE -> slide
        SkillActiveData.Type.DRIVE -> drive
        SkillActiveData.Type.LEADER -> leader
        else -> null
    }
}