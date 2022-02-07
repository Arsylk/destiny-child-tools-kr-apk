package com.arsylk.mammonsmite.domain.koin

import android.app.Application
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.room.RoomDatabase
import com.arsylk.mammonsmite.Cfg
import com.arsylk.mammonsmite.domain.db.AppDatabase
import com.arsylk.mammonsmite.domain.db.converter.ViewIdxConverter
import com.arsylk.mammonsmite.domain.live2d.L2DTools
import com.arsylk.mammonsmite.domain.pck.PckTools
import com.arsylk.mammonsmite.domain.prefs.AppPreferences
import com.arsylk.mammonsmite.domain.destinychild.CharacterRepository
import com.arsylk.mammonsmite.domain.destinychild.EngLocaleRepository
import com.arsylk.mammonsmite.domain.destinychild.ItemRepository
import com.arsylk.mammonsmite.domain.destinychild.SkillBuffRepository
import com.arsylk.mammonsmite.domain.retrofit.JsoupConverterFactory
import com.arsylk.mammonsmite.domain.retrofit.RetrofitApiService
import com.arsylk.mammonsmite.domain.retrofit.RetrofitBannerService
import com.arsylk.mammonsmite.domain.sync.SyncService
import com.arsylk.mammonsmite.presentation.activity.main.MainViewModel
import com.arsylk.mammonsmite.presentation.dialog.pck.pack.PckPackViewModel
import com.arsylk.mammonsmite.presentation.dialog.pck.unpack.PckUnpackViewModel
import com.arsylk.mammonsmite.presentation.dialog.result.file.ResultFileViewModel
import com.arsylk.mammonsmite.presentation.dialog.result.unpacked.ResultUnpackedViewModel
import com.arsylk.mammonsmite.presentation.dialog.wiki.buff.WikiBuffViewModel
import com.arsylk.mammonsmite.presentation.dialog.wiki.skill.WikiSkillViewModel
import com.arsylk.mammonsmite.presentation.screen.l2d.preview.L2DPreviewViewModel
import com.arsylk.mammonsmite.presentation.screen.pck.destinychild.PckDestinyChildViewModel
import com.arsylk.mammonsmite.presentation.screen.pck.unpacked.PckUnpackedViewModel
import com.arsylk.mammonsmite.presentation.screen.home.HomeViewModel
import com.arsylk.mammonsmite.presentation.screen.locale.patch.LocalePatchViewModel
import com.arsylk.mammonsmite.presentation.screen.pck.swap.PckSwapViewModel
import com.arsylk.mammonsmite.presentation.screen.settings.SettingsViewModel
import com.arsylk.mammonsmite.presentation.screen.wiki.character.WikiCharacterViewModel
import com.arsylk.mammonsmite.presentation.screen.wiki.items.WikiItemsViewModel
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
                    single { provideAppDatabase(get()) }
                    single { provideAppPreferences(get()) }
                    single { provideSyncService(get(), get(), get(), get(), get(), get()) }
                    single { provideEngLocaleRepository() }
                    single { provideSkillBuffRepository(get(), get()) }
                    single { provideCharacterRepository(get(), get(), get()) }
                    single { provideItemRepository(get(), get(), get()) }
                    single { providePckTools(get()) }
                    single { provideL2DTools(get()) }
                },
                module {
                    viewModel { MainViewModel(get()) }
                    viewModel { HomeViewModel(get()) }
                    viewModel { (handle: SavedStateHandle) -> PckDestinyChildViewModel(get(), get(), get(), handle = handle) }
                    viewModel { param -> PckPackViewModel(get(), file = param.get()) }
                    viewModel { param -> PckUnpackViewModel(get(), get(), get(), file = param.get()) }
                    viewModel { param -> L2DPreviewViewModel(get(), get(), file = param.get()) }
                    viewModel { PckUnpackedViewModel(get(), get(), get(), get()) }
                    viewModel { LocalePatchViewModel(get(), get(), get()) }
                    viewModel { param -> ResultFileViewModel(type = param.get()) }
                    viewModel { SettingsViewModel(get()) }
                    viewModel { PckSwapViewModel(get(), get(), get()) }
                    viewModel { ResultUnpackedViewModel(get(), get()) }
                    viewModel { param -> WikiCharacterViewModel(get(), idx = param.get()) }
                    viewModel { param -> WikiBuffViewModel(get(), idx = param.get()) }
                    viewModel { param -> WikiSkillViewModel(get(), get(), idx = param.get()) }
                    viewModel { WikiItemsViewModel(get()) }
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

    private fun provideAppDatabase(context: Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, "database.db")
            .fallbackToDestructiveMigration()
            .addTypeConverter(ViewIdxConverter())
            .build()

    private fun provideAppPreferences(context: Context) =
        AppPreferences(context)

    private fun provideSyncService(
        json: Json,
        apiService: RetrofitApiService,
        database: AppDatabase,
        engLocaleRepository: EngLocaleRepository,
        skillBuffRepository: SkillBuffRepository,
        characterRepository: CharacterRepository,
    ) = SyncService(json, apiService, database, engLocaleRepository, skillBuffRepository, characterRepository)

    private fun provideEngLocaleRepository() =
        EngLocaleRepository()

    private fun provideSkillBuffRepository(scope: CoroutineScope, repo: EngLocaleRepository) =
        SkillBuffRepository(scope, repo)

    private fun provideCharacterRepository(
        scope: CoroutineScope,
        engLocaleRepository: EngLocaleRepository,
        skillBuffRepository: SkillBuffRepository,
    ) = CharacterRepository(scope, engLocaleRepository, skillBuffRepository)

    private fun provideItemRepository(
        scope: CoroutineScope,
        database: AppDatabase,
        engLocaleRepository: EngLocaleRepository,
    ) = ItemRepository(scope, database, engLocaleRepository)

    private fun providePckTools(json: Json) =
        PckTools(json)

    private fun provideL2DTools(json: Json) =
        L2DTools(json)
}

inline val KoinAndroidContext : Context
    get() = KoinJavaComponent.get<Context>(clazz = Context::class.java)