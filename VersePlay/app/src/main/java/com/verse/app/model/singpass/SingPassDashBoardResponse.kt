package com.verse.app.model.singpass

import com.verse.app.contants.AppData
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BaseResponse
import com.verse.app.model.mypage.BlockUserListData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SingPassDashBoardResponse(
    val result: SingPassDashBoardData = SingPassDashBoardData(),
) : BaseResponse()

@Serializable
data class SingPassDashBoardData(
    @SerialName("seasonInfo")
    val seasonInfo: SeasonInfo = SeasonInfo(),
    @SerialName("singPointInfo")
    val singPointInfo: SingPointInfo = SingPointInfo(),
    @SerialName("seasonItemList")
    val seasonItemList: MutableList<SeasonItemData> = mutableListOf(),
)

@Serializable
data class SeasonItemData(
    @SerialName("miMngCd")
    val miMngCd: String = "",
    @SerialName("getItemPossibleYn")
    var getItemPossibleYn: String = "",
    @SerialName("topItemPicPath")
    val topItemPicPath: String = "",
) : BaseModel() {

    // 비활성화 여부
    var isItemPossibleYn: Boolean = false
        get() {
            return getItemPossibleYn == AppData.Y_VALUE
        }
        set(value) {
            getItemPossibleYn = if (value) {
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
        return "SeasonItemData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as SeasonItemData
        return this.miMngCd == asItem.miMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as SeasonItemData
        return this == asItem
    }

}

@Serializable
data class SingPointInfo(
    @SerialName("memSingPoint")
    val memSingPoint: Int = 0,
    @SerialName("progressPer")
    val progressPer: Int = 0,
    @SerialName("seaPossibleSingPoint")
    val seaPossibleSingPoint: Int = 0,
)
