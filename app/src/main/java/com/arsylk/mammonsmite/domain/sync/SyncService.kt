package com.arsylk.mammonsmite.domain.sync

import com.arsylk.mammonsmite.domain.destinychild.CharacterRepository
import com.arsylk.mammonsmite.domain.destinychild.EngLocaleRepository
import com.arsylk.mammonsmite.domain.destinychild.LevelEquationParser
import com.arsylk.mammonsmite.domain.destinychild.SkillBuffRepository
import com.arsylk.mammonsmite.domain.retrofit.RetrofitApiService
import com.arsylk.mammonsmite.domain.sync.SyncBuilder.sync
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

class SyncService(
    private val json: Json,
    private val apiService: RetrofitApiService,
    private val engLocaleRepository: EngLocaleRepository,
    private val skillBuffRepository: SkillBuffRepository,
    private val characterRepository: CharacterRepository,
) {

    fun getSyncFlow(): Flow<Pair<ProgressStore, Int>> {
        return prepareSyncFlow()
            .onStart { _isLoading.value = true }
            .onCompletion { _isLoading.value = false }
    }

    private fun prepareSyncFlow(): Flow<Pair<ProgressStore, Int>> {
        val sync = sync {
            group {
                tag = "eng locale repo"

                module {
                    tag = "eng locale data"
                    required = false
                    action {
                        val response = apiService.getEnglishPatch()
                        engLocaleRepository.setEngLocale(response)
                    }
                }
            }

            group {
                tag = "skill buff repo"
                concurrency = 5

                module {
                    tag = "skill active data"
                    required = false
                    action {
                        val response = apiService.getSkillActiveDataFile()
                        skillBuffRepository.setSkillActiveData(response)
                    }
                }

                module {
                    tag = "skill buff data"
                    required = false
                    action {
                        val response = apiService.getSkillBuffDataFile()
                        skillBuffRepository.setSkillBuffData(response)
                    }
                }

                module {
                    tag = "skill category data"
                    required = false
                    action {
                        val response = apiService.getSkillCategoryFile()
                        skillBuffRepository.setSkillCategoryData(response)
                    }
                }

                module {
                    tag = "skill equations data"
                    required = false
                    action {
                        val response = apiService.getSkillLevelEquationsFile()
                        LevelEquationParser.equations = response
                    }
                }
            }

            group {
                tag = "character repo"
                concurrency = 5

                module {
                    tag = "char data"
                    required = false
                    action {
                        val response = apiService.getCharDataFile()
                        characterRepository.setCharData(response)
                    }
                }

                module {
                    tag = "character skin data"
                    required = false
                    action {
                        val response = apiService.getCharacterSkinDataFile()
                        characterRepository.setCharacterSkinData(response)
                    }
                }

                module {
                    tag = "ignition character skill data"
                    required = false
                    action {
                        val response = apiService.getIgnitionCharacterSkillDataFile()
                        characterRepository.setIgnitionCharacterSkillData(response)
                    }
                }
            }

//            group {
//                tag = "locale group"
//                required = true
//
//                cacheableModule<LocalePatch> {
//                    tag = "english patch module"
//                    file = CommonFiles.Internal.englishPatchFile
//                    fetch { service.getEnglishPatch(it) }
//                    serialize { json.encodeToString(it) }
//                    deserialize { json.decodeFromString(it) }
//                }
//            }
        }
        return SyncBuilder.execute(sync)
    }

    companion object {
        private val _isLoading = MutableStateFlow(false)
        val isLoading by lazy(_isLoading::asStateFlow)
    }
}