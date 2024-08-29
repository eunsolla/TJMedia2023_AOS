package com.verse.app.model.param

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 댓글 param
 *
 * Created by jhlee on 2023-04-20
 */
@Deprecated("삭제 예정")
@ExperimentalSerializationApi
@Serializable
data class CommentCountParam(
    @SerialName("reqType")      //콘텐츠 유형(F:피드 / L:커뮤니티라운지 / V:커뮤니티투표)
    val reqType: String,
    @SerialName("contentsCode")     //콘텐츠관리코드(피드관리코드/라운지관리코드/투표관리코드)
    val contentsCode: String,
) {
    fun toMap(): MutableMap<String, String> = mutableMapOf(
        "reqType" to reqType,
        "contentsCode" to contentsCode,
    )
}