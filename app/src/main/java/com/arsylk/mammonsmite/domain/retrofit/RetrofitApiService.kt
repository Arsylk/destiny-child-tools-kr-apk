package com.arsylk.mammonsmite.domain.retrofit


import com.arsylk.mammonsmite.model.api.response.CharacterSkinDataFileResponse
import com.arsylk.mammonsmite.model.api.response.ItemDataResponse
import com.arsylk.mammonsmite.model.destinychild.*
import retrofit2.http.*

interface RetrofitApiService {

    @GET("/api/get_apk_version")
    suspend fun getApkVersion()

    @GET("/api/get_models")
    suspend fun getModels(@Query("offset") offset: Int)

    @GET("/api/get_model_file/{id}")
    suspend fun getModelFile(@Path("id") id: Int)

    @GET("/api/get_model_preview/{id}")
    suspend fun getModelPreview(@Path("id") id: Int)

    @GET("/api/get_english_patch/{md5}")
    suspend fun getEnglishPatch(@Path("md5") md5: String = ""): LocalePatch

    @GET("/api/get_russian_patch/{md5}")
    suspend fun getRussianPatch(@Path("md5") md5: String = ""):  LocalePatch

    @GET("/api/get_file/{name}/{md5}")
    suspend fun getDumpFile(@Path("name") name: String, @Path("md5") md5: String)

    @GET("/api/get_file/CHAR_DATA/")
    suspend fun getCharDataFile(): Map<String, CharData>

    @GET("/api/get_file/CHARACTER_SKIN_DATA/")
    suspend fun getCharacterSkinDataFile(): CharacterSkinDataFileResponse

    @GET("/api/get_file/SKILL_ACTIVE_DATA/")
    suspend fun getSkillActiveDataFile(): Map<String, SkillActiveData>

    @GET("/api/get_file/SKILL_BUFF_DATA/")
    suspend fun getSkillBuffDataFile(): Map<String, SkillBuffData>

    @GET("/api/get_file/IGNITION_CHARACTER_SKILL_DATA/")
    suspend fun getIgnitionCharacterSkillDataFile(): IgnitionCharacterSkills

    @GET("/api/get_file/SKILL_LEVEL_EQUATIONS/")
    suspend fun getSkillLevelEquationsFile(): Map<Int, String>

    @GET("/api/get_file/SKILL_CATEGORY/")
    suspend fun getSkillCategoryFile(): Map<String, Int>

    @GET("/api/get_file/ITEM_DATA")
    suspend fun getItemDataFile(): ItemDataResponse

    @GET("/api/get_children_skills")
    suspend fun getChildrenSkills(@Query("md5") md5: String)

    @GET("/api/get_equipment_stats")
    suspend fun getEquipmentStats(@Query("md5") md5: String)

    @GET("/api/get_soul_carta")
    suspend fun getSoulCarta(@Query("md5") md5: String)

    @POST("/api/post_locale_patch")
    suspend fun sendLocalePatch()
}