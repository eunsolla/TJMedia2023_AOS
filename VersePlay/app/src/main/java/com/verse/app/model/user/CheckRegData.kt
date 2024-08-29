package com.verse.app.model.user

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckRegData(

    @SerialName("access_token")
    var access_token: String = "",

    @SerialName("provider")
    var provider: String = "",

    @SerialName("email")
    var email: String = ""

)  : BaseModel() {

    fun CheckRegData(provider: String, email: String, access_token: String) {
        this.access_token = access_token
        this.email = email
        this.provider = provider
    }

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "CheckRegData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as CheckRegData
        return this == asItem
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as CheckRegData
        return this == asItem
    }
}