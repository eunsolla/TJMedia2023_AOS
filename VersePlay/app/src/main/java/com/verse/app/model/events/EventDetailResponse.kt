package com.verse.app.model.events

import com.verse.app.model.base.BaseResponse
import com.verse.app.model.community.CommunityEventData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/19
 */
@Serializable
data class EventDetailResponse(
    @SerialName("result")
    val result: EventDetail = EventDetail()
) : BaseResponse() {
    @Serializable
    data class EventDetail(
        @SerialName("evtMngCd")
        val code: String = "",
        @SerialName("svcNtCd")
        val svcNtCd: String = "",
        @SerialName("evtTitle")
        val title: String = "",
        @SerialName("stCd")
        val statusCode: String = "",
        @SerialName("thumbPicPath")
        val thumbnailImageUrl: String = "",
        @SerialName("evtPicPath")
        val imageUrl: String = "",
        @SerialName("evtStDt")
        val startDt: String = "",
        @SerialName("evtFnDt")
        val endDate: String = "",
        @SerialName("linkCd")
        val linkCode: String = "",
        @SerialName("linkUrl")
        val linkUrl: String = ""
    )
}
