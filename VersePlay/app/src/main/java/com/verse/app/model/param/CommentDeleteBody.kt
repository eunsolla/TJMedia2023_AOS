package com.verse.app.model.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 댓글 / 답글 삭제  param
 *
 * Created by jhlee on 2023-04-20
 */
@Serializable
data class CommentDeleteBody(
    @SerialName("commentType")    //콘텐츠 유형(F:피드 / L:커뮤니티라운지 / V:커뮤니티투표)
    val commentType: String,
    @SerialName("contentsCode")     //콘텐츠관리코드(피드관리코드/라운지관리코드/투표관리코드)
    val contentsCode: String,
    @SerialName("commentCd")      // 댓글 관리 코드
    val commentCd: String = "",
    @SerialName("replyCd")      // 답글 관리 코드
    val replyCd: String = "",
    @SerialName("commentMngCd")      // 상위 댓글  관리 코드
    val commentMngCd: String = "",
)