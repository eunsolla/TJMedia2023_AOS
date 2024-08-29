package com.verse.app.model.singpass

import com.google.common.collect.Lists
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SingPassResponse(
    val result: SingPassData = SingPassData(),
) : BaseResponse()

@Serializable
data class SingPassData(
    @SerialName("seasonInfo")
    val seasonInfo: SeasonInfo = SeasonInfo(),
    @SerialName("genreList")
    val genreList: MutableList<GenreList> = mutableListOf()
)

@Serializable
data class SeasonInfo(
    @SerialName("svcSeaMngCd")
    val svcSeaMngCd: String = "",
    @SerialName("svcSeaMngNm")
    val svcSeaMngNm: String = "",
    @SerialName("svcStDt")
    val svcStDt: String = "0",
    @SerialName("svcFnDt")
    val svcFnDt: String = "0",
    @SerialName("seaLevelNm")
    val seaLevelNm: String = "LEVEL 01",
    @SerialName("genrePageCnt")
    val genrePageCnt: Int = 0,
    @SerialName("seaRemainDate")
    val seaRemainDate: String = "0",
    @SerialName("seaRemainTime")
    val seaRemainTime: Int = 0,
    @SerialName("svcStndDt")
    val svcStndDt: String = "",
    @SerialName("clrItemPicPath")
    val clrItemPicPath: String = "",
    @SerialName("norSkCnt")
    val norSkCnt: String = "0",
    @SerialName("seaSkCnt")
    val seaSkCnt: String = "0",
    @SerialName("topItemPicPath")
    val topItemPicPath: String = "",
    @SerialName("fgVipYn")
    val fgVipYn: String = "N",

    ) {
    fun getTopMissionItem(index: Int): String {
        var topItemImageList: List<String> = listOf()
        var result: String = ""

        if (topItemPicPath.isNotEmpty()) {
            topItemImageList = topItemPicPath.split(",")
        }

        if (topItemImageList.size > index) {
            result = topItemImageList.get(index)
        }
        return result
    }
}

@Serializable
data class GenreList(
    @SerialName("genreCd")
    val genreCd: String = "",
    @SerialName("genreNm")
    val genreNm: String = "",
    @SerialName("memberRanking")
    val memberRanking: MemberRanking? = null,
    @SerialName("genreRankingList")
    val genreRankingList: MutableList<GenreRankingList> = mutableListOf(),
) : BaseModel() {

    var defaultThumbPicPath: String? = ""
        get() {
            if (genreRankingList.isEmpty()) {
                return ""
            }
            val range = (0 until genreRankingList.size)
            val index = range.random()
            return genreRankingList[index].highConPath
        }
        set(value) {
            field = value
        }
    var videoList: List<GenreRankingList>? = null
        get() {
            val dataList = genreRankingList?.let {
                return Lists.partition(it, 5)[0]
            }
            return dataList
        }

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "GenreList"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GenreList
        return this.genreCd == asItem.genreCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GenreList
        return this == asItem
    }
}

@Serializable
data class MemberRanking(
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
    @SerialName("memStCd")
    val memStCd: String = "",
    @SerialName("memTpCd")
    val memTpCd: String = "",
    @SerialName("subscTpCd")
    val subscTpCd: String = "",
) {
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

@Serializable
data class GenreRankingList(
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
    @SerialName("feedMngCd")
    val feedMngCd: String = "",
    @SerialName("orgConPath")
    val orgConPath: String = "",
    @SerialName("highConPath")
    val highConPath: String = "",
    @SerialName("thumbPicPath")
    val thumbPicPath: String = "",
) : BaseModel() {

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

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "GenreRankingList"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GenreRankingList
        return this.genreCd == asItem.genreCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GenreRankingList
        return this == asItem
    }
}
