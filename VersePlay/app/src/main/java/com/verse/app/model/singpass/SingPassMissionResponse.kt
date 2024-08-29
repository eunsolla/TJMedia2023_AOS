package com.verse.app.model.singpass

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SingPassMissionResponse(
    val result: SingPassMissionData = SingPassMissionData(),
) : BaseResponse()

@Serializable
data class SingPassMissionData(
    @SerialName("missionList")
    val missionList: MutableList<MissionItemData> = mutableListOf(),
) : BasePaging()

@Serializable
data class MissionInfo(
    @SerialName("svcMiRegTpCd")
    val svcMiRegTpCd: String = "",
    @SerialName("svcMiRegTpNm")
    val svcMiRegTpNm: String = "",
    @SerialName("missionList")
    val missionList: MutableList<MissionItemData> = mutableListOf(),
) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "MissionInfo"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as MissionInfo
        return this.svcMiRegTpCd == asItem.svcMiRegTpCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as MissionInfo
        return this == asItem
    }
}

@Serializable
data class MissionItemData(
    @SerialName("svcSeaMngCd")
    val svcSeaMngCd: String = "",
    @SerialName("svcMiMngCd")
    val svcMiMngCd: String = "",
    @SerialName("bfPoint")
    val bfPoint: Int = 0,
    @SerialName("miMngCd")
    val miMngCd: String = "",
    @SerialName("miMngNm")
    val miMngNm: String = "",
    @SerialName("svcMiRegTpCd")
    val svcMiRegTpCd: String = "",
    @SerialName("msTpCd")
    val msTpCd: String = "",
    @SerialName("fgCompletedYn")
    val fgCompletedYn: String = "",
) : BaseModel() {
    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "MissionItemData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as MissionItemData
        return this.svcMiMngCd == asItem.svcMiMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as MissionItemData
        return this == asItem
    }
}
