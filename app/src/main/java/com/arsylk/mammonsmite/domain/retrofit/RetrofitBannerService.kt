package com.arsylk.mammonsmite.domain.retrofit

import org.jsoup.nodes.Document
import retrofit2.http.GET

interface RetrofitBannerService {

    @GET("/notice/DC/ANDROID/inGame")
    suspend fun getBanners(): Document
}