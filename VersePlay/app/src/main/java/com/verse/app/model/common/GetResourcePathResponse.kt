package com.verse.app.model.common

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetResourcePathResponse(
    @SerialName("result")
    val result: GetResourcePathInfo = GetResourcePathInfo(),
) : BaseResponse()

@Serializable
data class GetResourcePathInfo(
    @SerialName("pfFrImgPath")                                 //프로필 Front 이미지 경로
    var pfFrImgPath: String = "",

    @SerialName("pfBgImgPath")                                //프로필 배경 이미지 경로
    var pfBgImgPath: String = "",

    @SerialName("csMngCd")                                 //1대1문의관리코드
    var csMngCd: String = "",

    @SerialName("attImgPath")                                //첨부파일이미지경로
    var attImgPath: String = "",

    @SerialName("louMngCd")                                 //라운지글관리코드
    val louMngCd: String = "",

    @SerialName("attImgPathList")
    val attImgPathList: List<String> = listOf(), // 라운지 이미지 경로

    @SerialName("feedMngCd")                     //피드관리코드
    val feedMngCd: String = "",

    @SerialName("thumbPicPath")                  //썸네일이미지경로
    val thumbPicPath: String = "",

    @SerialName("orgConPath")                     //업로드콘텐츠경로
    val orgConPath: String = "",

    @SerialName("highConPath")                     //하이라이트콘텐츠경로
    val highConPath: String = "",

    @SerialName("audioConPath")                 //오디오콘텐츠경로
    val audioConPath: String = "",

    ) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "GetResourcePathInfo"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetResourcePathInfo
        return this.pfFrImgPath == asItem.pfFrImgPath
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetResourcePathInfo
        return this == asItem
    }

}
