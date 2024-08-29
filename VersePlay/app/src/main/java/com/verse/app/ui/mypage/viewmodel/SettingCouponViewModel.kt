package com.verse.app.ui.mypage.viewmodel

import com.verse.app.R
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.HttpStatusType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.RequestUseCouponBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingCouponViewModel @Inject constructor(
    val accountPref: AccountPref,
    val deviceProvider: DeviceProvider,
    val resProvider: ResourceProvider,
    val apiService: ApiService,

    ) : ActivityViewModel() {
    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                      // 뒤로가기
    val startOneButtonPopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }          // popup
    val couponStatusSuccess: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }            // 쿠폰 success
    val couponStatusFail: SingleLiveEvent<String> by lazy { SingleLiveEvent() }            // 쿠폰 fail
    val onDelete: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                       // 삭제버튼 클릭
    val editCouponText: NonNullLiveData<String> by lazy { NonNullLiveData("") }     // 쿠폰번호
    /**
     * 뒤로 선택
     */
    fun back() {
        backpress.call()
    }

    /**
     * ? 선택 시 팝업 노출
     */
    fun popup() {
        startOneButtonPopup.call()
    }

    fun delete(){
        onDelete.call()
    }

    fun setCouponApi(){
        if (AppData.IS_MYPAGE_EDIT){
            apiService.requestUseCoupon(
                RequestUseCouponBody(
                    cpnCd = editCouponText.value
                ))
                .doLoading()
                .applyApiScheduler()
                .request({
                    if (it.status == HttpStatusType.SUCCESS.status) {
                        couponStatusSuccess.value = R.string.coupon_success
                    } else if (it.status == HttpStatusType.FAIL.status) {
                        couponStatusFail.value = it.message
                    }
                }, {
                    DLogger.d("Error 쿠폰 => ${it.message}")
                })
        }
    }

    /**
     * 데이터 저장
     */
    fun onComplete(){
        AppData.IS_MYPAGE_EDIT = true
        setCouponApi()
    }


}