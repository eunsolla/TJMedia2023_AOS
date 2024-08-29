package com.verse.app.model.mypage

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import com.verse.app.utility.LocaleUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class GetRecentLoginHistoryResponse(

    var result: GetRecentLoginHistoryList,

    ) : BaseResponse()

@Serializable
data class GetRecentLoginHistoryList(
    val dataList: MutableList<GetRecentLoginHistoryData> = mutableListOf(),
) : BasePaging()


@Serializable
data class GetRecentLoginHistoryData(
    @SerialName("loginMngCd")                               //로그인이력관리코드
    var loginMngCd: String = "",

    @SerialName("authTpCd")                                 //로그인유형(SNS인증유형)
    var authTpCd: String = "",

    @SerialName("conCh")                                    //로그인채널(PC/AOS/IOS)
    var conCh: String = "",

    @SerialName("conModel")                                 //사용디바이스모델
    val conModel: String = "",

    @SerialName("updDt")                                    //로그인일시
    val updDt: String = "",

    ) : BaseModel() {

    var dateText: String? = null
        get() {
            if (field == null) {
                field = try {
                    LocaleUtils.getLocalizationTime(updDt, true)
                } catch (ex: Exception) {
                    ""
                }
            }
            return field
        }

    var changeLoginName: String? = null
        get() {
            if (field == null) {
                field = try {
                    LocaleUtils.getLoginDeviceName(authTpCd)
                } catch (ex: Exception) {
                    ""
                }
            }
            return field
        }

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "GetRecentLoginHistoryData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetRecentLoginHistoryData
        return this.loginMngCd == asItem.loginMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetRecentLoginHistoryData
        return this == asItem
    }
}
