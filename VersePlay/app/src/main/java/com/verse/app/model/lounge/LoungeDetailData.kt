package com.verse.app.model.lounge

import com.verse.app.contants.AppData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/19
 */
@Serializable
data class LoungeDetailData(
    @SerialName("louMngCd")
    val code: String = "",
    @SerialName("memNk")
    val nickName: String = "",
    @SerialName("pfFrImgPath")
    val profileImagePath: String = "",
    @SerialName("louTitle")
    val title: String = "",
    @SerialName("louContents")
    val contents: String = "",
    @SerialName("louPicPath1")
    val imagePath1: String? = null,
    @SerialName("louPicPath2")
    val imagePath2: String? = null,
    @SerialName("louPicPath3")
    val imagePath3: String? = null,
    @SerialName("louPicPath4")
    val imagePath4: String? = null,
    @SerialName("louPicPath5")
    val imagePath5: String? = null,
    @SerialName("louPicPath6")
    val imagePath6: String? = null,
    @SerialName("louPicPath7")
    val imagePath7: String? = null,
    @SerialName("louPicPath8")
    val imagePath8: String? = null,
    @SerialName("louPicPath9")
    val imagePath9: String? = null,
    @SerialName("louPicPath10")
    val imagePath10: String? = null,
    @SerialName("linkUrl")
    val linkUrl: String? = null,
    @SerialName("comtCnt")
    val commentCnt: String = "0",
    @SerialName("updDt")
    val updateDt: String = "",
    @SerialName("likeCnt")
    var likeCount: Int = 0,
    @SerialName("fgLikeYn")
    var likeYn: String = ""
) {
    var isLike: Boolean = false
        get() {
            field = likeYn == AppData.Y_VALUE

            return field
        }

    fun getImageList(): List<String> {
        val list = mutableListOf<String>()

        if (!imagePath1.isNullOrEmpty()) {
            list.add(imagePath1)
        }
        if (!imagePath2.isNullOrEmpty()) {
            list.add(imagePath2)
        }
        if (!imagePath3.isNullOrEmpty()) {
            list.add(imagePath3)
        }
        if (!imagePath4.isNullOrEmpty()) {
            list.add(imagePath4)
        }
        if (!imagePath5.isNullOrEmpty()) {
            list.add(imagePath5)
        }
        if (!imagePath6.isNullOrEmpty()) {
            list.add(imagePath6)
        }
        if (!imagePath7.isNullOrEmpty()) {
            list.add(imagePath7)
        }
        if (!imagePath8.isNullOrEmpty()) {
            list.add(imagePath8)
        }
        if (!imagePath9.isNullOrEmpty()) {
            list.add(imagePath9)
        }
        if (!imagePath10.isNullOrEmpty()) {
            list.add(imagePath10)
        }

        return list
    }
}