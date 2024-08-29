package com.verse.app.ui.mypage.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.verse.app.R
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.HttpStatusType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.handleNetworkErrorRetry
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.RecommendNickNameBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MypageInviteFriendsViewModel @Inject constructor(
    val accountPref: AccountPref,
    val resProvider: ResourceProvider,
    val deviceProvider: DeviceProvider,
    val loginManager: LoginManager,
    val apiService: ApiService) : BaseViewModel() {

    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                       // 뒤로가기
    val myNk: SingleLiveEvent<String> by lazy { SingleLiveEvent() }                          // 내 닉네임
    val recUserNk: SingleLiveEvent<String> by lazy { SingleLiveEvent() }                     // 추천 유저 닉네임
    private val _myInvitableCnt: NonNullLiveData<String> by lazy { NonNullLiveData("0") }                          // 내 추천 가능 횟수
    val myInvitableCnt: LiveData<String> get() =_myInvitableCnt                      // 내 추천 가능 횟수
    val clickClipboard: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                  // 복사
    val couponStatusSuccess: SingleLiveEvent<String> by lazy { SingleLiveEvent() }            // 쿠폰 success
    val couponStatusFail: SingleLiveEvent<String> by lazy { SingleLiveEvent() }              // 쿠폰 fail

    /**
     * 뒤로 선택
     */
    fun back() {
        backpress.call()
    }

    /**
     * 추천 가능 횟수
     */
    fun getInvitableCount(){
        apiService.getInvitableCount(
            recNickName = myNk.value.toString()
        )
            .compose(handleNetworkErrorRetry())
            .doLoading()
            .applyApiScheduler()
            .request({ res ->
                _myInvitableCnt.value = res.result.remainCount
            }, { error ->
                DLogger.d("Throwable getInvitableCount $error")
            })
    }

    /**
     * 복사
     */
    fun onClickClipBoard(){
        showToastIntMsg.value = R.string.str_success_copy
        clickClipboard.call()
    }

    /**
     * 입력 클릭 시 api 호출
     */
    fun requestRecommendNickName(){
        apiService.requestRecommendNickName(
            RecommendNickNameBody(
                recNickName = recUserNk.value.toString()
            )
        )
            .compose(handleNetworkErrorRetry())
            .doLoading()
            .applyApiScheduler()
            .request({ res ->
                if (res.status == HttpStatusType.SUCCESS.status) {
                    couponStatusSuccess.value = res.message
                } else {
                    couponStatusFail.value = res.message
                }
            })
    }

}