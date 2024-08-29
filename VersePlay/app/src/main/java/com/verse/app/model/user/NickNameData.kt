package com.verse.app.model.user

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NickNameData(
    @SerialName("nickname")
    var nickname: String = "",
)  : BaseModel() {

    fun NickNameData(nickname: String) {
        this.nickname = nickname
    }

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "NickNameData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as NickNameData
        return this == asItem
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as NickNameData
        return this == asItem
    }
}