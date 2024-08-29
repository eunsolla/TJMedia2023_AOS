package com.verse.app.model.mypage

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * {
"httpStatus": 200,
"status": "Success",
"message": "성공",
"result": {
"dataList": [
{
"code": "KR",
"name": "한국"
},
{
"code": "US",
"name": "미국"
}
]
}
}
 */

@Serializable
data class GetNationLanguageListResponse(
    val result: GetNationLanguageListResponseInfo,
): BaseResponse()

@Serializable
data class GetNationLanguageListResponseInfo(
    val dataList: MutableList<GetNationLanguageListResponseData>,
)

@Serializable
data class GetNationLanguageListResponseData(
    @SerialName("code")
    val code: String = "",    //국가코드 혹은 언어코드
    @SerialName("name")
    val name: String = "",    //국가명 혹은 언어명
    var isSelected: Boolean = false,
) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "GetNationLanguageListResponseData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetNationLanguageListResponseData
        return this.code == asItem.code
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetNationLanguageListResponseData
        return this == asItem
    }
}