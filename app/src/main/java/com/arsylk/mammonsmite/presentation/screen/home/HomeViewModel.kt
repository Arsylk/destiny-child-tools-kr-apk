package com.arsylk.mammonsmite.presentation.screen.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arsylk.mammonsmite.domain.retrofit.RetrofitBannerService
import com.arsylk.mammonsmite.model.api.response.Banner
import com.arsylk.mammonsmite.model.api.response.BannersResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val service: RetrofitBannerService
) : ViewModel() {
    private val _banners = MutableStateFlow(emptyList<Banner>())
    val banners by lazy(_banners::asStateFlow)

    init {
//        _banners.value = handle.get<List<Banner>>(BANNERS_TAG).orEmpty()
        loadBanners()
    }

    private fun loadBanners() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val response = BannersResponse.fromDocument(service.getBanners())
//                handle.set(BANNERS_TAG, response.banners)
                _banners.value = response.banners
            }
        }
    }

    companion object {
        private const val BANNERS_TAG = "banners"
    }
}