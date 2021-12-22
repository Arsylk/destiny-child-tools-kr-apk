package com.arsylk.mammonsmite.domain.koin

import android.app.Application
import android.content.Context
import com.arsylk.mammonsmite.Cfg
import com.arsylk.mammonsmite.domain.pck.PckTools
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.domain.repo.CharacterRepository
import com.arsylk.mammonsmite.domain.retrofit.JsoupConverterFactory
import com.arsylk.mammonsmite.domain.retrofit.RetrofitApiService
import com.arsylk.mammonsmite.domain.retrofit.RetrofitBannerService
import com.arsylk.mammonsmite.presentation.activity.main.MainViewModel
import com.arsylk.mammonsmite.presentation.dialog.file.picker.FilePickerViewModel
import com.arsylk.mammonsmite.presentation.dialog.pck.unpack.PckUnpackViewModel
import com.arsylk.mammonsmite.presentation.fragment.home.HomeViewModel
import com.arsylk.mammonsmite.presentation.fragment.models.destinychild.ModelsDestinyChildViewModel
import com.arsylk.mammonsmite.presentation.fragment.settings.SettingsViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
object KoinService {

    fun start(app: Application) {
        startKoin {
            androidLogger(level = Level.ERROR)
            androidContext(app)
            modules(
                module {
                    single { provideAppCoroutineScope() }
                    single { provideGson() }
                    single { provideOkHttpClient() }
                    single { provideGsonConverterFactory(get()) }
                    single { provideRetrofitApiService(get(), get()) }
                    single { provideRetrofitBannerService(get()) }
                    single { provideAppPreferences(get()) }
                    single { provideCharacterRepository(get(), get()) }
                    single { providePckTools(get()) }
                },
                module {
                    viewModel { MainViewModel(get()) }
                    viewModel { HomeViewModel(get(), get()) }
                    viewModel { ModelsDestinyChildViewModel(get(), get()) }
                    viewModel { SettingsViewModel(get()) }
                    viewModel { param -> FilePickerViewModel(param.get()) }
                    viewModel { param -> PckUnpackViewModel(get(), file = param.get()) }
                }
            )
        }
    }

    private fun provideAppCoroutineScope() =
        CoroutineScope(SupervisorJob())

    private fun provideGson() =
        GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create()

    private fun provideOkHttpClient() =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()

    private fun provideGsonConverterFactory(gson: Gson) =
        GsonConverterFactory.create(gson)

    private fun provideRetrofitApiService(gson: Gson, okHttpClient: OkHttpClient) =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(Cfg.API_URL)
            .client(okHttpClient)
            .build()
            .create(RetrofitApiService::class.java)

    private fun provideRetrofitBannerService(okHttpClient: OkHttpClient) =
        Retrofit.Builder()
            .baseUrl("https://kr-gf.line.games/")
            .client(okHttpClient)
            .addConverterFactory(JsoupConverterFactory())
            .build()
            .create(RetrofitBannerService::class.java)

    private fun provideAppPreferences(context: Context) =
        AppPreferences(context)

    private fun provideCharacterRepository(scope: CoroutineScope, apiService: RetrofitApiService) =
        CharacterRepository(scope, apiService)

    private fun providePckTools(gson: Gson) =
        PckTools(gson)
}