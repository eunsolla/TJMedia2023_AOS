package com.verse.app.model.mypage

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetBlockUserListResponse(
    @SerialName("result")
    var result: BlockUserList = BlockUserList(),
) : BaseResponse()


@Serializable
data class BlockUserList(
    @SerialName("dataList")
    val dataList: MutableList<BlockUserListData> = mutableListOf(),
) : BasePaging()

@Serializable
data class BlockUserListData(
    @SerialName("conMngCd")                                 //차단회원관리코드
    val conMngCd: String = "",

    @SerialName("memNk")                                    //차단회원닉네임
    val memNk: String = "",

    @SerialName("pfFrImgPath")                              //차단회원프로필이미지경로
    val pfFrImgPath: String = "",

    ) : BaseModel() {

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "BlockUserListData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as BlockUserListData
        return this.conMngCd == asItem.conMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as BlockUserListData
        return this == asItem
    }

}
