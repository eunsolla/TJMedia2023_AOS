package com.verse.app.model.param

import com.verse.app.model.base.BasePaging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 댓글 param
 *
 * Created by jhlee on 2023-04-20
 */
@ExperimentalSerializationApi
@Serializable
data class CommentParam(
    @SerialName("commentType")      //콘텐츠 유형(F:피드 / L:커뮤니티라운지 / V:커뮤니티투표)
    var commentType: String = "",
    @SerialName("contentsCode")      //콘텐츠관리코드(피드관리코드/라운지관리코드/투표관리코드)
    var contentsCode: String= "",
    @SerialName("commentMngCd")     //댓글관리코드
    var commentMngCd: String= "",
) :BasePaging (){
    fun toMap(): MutableMap<String, Any> = mutableMapOf(
        "commentType" to commentType,
        "contentsCode" to contentsCode,
        "commentMngCd" to commentMngCd,
        "pageNum" to pageNum
    )
}