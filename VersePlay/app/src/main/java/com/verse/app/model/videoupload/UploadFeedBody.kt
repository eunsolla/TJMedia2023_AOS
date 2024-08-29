package com.verse.app.model.videoupload

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadFeedBody(

    @SerialName("feedDesc")
    var feedDesc: String = "",                 //피드설명

    @SerialName("feedTag")
    var feedTag: String = "",                  //피드태그

    @SerialName("micVol")
    var micVol: String = "",                   //MIC볼륨값

    @SerialName("mrVol")
    var mrVol: String = "",                   //MR볼륨값

    @SerialName("orgFeedMngCd")
    var orgFeedMngCd: String = "",         //원작피드관리코드(듀엣 및 배틀 인 경우)

    @SerialName("secFinishTime")
    var secFinishTime: String = "",                //구간부르기종료시간

    @SerialName("secStartTime")
    var secStartTime: String = "",                 //구간부르기시작시간

    @SerialName("showContentsType")
    var showContentsType: String = "",     //콘텐츠노출설정코드(SH002 : 나만허용 / SH003 : 친구허용 / SH001 : 전체허용)

    @SerialName("singPoint")
    var singPoint: String = "",                //부르기점수

    @SerialName("songMngCd")
    var songMngCd: String = "",             //서비스음원관리코드

    @SerialName("useReply")
    var useReply: String = "",          //댓글허용여부

    @SerialName("singPartType")
    var singPartType: String = "",              // 부르기파트(SP001: A / SP002 : B / SP003 : T)

    @SerialName("contentsCode")
    var contentsCode: String = "",      //피드미디어유형코드(MD001 : 녹화 / MD002 : 녹음)

    @SerialName("version ")
    var version: String = "",

    @SerialName("singTpCd")
    var singTpCd: String = "",      //부르기유형코드(SI001 : 전체부르기 / SI002 : 구간부르기)

    @SerialName("paTpCd")
    var paTpCd: String = "",   //피드파트유형코드(PA001 : 솔로 / PA002 : 듀엣 / PA003 : 그룹 / PA004 : 배틀 / PA005 : 일반영상)

    @SerialName("mdTpCd")
    var mdTpCd: String = "",        //피드미디어유형코드(MD001 : 녹화 / MD002 : 녹음)

    @SerialName("thumbPicPath")
    val thumbPicPath: String = "", //썸네일이미지경로
    @SerialName("orgConPath")
    val orgConPath: String = "", //업로드콘텐츠경로
    @SerialName("highConPath")
    val highConPath: String = "", //하이라이트콘텐츠경로
    @SerialName("audioConPath")
    val audioConPath: String = "", //오디오콘텐츠경로
    @SerialName("feedMngCd")
    val feedMngCd: String = "", //피드관리코드
) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "UploadFeedBody"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        val asItem = diffUtil as UploadFeedBody
        return this == asItem
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        val asItem = diffUtil as UploadFeedBody
        return this == asItem
    }

}