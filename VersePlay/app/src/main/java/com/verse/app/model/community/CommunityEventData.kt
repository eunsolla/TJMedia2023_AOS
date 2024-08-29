package com.verse.app.model.community

import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.utility.LocaleUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/16
 */
@Serializable
data class CommunityEventData(
    @SerialName("evtMngCd")
    val code: String = "",
    @SerialName("evtTitle")
    val title: String = "",
    @SerialName("stCd")
    val statusCode: String = "",
    @SerialName("thumbPicPath")
    val imagePath: String = "",
    @SerialName("evtStDt")
    val startDt: String = "", // 이벤트 시작일시
    @SerialName("evtFnDt")
    val endDt: String = "", // 이벤트 종료 일시
    @SerialName("pfFgImgPath")
    val profileImagePath: String = "" // 프로필 이미지 경로
) : BaseModel() {
    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_COMMUNITY_EVENT
    }

    override fun getClassName(): String {
        return "CommunityEventData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is CommunityEventData) {
            code == diffUtil.code
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is CommunityEventData) {
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

    var isEventEnd: Boolean = false
}