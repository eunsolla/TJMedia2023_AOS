package com.verse.app.model.feed

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 즐겨찾기 반주음
 *
 * Created by esna on 2023-05-05
 */

@Serializable
data class CommonAccompanimentData(
    @SerialName("songMngCd")
    val songMngCd: String = "",                     // 서비스음원관리코드
    @SerialName("songId")
    val songId: String = "",                        // 서비스음원곡아이디
    @SerialName("songNm")
    val songNm: String = "",                        // 음원명
    @SerialName("genreCd")
    val genreCd: String = "",                       // 장르코드
    @SerialName("genreNm")
    val genreNm: String = "",                       // 장르명
    @SerialName("sdCtgTpCd")
    val sdCtgTpCd: String = "",                     // 반주음원유형코드
    @SerialName("artNm")
    val artNm: String = "",                         // 아티스트명
    @SerialName("albImgPath")
    val albImgPath: String = "",                    // 앨범아트이미지파일경로

) : BaseModel() {

    //즐겨찾기
    var isBookMark: Boolean = true

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "CommonAccompanimentData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as CommonAccompanimentData
        return this.songMngCd == asItem.songMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as CommonAccompanimentData
        return this == asItem
    }
}