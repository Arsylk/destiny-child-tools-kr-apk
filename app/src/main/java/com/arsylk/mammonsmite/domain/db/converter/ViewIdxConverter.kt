package com.arsylk.mammonsmite.domain.db.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.arsylk.mammonsmite.model.destinychild.ViewIdx

@ProvidedTypeConverter
class ViewIdxConverter {

    @TypeConverter
    fun viewIdxToString(viewIdx: ViewIdx): String {
        return viewIdx.string
    }

    @TypeConverter
    fun stringToViewIdx(string: String): ViewIdx {
        return ViewIdx.parse(string)!!
    }
}