package com.verse.app.model.videoupload

import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadFeedResponse(

    @SerialName("result")
    var result: UploadFeedData,

) : BaseResponse()

@Serializable
data class UploadFeedData(

    @SerialName("feedMngCd")
    var feedMngCd: String = "",

    @SerialName("thumbPicPath")
    var thumbPicPath: String = "",

    @SerialName("orgConPath")
    var orgConPath: String = "",

    @SerialName("highConPath")
    var highConPath: String = "",

    @SerialName("audioConPath")
    var audioConPath: String = ""

)