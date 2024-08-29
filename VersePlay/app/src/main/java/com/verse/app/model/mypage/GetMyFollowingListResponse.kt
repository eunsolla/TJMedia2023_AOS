package com.verse.app.model.mypage

import com.verse.app.contants.AppData
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GetMyFollowingListResponse (
    @SerialName("result")
    val result: GetMyFollowingListInfo = GetMyFollowingListInfo(),
) : BaseResponse()

@Serializable
data class GetMyFollowingListInfo(
    @SerialName("followingList")
    var followingList: MutableList<GetMyFollowListData> = mutableListOf(),
): BasePaging()

@Serializable
data class GetMyFollowListData(
    @SerialName("memCd")
    val memCd: String = "",                               //사용자회원관리코드
    @SerialName("memNk")
    val memNk: String = "",                               //닉네임
    @SerialName("memStCd")
    val memStCd: String = "",                             //회원상태코드(정상/정지/휴면/탈퇴)
    @SerialName("memGrCd")
    val memGrCd: String = "",                             //회원구매등급코드
    @SerialName("memEmail")
    val memEmail: String = "",                            //회원이메일
    @SerialName("memTpCd")
    val memTpCd: String = "",                             //회원유형코드(신규/무료/유료/TJ대리점/협력사/BP파트너/인플루언서)
    @SerialName("authTpCd")
    val authTpCd: String = "",                            //회원계정인증유형코드(AU001 / AU002 / AU003 / AU004 / AU005 / AU006)
    @SerialName("authToken")
    val authToken: String = "",                           //회원계정인증토큰(SNS인증토큰)
    @SerialName("instDesc")
    val instDesc: String = "",                            //회원상태메시지
    @SerialName("uploadFeedCnt")
    var uploadFeedCnt: String = "",                       //업로드콘텐츠수
    @SerialName("followerCnt")
    var followerCnt: Int = 0,                         //팔로워수
    @SerialName("followingCnt")
    var followingCnt: Int = 0,                        //팔로잉수
    @SerialName("outLinkUrl")
    val outLinkUrl: String = "",                          //외부Link URL
    @SerialName("pfFrImgPath")
    val pfFrImgPath: String = "",                         //프로필이미지경로
    @SerialName("pfBgImgPath")
    val pfBgImgPath: String = "",                         //프로필배경이미지경로
    @SerialName("followYn")
    var followYn: String = "",                    //팔로우여부

): BaseModel(){

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

    var followYnText: String? = null
        get() {
            field = if (followYn == AppData.Y_VALUE) {
                "팔로잉"
            } else {
                "팔로우"
            }
            return field
        }

    var contentsText: String? = null
        get() {
            if (field == null) {
                val count = try {
                    uploadFeedCnt.toInt()
                } catch (ex: Exception) {
                    0
                }
                field = "게시물 $count"
            }
            return field
        }

    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "GetMyFollowListData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetMyFollowListData
        return this.memCd == asItem.memCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as GetMyFollowListData
        return this == asItem
    }
}