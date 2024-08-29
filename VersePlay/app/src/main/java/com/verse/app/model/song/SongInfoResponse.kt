package com.verse.app.model.song

import com.verse.app.model.base.BaseResponse
import com.verse.app.model.song.SongMainData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SongInfoResponse(
    @SerialName("result")
    val result: SongMainData = SongMainData()
) : BaseResponse()