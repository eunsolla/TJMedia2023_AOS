package com.verse.app.model.search

import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    @SerialName("result")
    val result: Result = Result(),
) : BaseResponse()

@Serializable
data class Result(
    val searchResult: SearchResultList = SearchResultList()
) : BasePaging()
