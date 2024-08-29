package com.verse.app.model.search

import com.verse.app.contants.AppData
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResultSongData(
    @SerialName("songMngCd")
    var songMngCd: String = "",
    @SerialName("songId")
    var songId: String = "",
    @SerialName("songNm")
    var songNm: String = "",
    @SerialName("totTime")
    var totTime: String = "",
    @SerialName("hiTime")
    var hiTime: String = "",
    @SerialName("genreCd")
    var genreCd: String = "",
    @SerialName("saleDt")
    var saleDt: String = "",
    @SerialName("svcStCd")
    var svcStCd: String = "",
    @SerialName("svcStUpdDt")
    var svcStUpdDt: String = "",
    @SerialName("cprCd")
    var cprCd: String = "",
    @SerialName("norSdScPath")
    var norSdScPath: String = "",
    @SerialName("sdScPath")
    var sdScPath: String = "",
    @SerialName("sdCtgTpCd")
    var sdCtgTpCd: String = "",
    @SerialName("subCon")
    var subCon: String = "",
    @SerialName("orgSubPath")
    var orgSubPath: String = "",
    @SerialName("relKwd")
    var relKwd: String = "",
    @SerialName("albNm")
    var albNm: String = "",
    @SerialName("albDistNm")
    var albDistNm: String = "",
    @SerialName("albDivNm")
    var albDivNm: String = "",
    @SerialName("albRelNtNm")
    var albRelNtNm: String = "",
    @SerialName("artNm")
    var artNm: String = "",
    @SerialName("artTpCd")
    var artTpCd: String = "",
    @SerialName("artDesc")
    var artDesc: String = "",
    @SerialName("artRetDt")
    var artRetDt: String = "",
    @SerialName("artDebDt")
    var artDebDt: String = "",
    @SerialName("artFandomNm")
    var artFandomNm: String = "",
    @SerialName("artFandomImgPath")
    var artFandomImgPath: String = "",
    @SerialName("artFandomColNm")
    var artFandomColNm: String = "",
    @SerialName("artFandomColPath")
    var artFandomColPath: String = "",
    @SerialName("singCnt")
    var singCnt: String = "",
    @SerialName("hitCnt")
    var hitCnt: String = "",
    @SerialName("artNtNm")
    var artNtNm: String = "",
    @SerialName("artBirthday")
    var artBirthday: String = "",
    @SerialName("artImgPath")
    var artImgPath: String = "",
    @SerialName("albImgPath")
    var albImgPath: String = "",
    @SerialName("jsonSubPath")
    var jsonSubPath: String = "",
    @SerialName("songCtgTpCd1")
    var songCtgTpCd1: String = "",
    @SerialName("songCtgTpCd2")
    var songCtgTpCd2: String = "",
    @SerialName("fgSoloYn")
    var fgSoloYn: String = "",
    @SerialName("fgDuetYn")
    var fgDuetYn: String = "",
    @SerialName("fgGroupYn")
    var fgGroupYn: String = "",
    @SerialName("fgBattleYn")
    var fgBattleYn: String = "",
    @SerialName("apvStCd")
    var apvStCd: String = "",
    @SerialName("svcNtCd")
    var svcNtCd: String = "",
    @SerialName("showPr")
    var showPr: String = "",
    @SerialName("fgBookMarkYn")
    var fgBookMarkYn: String = ""
) : BaseModel() {

    //즐겨찾기
    var isBookMark: Boolean = false
        get() {
            return fgBookMarkYn == AppData.Y_VALUE
        }
        set(value) {
            fgBookMarkYn = if (value) {
                AppData.Y_VALUE
            } else {
                AppData.N_VALUE
            }
            field = value
        }

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "SearchResultSongData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is SearchResultPopSongData) {
            songId == diffUtil.songId
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is SearchResultPopSongData) {
            this == diffUtil
        } else {
            false
        }
    }
}