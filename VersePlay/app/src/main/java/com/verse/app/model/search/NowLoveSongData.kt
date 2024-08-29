package com.verse.app.model.search

import com.verse.app.contants.Config.BASE_FILE_URL
import com.verse.app.contants.ListPagedItemType
import com.verse.app.model.base.BaseModel
import com.verse.app.utility.DLogger
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NowLoveSongData(

    @SerialName("loveSongMngCD")
    val loveSongMngCD: String = "",

    @SerialName("songMngCd")
    val songMngCd: String = "",

    @SerialName("songId")
    val songId: String = "",

    @SerialName("songNm")
    val songNm: String = "",

    @SerialName("albImgPath")
    val albImgPath: String = ""
) : BaseModel() {

    @Contextual
    var albImgPaths: String? = null
        get() {
            if (field == null) {
                // BASE_FILE_URL + albImgPath
                field = if (albImgPath.startsWith("http")) {
                    albImgPath
                } else {
                    "${BASE_FILE_URL}${albImgPath}"
                }
                DLogger.d("ImagePath ${field}")
            }
            return field
        }


    override fun getViewType(): ListPagedItemType {
        return itemViewType
    }

    override fun getClassName(): String {
        return "NowLoveSongData"
    }

    override fun areItemsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is NowLoveSongData) {
            loveSongMngCD == diffUtil.loveSongMngCD
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffUtil: Any): Boolean {
        return if (diffUtil is NowLoveSongData) {
            this == diffUtil
        } else {
            false
        }
    }
}