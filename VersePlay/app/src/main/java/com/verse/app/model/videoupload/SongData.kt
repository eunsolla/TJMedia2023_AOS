package com.verse.app.model.videoupload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 반주음 데이터
 *
 * Created by esna on 2023-04-23
 */
@Serializable
data class SongData(
    @SerialName("songMngCd")
    val songMngCd: String = "",                       // 서비스음원관리코드
    @SerialName("songId")
    val songId: String = "",                          // 서비스음원곡아이디
    @SerialName("songNm")
    val songNm: String = "",                          // 노래제목
    @SerialName("genreCd")
    val genreCd: String = "",                         // 장르코드
    @SerialName("genreNm")
    val genreNm: String = "",                         // 장르명
    @SerialName("sdCtgTpCd")
    val sdCtgTpCd: String = "",                       // 반주음원유형코드
    @SerialName("artNm")
    val artNm: String = "",                           // 아티스트명
    @SerialName("albImgPath")
    val albImgPath: String = "",                      // 앨범아트이미지파일경로
)