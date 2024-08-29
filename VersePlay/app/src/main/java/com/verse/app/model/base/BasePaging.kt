package com.verse.app.model.base

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 공통 Paging
 *
 * Created by jhlee on 2023-04-01
 */
@Serializable
open class BasePaging {

    @SerialName("pageSize")
    val pageSize: Int = 20

    @SerialName("totalCnt")
    val totalCnt: Int = 20

    @SerialName("pageNum")
    var pageNum: Int = 1

    @SerialName("totalPageCnt")
    val totalPageCnt: Int = 1

    @SerialName("startRownum")
    val startRownum: Int = 1

    @SerialName("endRownum")
    val endRownum: Int = 1

}