package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 유형별 방주음 목록 조회
 *
 * Created by jhlee on 2023/05/21
 */
@Serializable
class SongListBody(
    @SerialName("reqTypeCd")
    val reqTypeCd: String,               //반주음 조회 유형코드(G:장르별 / P:인기곡 / N:신곡 / R:연관음원곡)
    @SerialName("genreCd")
    val genreCd: String,                     //장르관리코드 (장르별 반주음 조회 시만 사용)
    @SerialName("songMngCd")
    val songMngCd: String,              //음원관리코드 (연관음원곡 조회 시만 사용)
    @SerialName("pageNum")
    var pageNum: Int,                  //요청페이지수
    @SerialName("sortType")
    var sortType: String,                //정렬 유형(ASC / DESC)
)