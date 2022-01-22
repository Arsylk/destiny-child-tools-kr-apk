package com.arsylk.mammonsmite.model.destinychild

data class FullSkill(
    val text: String?,
    val data: SkillActiveData,
    val buffs: Map<String, FullBuff>,
) {
    inline val isEmpty get() = data.idx == "0"

    val name by lazy { text?.substringBefore('\t') }
    val contents by lazy {
        text?.substringAfterLast('\t')
            ?.replace("\\", "\n")
    }

    companion object {
        val Empty = FullSkill(
            text = null,
            data = SkillActiveData(
                idx = "0",
                attribute = 0,
                buff1 = "0",
                buff2 = "0",
                buff3 = "0",
                buff4 =  "0",
                buffApply = 0,
                cost = 0,
                duration1 = 0,
                duration1Time = 0,
                duration1Type = 0,
                duration2 = 0,
                duration2Time = 0,
                duration2Type = 0,
                duration3 = 0,
                duration3Time = 0,
                duration3Type = 0,
                duration4 = 0,
                duration4Time = 0,
                duration4Type = 0,
                gaugePerSec = 0,
                levelEquation = 0,
                levelEquation1 = 0,
                levelEquation2 = 0,
                levelEquation3 = 0,
                levelEquation4 = 0,
                nAttack = 0,
                nAttack1 = 0,
                nAttack2 = 0,
                nAttack3 = 0,
                nAttack4 = 0,
                target = 0,
                target1 = 0,
                target2 = 0,
                target3 = 0,
                target4 = 0,
                targetFit = 0,
                targetFit1 = 0,
                targetFit2 = 0,
                targetFit3 = 0,
                targetFit4 = 0,
                timeCool = 0,
                type = 0,
                value = 0,
                valueType = 0,
                value1 = 0,
                value1Type = 0,
                value2 = 0,
                value2Type = 0,
                value3 = 0,
                value3Type = 0,
                value4 = 0,
                value4Type = 0,
            ),
            buffs = emptyMap(),
        )
    }
}