package com.arsylk.mammonsmite.domain.destinychild


object LevelEquationParser {
    var equations = emptyMap<Int, String>()

    fun evaluate(value: Int, level: Int, equationId: Int): Int {
        return parse(equations[equationId], value, level)
    }

    private fun multiply(text: String): String {
        var s = text
        "(-?[0-9.]+) \\* (-?[0-9.]+)".toRegex().findAll(s).forEach {
            val x = it.groups[1]!!.value
            val y = it.groups[2]!!.value
            val z = (x.toFloat() * y.toFloat())
            s = s.replace(it.value, z.toString())
        }
        return if(s.contains('*')) multiply(s) else s
    }

    private fun single(text: String): Float {
        var s = text
        s = multiply(s)
        s = if (s[0] != '-') "+ $s" else s
        val matched = "([-+]?) ?([0-9.]+)".toRegex().findAll(s)
        return matched.map { ("${it.groups[1]!!.value}${it.groups[2]!!.value}".toFloat()) }.sum()
    }

    private fun brackets(text: String): String {
        var s = text
        val matched = "\\(([^()]+?)\\)".toRegex().findAll(s)
        matched.forEach {
            val value = single(it.groups[1]!!.value).toString()
            s = s.replace(it.value, value)
        }
        return if (s.contains('(') || s.contains(')')) brackets(s) else s
    }

    private fun parse(equation: String?, value: Int, level: Int): Int {
        var mEquation = equation ?: return value
        return kotlin.runCatching {
            val mVariables = mapOf("inValue" to "$value", "level" to "$level", "tier" to "${level / 10}")
            mVariables.forEach {
                mEquation = mEquation.replace(it.key, it.value)
            }
            single(brackets(mEquation)).toInt()
        }.getOrNull() ?: value
    }
}