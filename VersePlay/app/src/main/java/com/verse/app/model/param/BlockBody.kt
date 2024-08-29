package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 차단  On/Off Body
 *
 * Created by jhlee on 2023-04-20
 */
@Serializable
data class BlockBody(
    @SerialName("blockContentCode")     //차단유형 별 콘텐츠 관리코드(회원관리코드)
    val blockContentCode: String,
    @SerialName("blockYn")                   //차단 설정/해제 (Y:차단 / N:해제)
    val blockYn: String,
    @SerialName("blockType")                 //차단 유형 코드 (BK001 : 사용자차단 / BK002 : 피드콘텐츠차단 / BK003 : 커뮤니티콘텐츠차단)
    val blockType: String,
)
