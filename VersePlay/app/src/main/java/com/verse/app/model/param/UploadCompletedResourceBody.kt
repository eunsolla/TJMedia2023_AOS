package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 이미지 업로드 완료 처리 Body
 *
 * Created by esna on 2023-05-14
 */
@Serializable
data class UploadCompletedResourceBody(
    @SerialName("attImgCount")
    val attImgCount: String = "",
    @SerialName("attImgListNum")
    val attImgListNum: String = "",
    @SerialName("attImgPath")
    val attImgPath: String = "",
    @SerialName("attImgPathList")
    val attImagePathList: List<String> = listOf(),
    @SerialName("csMngCd")
    val csMngCd: String = "",
    @SerialName("louMngCd")
    val louMngCd: String = "",
    @SerialName("pfBgImgPath")
    val pfBgImgPath: String = "",
    @SerialName("pfFrImgPath")
    val pfFrImgPath: String = "",
    @SerialName("resType")
    val resType: String = "",
)