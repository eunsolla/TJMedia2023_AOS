package com.verse.app.model.mypage

import android.os.Parcelable
import com.verse.app.model.user.UserData
import kotlinx.parcelize.Parcelize

/**
 * Description : 튜토리얼 데이터
 *
 * Created by juhongmin on 2023/06/06
 */
data class TutorialItemData(
    val imageRes: Int,
    val title: String,
    val subTitle: String,
)