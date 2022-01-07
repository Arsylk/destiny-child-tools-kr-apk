package com.arsylk.mammonsmite.domain.koin

import android.app.Application
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import com.arsylk.mammonsmite.Cfg
import com.arsylk.mammonsmite.domain.live2d.L2DTools
import com.arsylk.mammonsmite.domain.pck.PckTools
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.domain.repo.CharacterRepository
import com.arsylk.mammonsmite.domain.retrofit.JsoupConverterFactory
import com.arsylk.mammonsmite.domain.retrofit.RetrofitApiService
import com.arsylk.mammonsmite.domain.retrofit.RetrofitBannerService
import com.arsylk.mammonsmite.domain.sync.SyncService
import com.arsylk.mammonsmite.presentation.activity.main.MainViewModel
import com.arsylk.mammonsmite.presentation.dialog.file.picker.FilePickerViewModel
import com.arsylk.mammonsmite.presentation.dialog.pck.unpack.PckUnpackViewModel
import com.arsylk.mammonsmite.presentation.dialog.result.file.ResultFileViewModel
import com.arsylk.mammonsmite.presentation.screen.l2d.preview.L2DPreviewViewModel
import com.arsylk.mammonsmite.presentation.screen.pck.destinychild.PckDestinyChildViewModel
import com.arsylk.mammonsmite.presentation.screen.pck.unpacked.PckUnpackedViewModel
import com.arsylk.mammonsmite.presentation.fragment.settings.SettingsViewModel
import com.arsylk.mammonsmite.presentation.screen.home.HomeViewModel
import com.arsylk.mammonsmite.presentation.screen.locale.patch.LocalePatchViewModel
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalSerializationApi
@ExperimentalComposeUiApi
object KoinService {

    fun start(app: Application) {
        startKoin {
            androidLogger(level = Level.ERROR)
            androidContext(app)
            modules(
                module {
                    single { provideAppCoroutineScope() }
                    single { provideJson() }
                    single { provideOkHttpClient() }
                    single { provideRetrofitApiService(get(), get()) }
                    single { provideRetrofitBannerService(get()) }
                    single { provideAppPreferences(get()) }
                    single { provideSyncService(get(), get(), get()) }
                    single { provideCharacterRepository(get(), get(), get()) }
                    single { providePckTools(get()) }
                    single { provideL2DTools(get()) }
                },
                module {
                    viewModel { MainViewModel(get()) }
                    viewModel { HomeViewModel(get()) }
                    viewModel { PckDestinyChildViewModel(get(), get()) }
                    viewModel { SettingsViewModel(get()) }
                    viewModel { param -> FilePickerViewModel(type = param.get()) }
                    viewModel { param -> PckUnpackViewModel(get(), get(), get(), file = param.get()) }
                    viewModel { param -> L2DPreviewViewModel(get(), get(), file = param.get()) }
                    viewModel { PckUnpackedViewModel(get(), get(), get(), get()) }
                    viewModel { LocalePatchViewModel(get(), get(), get()) }
                    viewModel { param -> ResultFileViewModel(type = param.get(), startIn = param.getOrNull()) }
                }
            )
        }
    }

    private fun provideAppCoroutineScope() =
        CoroutineScope(SupervisorJob())

    private fun provideJson() =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
            encodeDefaults = true
        }

    private fun provideOkHttpClient() =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()

    private fun provideRetrofitApiService(json: Json, okHttpClient: OkHttpClient) =
        Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory(MediaType.get("application/json")))
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

    private fun provideSyncService(json: Json, apiService: RetrofitApiService, characterRepository: CharacterRepository) =
        SyncService(json, apiService, characterRepository)

    private fun provideCharacterRepository(scope: CoroutineScope, apiService: RetrofitApiService, json: Json) =
        CharacterRepository(scope, apiService, json)

    private fun providePckTools(json: Json) =
        PckTools(json)

    private fun provideL2DTools(json: Json) =
        L2DTools(json)
}

inline val KoinAndroidContext : Context
    get() = KoinJavaComponent.get<Context>(clazz = Context::class.java)