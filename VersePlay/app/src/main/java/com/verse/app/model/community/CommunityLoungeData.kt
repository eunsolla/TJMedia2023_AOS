package com.verse.app.model.community

import android.net.Uri
import android.text.format.DateFormat.getBestDateTimePattern
import com.verse.app.contants.AppData
import com.verse.app.contants.Config
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.utility.DLogger
import com.verse.app.utility.LocaleUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


/**
 * Description :
 *
 * Created by juhongmin on 2023/05/16
 */
@Serializable
data class CommunityLoungeData(
    @SerialName("louMngCd")
    val code: String = "", // 관리 코드
    @SerialName("memNk")
    val name: String = "", // 작성자 닉네임
    @SerialName("memCd")
    val memberCode: String = "",
    @SerialName("louTitle")
    val title: String = "",
    @SerialName("louContents")
    val contents: String = "",
    @SerialName("louPicPath")
    val imagePath: String = "",
    @SerialName("comtCnt")
    val commentCnt: String = "",
    @SerialName("updDt")
    val updateDt: String = "",
    @SerialName("likeCnt")
    var likeCount: Int = 0,
    @SerialName("pfFrImgPath")
    val profileImagePath: String = "",
    @SerialName("fgLikeYn")
    var fgLikeYn: String = "",
    @SerialName("linkUrl")
    val linkUrl: String = ""
) : BaseModel() {

    enum class Type {
        NONE,
        IMAGE,
        LINK
    }

    override fun getViewType(): ListPagedItemType {
        return ListPagedItemType.ITEM_COMMUNITY_LOUNGE
    }

    override fun getClassName(): String {
        return "CommunityLoungeData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is CommunityLoungeData) {
            code == diffUtil.code
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is CommunityLoungeData) {
            this == diffUtil
        } else {
            false
        }
    }

    var dateText: String? = null
        get() {
            if (field == null) {
                field = try {
                    LocaleUtils.getLocalizationTime(updateDt, true)
                 } catch (ex: Exception) {
                    ""
                }
            }
            return field
        }

    var imageUri: String? = null
        get() {
            if (field == null) {
                field = try {
                    val imageUri = Uri.parse(Config.BASE_FILE_URL.plus(imagePath))
                    Config.BASE_FILE_URL.plus(imageUri.path)
                } catch (ex: Exception) {
                    ""
                }
            }
            return field
        }

    var isLike: Boolean = false
        get() {
            field = fgLikeYn == AppData.Y_VALUE

            return field
        }

    var linkThumbnailUrl: String? = null
    var linkTitle: String? = null

    val type: Type
        get() {
            return if (imagePath.isNotEmpty()) {
                DLogger.d("ImagePath $imagePath")
                Type.IMAGE
            } else if (!linkThumbnailUrl.isNullOrEmpty()) {
                Type.LINK
            } else {
                Type.NONE
            }
        }
}