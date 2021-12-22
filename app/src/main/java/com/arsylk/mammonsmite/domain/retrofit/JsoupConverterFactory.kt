package com.arsylk.mammonsmite.domain.retrofit

import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import org.jsoup.nodes.Document


class JsoupConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, Document>? {
        if (getRawType(type) != Document::class.java)
            return null

        return Converter<ResponseBody, Document> { value ->
            Jsoup.parse(value.string()) ?: null
        }
    }
}