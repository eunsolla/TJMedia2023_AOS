package com.verse.app.model.song

import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import com.verse.app.model.base.BaseViewTypeModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 곡 정보 Data
 *
 * Created by jhlee on 2023-04-27
 */

@Serializable
data class SongListResponse(
    @SerialName("result")
    val result: SongListInfo = SongListInfo()
) : BaseResponse()

@Serializable
data class SongListInfo(
    var resSongInfoList: List<SongMainData> = listOf()
) : BasePaging()
