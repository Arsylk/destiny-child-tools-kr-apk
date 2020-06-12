package com.arsylk.mammonsmite.utils


@Suppress("NAME_SHADOWING")
class EquationParser {
    companion object {
        fun multiply(_s: String): String {
            var s = _s
            "(-?[0-9.]+) \\* (-?[0-9.]+)".toRegex().findAll(s).forEach {
                val x = it.groups[1]!!.value
                val y = it.groups[2]!!.value
                val z = (x.toFloat() * y.toFloat())
                s = s.replace(it.value, z.toString())
            }
            return if(s.contains('*')) multiply(s) else s
        }

        fun single(_s: String): Float {
            var s = _s
            s = multiply(s)
            s = if(s[0] != '-') "+ $s" else s
            val matched = "([-+]?) ?([0-9.]+)".toRegex().findAll(s)
            val base = matched.map { ("${it.groups[1]!!.value}${it.groups[2]!!.value}".toFloat()) }.sum()

            return base
        }

        fun brackets(_s: String): String {
            var s = _s
            val matched = "\\(([^()]+?)\\)".toRegex().findAll(s)
            matched.forEach {
                val value = single(it.groups[1]!!.value).toString()
                s = s.replace(it.value, value)
            }
            return if(s.contains('(') or s.contains(')')) brackets(s) else s
        }

        fun parse(equation: String?, value: Int, level: Int): Int {
            var mEquation = equation ?: return value
            val mVariables = mapOf("inValue" to "$value", "level" to "$level", "tier" to "${level.div(10)}")
            mVariables.forEach {
                mEquation = mEquation.replace(it.key, it.value)
            }
            return single(brackets(mEquation)).toInt()
        }
    }
}