package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 좋아요 On/Off Body
 *
 * Created by jhlee on 2023-04-20
 */
@Serializable
data class LikeBody(
    @SerialName("likeType")         //좋아요 유형(F:일반피드 / C:댓글 / R:답글)
    val likeType: String,
    @SerialName("likeYn")            //좋아요 설정/해제(Y:설정 / N:해제)
    val likeYn: String,
    @SerialName("contentsCode") //피드관리코드(배틀) or 댓글관리코드(답글)
    val contentsCode: String,
)