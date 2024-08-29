package com.verse.app.model.vote

import com.verse.app.contants.AppData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/19
 */
@Serializable
data class VoteDetailData(
    @SerialName("comVoteMngCd")
    val code: String = "",
    @SerialName("comVoteStDt")
    val startDt: String = "",
    @SerialName("comVoteFnDt")
    val endDt: String = "",
    @SerialName("stCd")
    val statusCode: String = "",
    @SerialName("comVoteTitle")
    val title: String = "",
    @SerialName("comVoteImgPath")
    val imagePath: String = "",
    @SerialName("joinVoteYn")
    var joinVoteYn: String = "",
    @SerialName("voteItem")
    val voteItem: String = "", // 이전에 투표 선택한 번호
    @SerialName("distItem1")
    val selectOne: String = "",
    @SerialName("distItem2")
    val selectTwo: String = "",
    @SerialName("distItem3")
    val selectThree: String = "",
    @SerialName("distItem4")
    val selectFour: String = ""
) {

    val isVote: Boolean
        get() = joinVoteYn == AppData.Y_VALUE
}