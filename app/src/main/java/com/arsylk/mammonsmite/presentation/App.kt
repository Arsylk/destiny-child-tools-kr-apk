package com.arsylk.mammonsmite.presentation

import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.multidex.MultiDexApplication
import com.arsylk.mammonsmite.domain.koin.KoinService

@ExperimentalFoundationApi
class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        KoinService.start(this)
    }
}