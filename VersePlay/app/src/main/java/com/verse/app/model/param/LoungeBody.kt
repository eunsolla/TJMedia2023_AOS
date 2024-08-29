package com.verse.app.model.param

import com.verse.app.usecase.PostLoungeUseCase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/16
 */
@Serializable
data class LoungeBody(
    @SerialName("louMngCd")
    val code: String,
    @SerialName("louContents")
    val contents: String = "",
    @SerialName("linkUrl")
    val linkUrl: String? = null,
    @SerialName("attachImgCount")
    val imageUploadCount: String = "",
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
    val imagePath10: String? = null
) {

    constructor(code: String, contents: String, linkUrl: String?) : this(
        code = code,
        contents = contents,
        linkUrl = linkUrl,
        imageUploadCount = "0"
    )

    constructor(
        code: String,
        contents: String,
        linkUrl: String?,
        uploadPathList: List<String>
    ) : this(
        code = code,
        contents = contents,
        linkUrl = linkUrl,
        imageUploadCount = uploadPathList.size.toString(),
        imagePath1 = uploadPathList.getOrNull(0),
        imagePath2 = uploadPathList.getOrNull(1),
        imagePath3 = uploadPathList.getOrNull(2),
        imagePath4 = uploadPathList.getOrNull(3),
        imagePath5 = uploadPathList.getOrNull(4),
        imagePath6 = uploadPathList.getOrNull(5),
        imagePath7 = uploadPathList.getOrNull(6),
        imagePath8 = uploadPathList.getOrNull(7),
        imagePath9 = uploadPathList.getOrNull(8),
        imagePath10 = uploadPathList.getOrNull(9)
    )

    constructor(model: PostLoungeUseCase.MiddleModel.Step3) : this(
        code = model.baseData.code ?: throw NullPointerException("code is Null"),
        contents = model.baseData.contents,
        linkUrl = model.baseData.linkUri,
        imageUploadCount = Math.max(0, model.mergeImagePathList.size).toString(),
        imagePath1 = model.mergeImagePathList.getOrNull(0),
        imagePath2 = model.mergeImagePathList.getOrNull(1),
        imagePath3 = model.mergeImagePathList.getOrNull(2),
        imagePath4 = model.mergeImagePathList.getOrNull(3),
        imagePath5 = model.mergeImagePathList.getOrNull(4),
        imagePath6 = model.mergeImagePathList.getOrNull(5),
        imagePath7 = model.mergeImagePathList.getOrNull(6),
        imagePath8 = model.mergeImagePathList.getOrNull(7),
        imagePath9 = model.mergeImagePathList.getOrNull(8),
        imagePath10 = model.mergeImagePathList.getOrNull(9)
    )
}
