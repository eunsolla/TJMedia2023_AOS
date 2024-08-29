package com.verse.app.model.mypage

import android.net.Uri
import com.verse.app.contants.AppData
import com.verse.app.contants.Config
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetRecommendUserResponse(
    val result: RecommendUserResponseInfo = RecommendUserResponseInfo(),
) : BaseResponse()

@Serializable
data class RecommendUserResponseInfo(
    // datalist -> userlist로 변경
    val userList: List<RecommendUserData> = listOf(),
)

@Serializable
data class RecommendUserData(
    @SerialName("memCd")
    val memCd: String = "",             //사용자회원관리코드
    @SerialName("memNk")
    val memNk: String = "",             //닉네임
    @SerialName("memStCd")
    val memStCd: String = "",           //회원상태메시지(US001:정상 / US002:정지 / US003:휴면 / US004:탈퇴)
    @SerialName("memEmail")
    val memEmail: String = "",          //회원이메일
    @SerialName("memTpCd")
    val memTpCd: String = "",           //회원유형코드(신규/무료/유료/TJ대리점/협력사/BP파트너/인플루언서)
    @SerialName("pfFrImgPath")
    val pfFrImgPath: String = "",       //프로필이미지경로
    @SerialName("pfBgImgPath")
    val pfBgImgPath: String = "",       //프로필배경이미지경로

    var followYn: String = AppData.N_VALUE,           //api followYn이 없는 경우..
) : BaseModel() {

    var isFollow: Boolean = false
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
        return "RecommendUserData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is RecommendUserData) {
            this.memCd == diffUtil.memCd
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is RecommendUserData) {
            this == diffUtil
        } else {
            false
        }
    }

    var profileImageUrl: String? = null
        get() {
            if (field == null) {
                field = if (pfFrImgPath.isNotEmpty()) {
                    val uri = Uri.parse(Config.BASE_FILE_URL.plus(pfFrImgPath))
                    Config.BASE_FILE_URL.plus(uri.path)
                } else {
                    ""
                }
            }
            return field
        }
}
