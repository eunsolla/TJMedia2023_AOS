package com.verse.app.model.search

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SearchResultVideoData(
    @SerialName("feedMngCd")
    var feedMngCd: String = "",
    @SerialName("paTpCd")
    var paTpCd: String = "",
    @SerialName("mdTpCd")
    var mdTpCd: String = "",
    @SerialName("singTpCd")
    var singTpCd: String = "",
    @SerialName("songMngCd")
    var songMngCd: String = "",
    @SerialName("songId")
    var songId: String = "",
    @SerialName("ownerMemCd")
    var ownerMemCd: String = "",
    @SerialName("ownerMemNk")
    var ownerMemNk: String = "",
    @SerialName("thumbPicPath")
    var thumbPicPath: String = "",
    @SerialName("orgConPath")
    var orgConPath: String = "",
    @SerialName("highConPath")
    var highConPath: String = "",
    @SerialName("audioConPath")
    var audioConPath: String = "",
    @SerialName("fgHighlightYn")
    var fgHighlightYn: String = "",
    @SerialName("orgFeedMngCd")
    var orgFeedMngCd: String = "",
    @SerialName("stBattleDt")
    var stBattleDt: String = "",
    @SerialName("fnBattleDt")
    var fnBattleDt: String = "",
    @SerialName("baStCd")
    var baStCd: String = "",
    @SerialName("secStartTime")
    var secStartTime: String = "",
    @SerialName("secEndTime")
    var secEndTime: String = "",
    @SerialName("singPart")
    var singPart: String = "",
    @SerialName("singScore")
    var singScore: String = "",
    @SerialName("feedNote")
    var feedNote: String = "",
    @SerialName("feedTag")
    var feedTag: String = "",
    @SerialName("hitCnt")
    var hitCnt: String = "",
    @SerialName("fgAplRepYn")
    var fgAplRepYn: String = "",
    @SerialName("exposCd")
    var exposCd: String = "",
    @SerialName("fgLikeYn")
    var fgLikeYn: String = "",
    @SerialName("fgBookMarkYn")
    var fgBookMarkYn: String = "",
    @SerialName("fgFollowYn")
    var fgFollowYn: String = "",
    @SerialName("replyCount")
    var replyCount: String = "",
    @SerialName("likeCount")
    var likeCount: String = "",
    @SerialName("songNm")
    var songNm: String = "",
    @SerialName("artNm")
    var artNm: String = "",
    @SerialName("albImgPath")
    var albImgPath: String = ""
) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "SearchResultFeedData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is SearchResultVideoData) {
            feedMngCd == diffUtil.feedMngCd
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is SearchResultVideoData) {
            this == diffUtil
        } else {
            false
        }
    }
}