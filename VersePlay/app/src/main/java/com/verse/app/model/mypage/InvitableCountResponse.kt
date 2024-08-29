package com.verse.app.model.mypage

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InvitableCountResponse (
    val result: InvitableCountData = InvitableCountData(),
) : BaseResponse()

@Serializable
data class InvitableCountData(
    @SerialName("remainCount")
    val remainCount: String = "",            // 추천가능수

) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "InvitableCountData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as InvitableCountData
        return this.remainCount == asItem.remainCount
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as InvitableCountData
        return this == asItem
    }
}
