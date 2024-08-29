package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 좋아요 On/Off Body
 *
 * Created by jhlee on 2023-04-20
 */
@Serializable
data class BookMarkBody(
    @SerialName("bookMarkType")     //북마크 유형(F:피드 / S:반주음)
    val bookMarkType: String,
    @SerialName("contentsCode")     //북마크 대상 콘텐츠 관리코드(피드관리코드 혹은 반주음관리코드)
    val contentsCode: String,
    @SerialName("bookMarkYn")     //북마크 설정/해제(Y:설정 / N:해제)
    val bookMarkYn: String,
)