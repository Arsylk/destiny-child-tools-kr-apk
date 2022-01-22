package com.arsylk.mammonsmite.domain.destinychild

import com.arsylk.mammonsmite.model.destinychild.CharData
import com.arsylk.mammonsmite.model.destinychild.ChildRole
import com.arsylk.mammonsmite.model.destinychild.FullSkill
import com.arsylk.mammonsmite.model.destinychild.SkillActiveData
import java.text.DecimalFormat
import java.util.logging.Level
import kotlin.math.floor

object SkillValueParser {

    fun parseCharacterSkill(
        char: CharData,
        skill: FullSkill,
        level: Int = 1
    ): String? {
        val data = skill.data
        var text = skill.contents ?: return null

        val values = data.values.mapIndexed { i, value ->
            LevelEquationParser.evaluate(value, level, data.levelEquations[i])
        }
        val valueTypes = data.valueTypes
        val durations = data.durations

        val parsedValue = when (data.skillType) {
            SkillActiveData.Type.DEFAULT -> parseDefault(char.atk, data.value)
            SkillActiveData.Type.NORMAL -> parseNormal(char.atk, data.value, data.valueType)
            SkillActiveData.Type.SLIDE -> parseSlide(char.atk, char.agi, data.value, data.valueType)
            SkillActiveData.Type.DRIVE -> parseDrive(char.atk, char.agi, char.cri, char.baseGrade, data.value, data.valueType)
            SkillActiveData.Type.LEADER, SkillActiveData.Type.UNKNOWN -> data.value
        }

        val valueLevel = LevelEquationParser.evaluate(data.value, level, data.levelEquation)
        text = text.replace(SkillActiveData.stringKey("value", 0), "$parsedValue")
        text = text.replace(SkillActiveData.stringKey("p_value", 0), percentString(valueLevel))
        for (i in 1 until values.size) {
            val valueText =  when (valueTypes[i]) {
                ValueType.PERCENT.int -> percentString(values[i])
                else -> "${values[i]}"
            }
            text = text.replace(SkillActiveData.stringKey("value", i), if (values[i] >= 0) "+$valueText" else valueText)
            text = text.replace(SkillActiveData.stringKey("p_value", i), valueText)


            val healValue = parseHeal(char.atk, data.durations[i], char.role)
            val healText = when (valueTypes[i]) {
                ValueType.PERCENT.int -> "${values[i] / 10}%+$healValue"
                else -> "${values[i] + healValue}"
            }
            text = text.replace(SkillActiveData.stringKey("h_value", i), healText)


            val durationText = durations[i].toString()
            text = text.replace(SkillActiveData.stringKey("duration", i), durationText)
        }

        return text
    }

    private fun percentString(value: Int): String {
        return DecimalFormat("0.#").format(value / 10.0f) + "%"
    }

    private fun parseDefault(atk: Int, value: Int): Int {
        val statusDmg = atk * 20
        val float = statusDmg / 500.0f + value
        return floor(float).toInt()
    }

    private fun parseNormal(atk: Int, value: Int, valueType: Int): Int {
        val statusDmg = atk * 125
        val float = if (valueType == ValueType.PERCENT.int)
            (statusDmg * value / 1000.0f) / 400.0f
        else
            (statusDmg + value * 100) / 400.0f
        return floor(float).toInt()
    }

    private fun parseSlide(atk: Int, agi: Int, value: Int, valueType: Int): Int {
        val statusDmg = atk * 120 + agi * 30
        val float = if (valueType == ValueType.PERCENT.int)
            (statusDmg * value / 1000.0f) / 400.0f
        else
            (statusDmg + value * 100) / 400.0f
        return floor(float).toInt()
    }

    private fun parseDrive(atk: Int, agi: Int, cri: Int, baseGrade: Int, value: Int, valueType: Int): Int {
        val statusDmg = atk * 140 + agi * 40 + cri * 40
        val baseGradeDmg = (1 + baseGrade / 5.0f)
        val float = if (valueType == ValueType.PERCENT.int)
            (statusDmg * value / 1000.0f) * baseGradeDmg / 400.0f
        else
            (statusDmg + value * 100) * baseGradeDmg / 400.0f
        return floor(float).toInt()
    }

    private fun parseHeal(atk: Int, duration: Int, role: ChildRole): Int {
        val healPercent = when (role) {
            ChildRole.ATTACKER, ChildRole.TANK, ChildRole.DEBUFFER, ChildRole.SUPPORT -> 30
            ChildRole.HEALER -> 5
            else -> 1
        }
        val durationMultiplier = if (duration > 0) 8 else 1
        return atk / (healPercent * durationMultiplier + 1)
    }

    enum class ValueType(val int: Int) {
        PLUS(1),
        PERCENT(2);
    }
}