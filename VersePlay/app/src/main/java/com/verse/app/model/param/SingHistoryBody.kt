package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 노래 부르기 이력 적재 Body
 *
 * Created by jhlee on 2023-05-31
 */
@Serializable
data class SingHistoryBody(
    @SerialName("songMngCd")     //음원관리코드
    val songMngCd: String,
    @SerialName("paTpCd")
    val paTpCd: String = "",                      // 피드파트유형코드(PA001:솔로/PA002:듀엣/PA003:그룹/PA004:배틀/PA005:일반영상/PA006:광고)
    @SerialName("mdTpCd")
    val mdTpCd: String = "",                      // 피드미디어유형코드(녹화/녹음)
    @SerialName("fgSingPassYn")
    val fgSingPassYn: String = "",               // 전곡: N or 씽패스로 도전하기 : Y
)
