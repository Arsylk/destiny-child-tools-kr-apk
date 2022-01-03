package com.arsylk.mammonsmite

import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.multidex.MultiDexApplication
import com.arsylk.mammonsmite.domain.koin.KoinService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalSerializationApi
@ExperimentalComposeUiApi
class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        KoinService.start(this)
    }
}