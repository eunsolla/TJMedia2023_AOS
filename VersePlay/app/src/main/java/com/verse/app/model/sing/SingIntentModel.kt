package com.verse.app.model.sing

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Description : 부르기 정보
 *
 * Created by jhlee on 2023-06-20
 */
@Parcelize
data class SingIntentModel(
    val singType: String,
    val songMngCd: String,
    val feedMngCd: String,
    val feedMdTpCd: String
) : Parcelable