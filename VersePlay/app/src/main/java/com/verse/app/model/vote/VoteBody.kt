package com.verse.app.model.vote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/24
 */
@Serializable
data class VoteBody(
    @SerialName("comVoteMngCd")
    val code: String = "",
    @SerialName("voteIndex")
    val selectedPos: String = ""
)
