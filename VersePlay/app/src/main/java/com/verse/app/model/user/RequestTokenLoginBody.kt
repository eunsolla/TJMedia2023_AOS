package com.verse.app.model.user

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.utility.manager.LoginManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestTokenLoginBody(

    @SerialName("authType")
    var authType: String = "",

    @SerialName("appVersion")
    var appVersion: String = "",

    @SerialName("deviceModel")
    var deviceModel: String = "",

    @SerialName("osVersion")
    var osVersion: String = "",

    @SerialName("conIp")
    var conIp: String = "",

    @SerialName("pushKey")
    var pushKey: String = "",

    @SerialName("authToken")
    var authToken: String = "",

    ) : BaseModel() {

    fun requestTokenLoginBody(provider: String, authToken: String) {
        if (provider == LoginManager.LoginType.facebook.name) {
            this.authType = "AU002"
        } else if (provider == LoginManager.LoginType.google.name) {
            this.authType = "AU001"
        } else if (provider == LoginManager.LoginType.kakao.name) {
            this.authType = "AU004"
        } else if (provider == LoginManager.LoginType.naver.name) {
            this.authType = "AU005"
        } else if (provider == LoginManager.LoginType.twitter.name) {
            this.authType = "AU006"
        } else if (provider == LoginManager.LoginType.snapchat.name) {
            this.authType = ""
        }
        /*   this.appVersion = LoginManagerImpl.getInstance().appVersion
           this.deviceModel = LoginManagerImpl.getInstance().getDeviceName()
           this.osVersion = LoginManagerImpl.getInstance().getAndroidVersion()
           this.conIp = LoginManagerImpl.getInstance().getMacAddress()
           this.pushKey = ""
           this.authToken = authToken*/
    }


    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "RequestTokenLoginBody"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        val asItem = diffUtil as RequestTokenLoginBody
        return this == asItem
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        val asItem = diffUtil as RequestTokenLoginBody
        return this == asItem
    }

}