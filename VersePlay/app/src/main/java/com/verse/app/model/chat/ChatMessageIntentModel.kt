package com.verse.app.model.chat

import android.os.Parcelable
import com.verse.app.model.user.UserData
import kotlinx.parcelize.Parcelize

/**
 * Description : 채팅 하기전 필요한 데이터 모델
 *
 * Created by juhongmin on 2023/06/15
 */
@Parcelize
data class ChatMessageIntentModel(
    var roomCode: String? = null,
    val targetMemberCode: String,
    val targetProfileImagePath: String,
    val targetNickName: String,
    val targetDesc: String? = null,
    val targetFollowerCount: String,
    val targetFeedCount: String,
    val targetPrivateYn: String? = null,
    val targetBlockYn: String? = null
) : Parcelable {

    constructor(entity: ChatMemberRoomModel) : this(
        roomCode = entity.code,
        targetMemberCode = entity.targetMemberCode,
        targetProfileImagePath = entity.targetProfileImagePath,
        targetNickName = entity.targetMemberNk,
        targetDesc = entity.targetMemDesc,
        targetFollowerCount = "",
        targetFeedCount = "",
        targetPrivateYn = entity.fgPrivateYn,
        targetBlockYn = entity.fgBlockYn
        // targetFollowerCount = entity.targetFollowerCnt,
        // targetFeedCount = entity.targetContentsCnt
    )

    constructor(entity: UserData) : this(
        roomCode = null,
        targetMemberCode = entity.memCd,
        targetProfileImagePath = entity.pfFrImgPath,
        targetNickName = entity.memNk,
        targetDesc = entity.instDesc,
        targetFollowerCount = entity.followerCnt.toString(),
        targetFeedCount = entity.uploadFeedCnt.toString()
    )
}