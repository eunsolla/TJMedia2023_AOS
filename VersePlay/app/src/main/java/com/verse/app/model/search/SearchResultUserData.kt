package com.verse.app.model.search

import com.verse.app.contants.AppData
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResultUserData(
    @SerialName("memCd")
    var memCd: String = "",
    @SerialName("memNk")
    var memNk: String = "",
    @SerialName("memStCd")
    var memStCd: String = "",
    @SerialName("memEmail")
    var memEmail: String = "",
    @SerialName("memTpCd")
    var memTpCd: String = "",
    @SerialName("pfFrImgPath")
    var pfFrImgPath: String = "",
    @SerialName("pfBgImgPath")
    var pfBgImgPath: String = "",
    @SerialName("followCount")
    var followCount: String = "",
    @SerialName("relateFeedCount")
    var relateFeedCount: String = "",
    @SerialName("followYn")
    var followYn: String = AppData.N_VALUE,
) : BaseModel() {

    //팔로우여부
    var isFollowYn: Boolean = false
        get() {
            return followYn == AppData.Y_VALUE
        }
        set(value) {
            followYn = if (value) {
                AppData.Y_VALUE
            } else {
                AppData.N_VALUE
            }
            field = value
        }

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "SearchResultUserData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is SearchResultUserData) {
            memCd == diffUtil.memCd
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is SearchResultUserData) {
            this == diffUtil
        } else {
            false
        }
    }
}