package com.verse.app.model.lounge

import android.os.Parcelable
import com.verse.app.model.community.CommunityLoungeData
import kotlinx.parcelize.Parcelize

/**
 * Description : 라운지 글쓰기 or 수정 IntentModel
 *
 * Created by juhongmin on 2023/05/17
 */
@Parcelize
data class LoungeIntentModel(
    val type: Type, // write, modify
    val modifyInfo: ModifyInfo? = null
) : Parcelable {
    enum class Type {
        WRITE,
        DETAIL,
        MODIFY
    }

    @Parcelize
    data class ModifyInfo(
        val code: String = "",
        val memberCode: String = "",
        val contents: String = "",
        val imageList: List<String> = listOf(),
        val linkUrl: String? = null
    ) : Parcelable {
        constructor(data: CommunityLoungeData) : this(
            code = data.code,
            memberCode = data.memberCode,
            contents = data.contents,
            imageList = listOf(data.imagePath),
            linkUrl = null
        )

        constructor(code: String) : this(
            code = code,
            memberCode = "",
            contents = "",
            imageList = listOf(),
            linkUrl = null
        )
    }

    constructor() : this(
        type = Type.WRITE,
        modifyInfo = null
    )

    constructor(data: CommunityLoungeData) : this(
        type = Type.DETAIL,
        modifyInfo = ModifyInfo(data)
    )

    constructor(code: String) : this(
        type = Type.MODIFY,
        modifyInfo = ModifyInfo(code)
    )
}