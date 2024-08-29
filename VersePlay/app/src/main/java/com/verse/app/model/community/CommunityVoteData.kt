package com.verse.app.model.community

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.utility.LocaleUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/16
 */
@Serializable
data class CommunityVoteData(
    @SerialName("comVoteMngCd")
    val code: String = "",
    @SerialName("comVoteStDt")
    val startDt: String = "",
    @SerialName("comVoteFnDt")
    val endDt: String = "",
    @SerialName("stCd")
    val statusCode: String = "",
    @SerialName("comVoteTitle")
    val title: String = "",
    @SerialName("comVoteImgPath")
    val imagePath: String = "",
    @SerialName("joinVoteYn")
    val joinVoteYn: String = "", // 본인 참여 여부
    @SerialName("voteItem")
    val voteItem: String = "" // 투표 선택 번호
) : BaseModel() {
    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_COMMUNITY_VOTE
    }

    override fun getClassName(): String {
        return "CommunityVoteData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is CommunityVoteData) {
            code == diffUtil.code
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is CommunityVoteData) {
            this == diffUtil
        } else {
            false
        }
    }

    companion object {
        const val DTE_FORMAT = "yyyy.MM.dd"
        private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.KOREA)
    }

    var dateText: String? = null
        get() {
            if (field == null) {
                field = try {
                    LocaleUtils.getLocalizationTime(startDt, true)
                } catch (ex: Exception) {
                    ""
                }
            }
            return field
        }

    var isVoteEnd: Boolean = false

}
