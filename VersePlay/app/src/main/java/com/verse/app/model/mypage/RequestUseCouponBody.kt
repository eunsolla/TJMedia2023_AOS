package com.verse.app.model.mypage

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestUseCouponBody (
    @SerialName("cpnCd")                                  //쿠폰발급관리코드
    var cpnCd: String = "",
    ) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "RequestUseCouponBody"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        val asItem = diffUtil as RequestUseCouponBody
        return this == asItem
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        val asItem = diffUtil as RequestUseCouponBody
        return this == asItem
    }

}