package com.verse.app.model.mypage

import com.verse.app.R
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.model.base.BasePaging
import com.verse.app.model.base.BaseResponse
import com.verse.app.utility.LocaleUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetAlrimListResponse(
    @SerialName("result")
    var result: AlrimList = AlrimList(),
) : BaseResponse()

@Serializable
data class AlrimList(

    val dataList: MutableList<AlrimListData> = mutableListOf(),
) : BasePaging()

@Serializable
data class AlrimListData(
    @SerialName("pushMngCd")                              //푸쉬발송관리코드
    val pushMngCd: String = "",
    @SerialName("pushTpCd")                               //PT001 : PUSH알림 / PT002 : PUSH 알림팝업 / PT003 : PUSH알림 + PUSH알림팝업
    val pushTpCd: String = "",
    @SerialName("pushTitle")                              //푸쉬제목
    val pushTitle: String = "",
    @SerialName("pushMsg")                                //푸쉬메시지내용
    val pushMsg: String = "",
    @SerialName("linkCd")                                 //LD001 : URL / LD002 : 부르기메인 / LD003 : 피드메인 / LD004 : 씽패스 / LD005 : 마이페이지 / LD006 : 커뮤니티투표
    val linkCd: String = "",
    @SerialName("linkInfo")                                //이동처리URL 혹은 콘텐츠관리코드
    val linkInfo: String = "",
    @SerialName("pushImgPath")                            //첨부이미지파일경로
    var pushImgPath: String = "",
    @SerialName("pushAlTpCd")                             //푸쉬알림유형코드(PP001:업로드콘텐츠좋아요/PP002:댓글좋아요/PP003	: 답글좋아요/PP004:라운지글좋아요/PP005:참여한투표종료/PP006:구매완료/PP007:이용권만료3일전/PP008:이용권만료1일전/PP009:미션완료/PP010:미션아이템획득/PP011:시즌클리어뱃지획득/PP012:시즌시작/PP013:시즌종료/PP014:듀엣완성/PP015:배틀완성/PP016:휴면계정전환/PP017:정지계정전환/PP018:마케팅수신동의/PP019:야간푸시알림동의/PP020:일반알림
    val pushAlTpCd: String = "",
    @SerialName("sendDt")                                 //발송처리일시
    val sendDt: String = "",
    @SerialName("profileImgPath")                         //프로필이미지경로
    var profileImgPath: String = "",
    @SerialName("fgDelYn")                                //삭제여부
    var fgDelYn: String = "",
    @SerialName("fgExposeYn")                             //콘텐츠노출여부
    var fgExposeYn: String = "",
    @SerialName("songDelYn")                              //음원서비스정지여부(Y:서비스정지 / N:정상서비스중)
    var songDelYn: String = "",
    ) : BaseModel() {
    var alrimImage: Int? = when(this.pushAlTpCd) {
        "PP001" -> {
            R.drawable.ic_al_dormant_mem
        }

        "PP002" -> {
            R.drawable.ic_al_suspend_mem
        }

        "PP003" -> {
            R.drawable.ic_al_normal
        }

        "PP004" -> {
            R.drawable.ic_al_event
        }

        "PP005" -> {
            R.drawable.ic_al_start
        }

        "PP006" -> {
            R.drawable.ic_al_stop
        }

        "PP007" -> {
            R.drawable.ic_al_start
        }

        "PP008" -> {
            R.drawable.ic_al_stop
        }

        "PP019" -> {
            R.drawable.ic_al_normal
        }

        "PP020" -> {
            R.drawable.ic_al_expire
        }

        "PP021" -> {
            R.drawable.ic_al_expire
        }

        "PP022" -> {
            R.drawable.ic_al_normal
        }

        else -> {
            R.drawable.profile_empty_big
        }
    }

    var dateText: String? = null
        get() {
            if (field == null) {
                field = try {
                    LocaleUtils.getLocalizationTime(sendDt, true)
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
        return "AlrimListData"
    }

    override fun areItemsTheSame(newItem: Any): Boolean {
        val asItem = newItem as AlrimListData
        return this.pushMngCd == asItem.pushMngCd
    }

    override fun areContentsTheSame(newItem: Any): Boolean {
        val asItem = newItem as AlrimListData
        return this == asItem
    }
}