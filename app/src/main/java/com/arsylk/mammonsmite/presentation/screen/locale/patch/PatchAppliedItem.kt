package com.arsylk.mammonsmite.presentation.screen.locale.patch

import com.arsylk.mammonsmite.model.destinychild.LocalePatch

data class PatchAppliedItem(
    val source: LocalePatch,
    val patch: LocalePatch,
) {
    val applied = source + patch
    val newFiles = (patch.files.keys - source.files.keys).size
    val newKeys = prepareNewKeys()
    val changedValues = prepareChangedValues()

    private fun prepareNewKeys(): Int {
        return source.files.map { (k, inSource) ->
            val inPatch = patch.files[k] ?: return@map 0
            (inPatch.dict.keys - inSource.dict.keys).size
        }.sum()
    }

    private fun prepareChangedValues(): Int {
        return source.files.map { (k, inSource) ->
            val inPatch = patch.files[k] ?: return@map 0
            inSource.dict.count { (name, value) ->
                val inPatchValue = inPatch.dict[name] ?: return@count false
                value != inPatchValue
            }
        }.sum()
    }
}