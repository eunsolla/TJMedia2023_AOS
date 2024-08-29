package com.verse.app.model.vote

import android.os.Parcelable
import com.verse.app.model.community.CommunityVoteData
import kotlinx.parcelize.Parcelize

/**
 * Description : 투표 참여 or 마감 Intent Model
 *
 * Created by juhongmin on 2023/05/19
 */
@Parcelize
data class VoteIntentModel(
    val type: Type,
    val code: String
) : Parcelable {

    enum class Type {
        PARTICIPATION,
        END
    }

    constructor(entity: CommunityVoteData) : this(
        type = if (entity.isVoteEnd) Type.END else Type.PARTICIPATION,
        code = entity.code
    )
}
