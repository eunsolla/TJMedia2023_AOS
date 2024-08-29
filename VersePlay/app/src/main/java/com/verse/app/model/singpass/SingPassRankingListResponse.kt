package com.verse.app.model.singpass

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SingPassRankingListResponse(
    val result: SingPassRankingData = SingPassRankingData(),
) : BaseResponse()

@Serializable
data class SingPassRankingData(
    @SerialName("genreRankingList")
    var singPassRankingInfo: MutableList<SingPassRankingInfo> = mutableListOf(),
    @SerialName("memberRanking")
    val memberRanking: MemberRanking? = null,
) : BasePaging()

@Serializable
data class SingPassRankingInfo(
    @SerialName("ranking")
    val ranking: String = "",
    @SerialName("memCd")
    val memCd: String = "",
    @SerialName("genreCd")
    val genreCd: String = "",
    @SerialName("memNk")
    val memNk: String = "",
    @SerialName("genreNm")
    val genreNm: String = "",
    @SerialName("singPoint")
    val singPoint: Int = 0,
    @SerialName("pfFrImgPath")
    val pfFrImgPath: String = "",
    @SerialName("pfBgImgPah")
    val pfBgImgPah: String = "",
    @SerialName("topItemPicPath")
    val topItemPicPath: String = "",
) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "SingPassRankingInfo"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as SingPassRankingInfo
        return this.memCd == asItem.memCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as SingPassRankingInfo
        return this == asItem
    }

    fun getTopMissionItem(index: Int): String {
        var topItemImageList: List<String> = listOf()
        var result: String = ""

        if (!topItemPicPath.isNullOrEmpty()) {
            topItemImageList = topItemPicPath.split(",")
        }

        if (topItemImageList.size > index) {
            result = topItemImageList.get(index)
        }

        return result
    }
}
