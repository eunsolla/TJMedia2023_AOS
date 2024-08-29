package com.verse.app.model.user

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SetNickCountyBody(

    @SerialName("zip")
    var zip: String = "",

    @SerialName("county")
    var county: String = "",

    @SerialName("nickname")
    var nickname: String = ""
)  : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "SetNickCountyBody"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as SetNickCountyBody
        return this == asItem
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as SetNickCountyBody
        return this == asItem
    }
}