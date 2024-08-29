package com.verse.app.model.community

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 라운지 콘텐츠 등록
 *
 * Created by juhongmin on 2023/05/16
 */
@Serializable
data class CommunityLoungeCreateResponse(
    val result: Result = Result()
) : BaseResponse() {

    @Serializable
    data class Result(
        @SerialName("louMngCd")
        val code: String = "",
        @SerialName("louPicPath1")
        val uploadPath1: String = "",
        @SerialName("louPicPath2")
        val uploadPath2: String = "",
        @SerialName("louPicPath3")
        val uploadPath3: String = "",
        @SerialName("louPicPath4")
        val uploadPath4: String = "",
        @SerialName("louPicPath5")
        val uploadPath5: String = "",
        @SerialName("louPicPath6")
        val uploadPath6: String = "",
        @SerialName("louPicPath7")
        val uploadPath7: String = "",
        @SerialName("louPicPath8")
        val uploadPath8: String = "",
        @SerialName("louPicPath9")
        val uploadPath9: String = "",
        @SerialName("louPicPath10")
        val uploadPath10: String = ""
    )
}