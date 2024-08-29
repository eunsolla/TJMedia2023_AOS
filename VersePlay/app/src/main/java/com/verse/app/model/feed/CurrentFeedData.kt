package com.verse.app.model.feed

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * Description : 피드 인텐트
 *
 * Created by jhlee on 2023-04-13
 */
@Parcelize
data class CurrentFeedData(
    val feedMngCd: String,                   // 피드관리코드
    val ownerMemCd: String,                   // 피드관리코드
    val paTpCd: String,
    val ownerMemNk: String,
    val ownerFrImgPath: String,
    val ownerBgImgPath: String,
) : Parcelable