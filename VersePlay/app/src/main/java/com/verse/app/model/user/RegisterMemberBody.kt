package com.verse.app.model.user

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.utility.DLogger
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterMemberBody(

    @SerialName("authType")
    var authType: String = "",

    @SerialName("authToken")
    var authToken: String = "",

    @SerialName("nickName")
    var nickName: String = "",

    @SerialName("email")
    var email: String = "",

    @SerialName("conVer")
    var conVer: String = "",            //사용버전(APP버전)

    @SerialName("conModel")
    var conModel: String = "",          //사용디바이스모델

    @SerialName("conOsVer")
    var conOsVer: String = "",          //사용OS버전

    @SerialName("conIp")
    var conIp: String = "",             //접속IP

    ) : BaseModel() {

    fun registerMemberBody(nickName: String, authType: String, email: String, conVer: String, conModel: String, conOsVer: String, conIp: String) {
        this.authType = authType
        this.nickName = nickName
        this.email = email
        this.conVer = conVer
        this.conModel = conModel
        this.conOsVer = conOsVer
        this.conIp = conIp
        DLogger.d("authType : ${this.authType} authToken : ${this.authToken} nickName : ${this.nickName} email : ${this.email} conVer : ${this.conVer} conModel : ${this.conModel} conOsVer : ${this.conOsVer} conIp : ${this.conIp}")
    }

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "RegisterMemberBody"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        val asItem = diffUtil as RegisterMemberBody
        return this == asItem
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        val asItem = diffUtil as RegisterMemberBody
        return this == asItem
    }

}