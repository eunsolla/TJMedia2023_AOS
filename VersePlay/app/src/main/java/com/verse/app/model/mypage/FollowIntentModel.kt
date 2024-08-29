package com.verse.app.model.mypage

import android.os.Parcelable
import com.verse.app.model.user.UserData
import kotlinx.parcelize.Parcelize

/**
 * Description : 팔로잉 페이지로 가기 위한 IntentDataModel
 *
 * Created by juhongmin on 2023/06/06
 */
@Parcelize
data class FollowIntentModel(
    val memberCode: String,
    val memberNickname: String,
    val followerCnt: Int,
    val followingCnt: Int,
    val isMyPage: Boolean,
    val initTabPosition: Int
) : Parcelable {
    constructor(
        entity: UserData,
        isMyPage: Boolean,
        position: Int
    ) : this(
        memberCode = entity.memCd,
        memberNickname = entity.memNk,
        followerCnt = entity.followerCnt,
        followingCnt = entity.followingCnt,
        isMyPage = isMyPage,
        initTabPosition = position
    )
}