package com.verse.app.model.mypage

import com.android.billingclient.api.ProductDetails
import com.verse.app.contants.ListPagedItemType
import com.verse.app.contants.MemberShipType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetSubscribeListResponse(
    var result: SubscribeList = SubscribeList(),
) : BaseResponse()


@Serializable
data class SubscribeList(
    val subscTpCd: String = "",
    val purchPrdId: String = "",
    val purchToken: String = "",
    val subscMngCd: String = "",
    val subscSongCount: Int = 0,
    val subscPeriodDt: String = "",
    val periodSubsList: MutableList<SubscData> = mutableListOf(),
    val songSubsList: MutableList<SubscData> = mutableListOf(),
) {

    var purchaseProductIdList: MutableList<String> = mutableListOf()
        get() {
            var resultList: MutableList<String> = mutableListOf()

            this.songSubsList.forEach {
                resultList += it.subscPurcId
            }

            this.periodSubsList.forEach {
                resultList += it.subscPurcId
            }

            return resultList
        }

    var validPeriodSubsList: MutableList<SubscData> = mutableListOf()
        get() {
            var resultList: MutableList<SubscData> = mutableListOf()

            // 기간 이용권 중 VP쿠폰 제외
            this.periodSubsList.forEach {
                if (it.subscTpCd != MemberShipType.SC009.code) {
                    resultList += it
                }
            }

            return resultList
        }
}

@Serializable
data class SubscData(
    @SerialName("subscPurcId")
    val subscPurcId: String = "",   // 인앱결제상품아이디
    @SerialName("subscTpCd")
    val subscTpCd: String = "",     // 이용권유형코드
    @SerialName("subscTpNm")
    val subscTpNm: String = "",     // 이용권유형명
    @SerialName("subscTpDesc")
    val subscTpDesc: String = "",   // 이용권유형설명
    var subscPrice: String = "",   // 이용권가격
    var isVisibility: Boolean = true, // 이용권구매가능여부
    @Contextual
    var productDetails: ProductDetails? = null,
) : BaseModel() {
    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_MEMBERSHIP
    }

    override fun getClassName(): String {
        return "SubscData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is SubscData) {
            this == diffUtil
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is SubscData) {
            this == diffUtil
        } else {
            false
        }
    }
}

data class ProductDetails(
    var productDetails: ProductDetails
)
