package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 피드 정보 업데이트
 *
 * Created by jhlee on 2023-06-05
 */
@Serializable
data class FeedUpdateBody(
    @SerialName("feedMngCd")        // 피드관리코드
    val feedMngCd: String,
    @SerialName("reqType")     //요청 유형(PR : 비공개 / PF : 친구만공개 / PA : 전체공개)
    val reqType: String? = null,
)
