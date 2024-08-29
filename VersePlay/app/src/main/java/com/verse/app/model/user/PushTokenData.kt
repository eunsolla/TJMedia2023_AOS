package com.verse.app.model.user

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PushTokenData (

    @SerialName("token")
    var token: String? = null,

    @SerialName("os")
    val os: String = "android",  // TODO aos ?


)  : BaseModel() {

    fun PushTokenBody(token: String?) {
        this.token = token
    }

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "PushTokenData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as PushTokenData
        return this == asItem
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as PushTokenData
        return this == asItem
    }

}