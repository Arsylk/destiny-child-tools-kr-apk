package com.arsylk.mammonsmite.model.api.response

import org.jsoup.nodes.Document
import java.io.Serializable

data class BannersResponse(
    val banners: List<Banner>
) {

    companion object {
        fun fromDocument(doc: Document): BannersResponse {
            val banners = doc.select("a")
                .mapNotNull { a ->
                    kotlin.runCatching {
                        val href = a.attr("href")
                        if (href.startsWith("http")) {
                            Banner(
                                url = href,
                                imageUrl = a.selectFirst("img[src]").attr("src")
                            )
                        } else {
                            val parts = a.attr("onclick").split(",")
                            val urlPart = parts[0]
                                .substring(parts[0].indexOf("'") + 1)
                            val url = urlPart.substring(0, urlPart.indexOf("'"))

                            val imageUrlPart = parts[1]
                                .substring(parts[1].indexOf("'") + 1)
                            val imageUrl = imageUrlPart
                                .substring(0, imageUrlPart.indexOf("'"))
                                .replace("https://", "http://")
                            Banner(
                                url = url,
                                imageUrl = imageUrl,
                            )
                        }
                    }.getOrNull()
                }


            return BannersResponse(banners)
        }
    }
}

data class Banner(val url: String, val imageUrl: String) : Serializable