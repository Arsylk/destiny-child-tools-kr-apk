package com.arsylk.mammonsmite.domain.files

import com.arsylk.mammonsmite.model.destinychild.DestinyChildPackage
import java.io.File

object DestinyChildFiles {
    private val DEFAULT_PACKAGE = DestinyChildPackage.KOREA

    val filesFolder by lazy { getFilesFolder(DEFAULT_PACKAGE) }
    val modelsFolder by lazy { getModelsFolder(DEFAULT_PACKAGE) }
    val soundsFolder by lazy { getSoundsFolder(DEFAULT_PACKAGE) }
    val titleScreensFolder by lazy { getTitleScreensFolder(DEFAULT_PACKAGE) }
    val backgroundsFolder by lazy { getBackgroundsFolder(DEFAULT_PACKAGE) }
    val localeFile by lazy { getLocaleFile(DEFAULT_PACKAGE) }
    val modelInfoFile by lazy { getModelInfoFile(DEFAULT_PACKAGE) }


    fun getFilesFolder(pkg: DestinyChildPackage): File =
        File(CommonFiles.storage, "/Android/data/${pkg.pkg}/files")

    fun getModelsFolder(pkg: DestinyChildPackage): File =
        File(getFilesFolder(pkg), "/asset/character")

    fun getSoundsFolder(pkg: DestinyChildPackage): File =
        File(getFilesFolder(pkg), "/asset/sound/voice")

    fun getTitleScreensFolder(pkg: DestinyChildPackage): File =
        File(getFilesFolder(pkg), "/ux/title")

    fun getBackgroundsFolder(pkg: DestinyChildPackage): File =
        File(getFilesFolder(pkg), "/asset/scenario/image")

    fun getLocaleFile(pkg: DestinyChildPackage): File =
        File(getFilesFolder(pkg), "/locale.pck")

    fun getModelInfoFile(pkg: DestinyChildPackage): File =
        File(getModelsFolder(pkg), "/model_info.json")

}