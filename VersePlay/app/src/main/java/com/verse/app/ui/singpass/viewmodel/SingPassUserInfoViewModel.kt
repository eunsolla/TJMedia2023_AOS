package com.verse.app.ui.singpass.viewmodel

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.verse.app.R
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.UserPurchaseLevel
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.singpass.SingPassTerms
import com.verse.app.model.singpass.SingPassUserInfoData
import com.verse.app.repository.http.ApiService
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : 씽패스 ViewModel
 *
 * Created by jhlee on 2023-03-2
 */
@HiltViewModel
class SingPassUserInfoViewModel @Inject constructor(
    private val apiService: ApiService,
    val deviceProvider: DeviceProvider,
    private val resourceProvider: ResourceProvider
) : ActivityViewModel() {

    val startFinish: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val moveToWebView: SingleLiveEvent<SingPassTerms> by lazy { SingleLiveEvent() }
    private val _singPassUserInfoData: MutableLiveData<SingPassUserInfoData> by lazy { MutableLiveData() }
    val singPassUserInfoData: LiveData<SingPassUserInfoData> get() = _singPassUserInfoData
    val profileFrImagePath: LiveData<String>
        get() = Transformations.map(singPassUserInfoData) { it.singPassUserInfo?.pfFrImgPath ?: "" }
    val profileBgImagePath: LiveData<String>
        get() = Transformations.map(singPassUserInfoData) { it.singPassUserInfo?.pfBgImgPath ?: "" }
    val userNickName: LiveData<String>
        get() = Transformations.map(singPassUserInfoData) { it.singPassUserInfo?.memNk ?: "" }
    val userRanking: LiveData<String>
        get() = Transformations.map(singPassUserInfoData) { it.singPassUserInfo?.ranking }
    val singPassLevelIcon: LiveData<Drawable>
        get() = Transformations.map(singPassUserInfoData) { getSingPassLevelImage(it.singPassUserInfo?.seaLevelNm) }
    val userLevelIcon: LiveData<Drawable>
        get() = Transformations.map(singPassUserInfoData) { getUserLevelImage(it.singPassUserInfo?.memGrCd) }
    private val _isUserInfo: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }
    val isUserInfo: LiveData<Boolean> get() = _isUserInfo


    val moveToProfileImageDetail: SingleLiveEvent<String> by lazy { SingleLiveEvent() }                         // 프로필 이미지 상세

    /**
     * api call - sing pss
     */
    fun requestSingPassUserInfo(userMemCd: String, seasonCd: String, genreCd: String) {
        apiService.getSingPassUserDetailInfo(
            userMemCd,
            seasonCd,
            genreCd
        )
            .doLoading()
            .map { it.result }
            .applyApiScheduler()
            .request({
                _singPassUserInfoData.value = it
                _isUserInfo.value = it.singPassUserInfo != null
            }, { error ->
                DLogger.d("##!!! Fail SingPass  ${error} ")
                // startFinish.call()
            })
    }

    fun close() {
        startFinish.call()
    }

    fun moveToWebView() {
        _singPassUserInfoData.value?.let {
            if (it.singPassTerms != null) {
                moveToWebView.value = it.singPassTerms
            }
        }
    }

    fun moveToProfileImageDetail() {
        singPassUserInfoData.value?.singPassUserInfo?.pfFrImgPath?.let {
            moveToProfileImageDetail.value = it
        }
    }

    private fun getSingPassLevelImage(level: String?): Drawable? {
        val id = when (level) {
            "LEVEL 02" -> R.drawable.ic_sing_pass_level_2
            "LEVEL 03" -> R.drawable.ic_sing_pass_level_3
            "LEVEL 04" -> R.drawable.ic_sing_pass_level_4
            "LEVEL 05" -> R.drawable.ic_sing_pass_level_5
            else -> R.drawable.ic_sing_pass_level_1
        }
        return resourceProvider.getDrawable(id)
    }

    private fun getUserLevelImage(level: String?): Drawable? {
        val id = when (level) {
            UserPurchaseLevel.NORMAL.code -> R.drawable.common_lv_2
            UserPurchaseLevel.FRIEND.code -> R.drawable.buddy_lv_3
            UserPurchaseLevel.FAMILY.code -> R.drawable.family_lv_4
            else -> R.drawable.user_lv_1
        }
        return resourceProvider.getDrawable(id)
    }
}