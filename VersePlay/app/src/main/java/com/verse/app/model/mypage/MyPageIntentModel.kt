package com.verse.app.model.mypage

import android.os.Parcelable
import com.verse.app.model.feed.CurrentFeedData
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.user.UserData
import kotlinx.parcelize.Parcelize

/**
 * Description : 마이 페이지 진입시 필요한 데이터 모델 함수
 *
 * Created by juhongmin on 2023/06/10
 */
@Parcelize
data class MyPageIntentModel(
    val memberCode: String,
    val isShowLoading: Boolean = true,
    val isMainRightFragmentType: Boolean = false,
    val memberName: String? = null,
    val memberFrImagePath: String? = null,
    val memberBgImagePath: String? = null,
) : Parcelable {
    /**
     * 단순 멤버 코드만 전달하는 타입
     */
    constructor(memberCode: String) : this(
        memberCode = memberCode,
        isShowLoading = true,
        isMainRightFragmentType = false,
        memberName = null,
        memberFrImagePath = null,
        memberBgImagePath = null,
    )

    /**
     * MainRightFragment 전용
     */
    constructor(entity: CurrentFeedData) : this(
        memberCode = entity.ownerMemCd,
        isShowLoading = false,
        isMainRightFragmentType = true,
        memberName = entity.ownerMemNk,
        memberFrImagePath = entity.ownerFrImgPath,
        memberBgImagePath = entity.ownerBgImgPath
    )
}