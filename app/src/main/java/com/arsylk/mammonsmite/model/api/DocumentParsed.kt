package com.arsylk.mammonsmite.model.api

import org.jsoup.nodes.Document

interface DocumentParsed<T: Any> {
    fun fromDocument(doc: Document): T
}