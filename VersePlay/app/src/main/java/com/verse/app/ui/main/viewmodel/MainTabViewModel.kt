package com.verse.app.ui.main.viewmodel

import android.Manifest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.ExoPageType
import com.verse.app.contants.HttpStatusType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.handleNetworkErrorRetry
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.common.NoticeResponse
import com.verse.app.model.mypage.UploadSettingBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.exo.ExoProvider
import com.verse.app.widget.views.ExoPagerItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : Main Tab ViewModel
 *
 * Created by jhlee on 2023-01-01
 */
@HiltViewModel
class MainTabViewModel @Inject constructor(
    val accountPref: AccountPref,
    val exoProvider: ExoProvider,
    val loginManager: LoginManager,
    val apiService: ApiService,
    val deviceProvider: DeviceProvider,
) : FragmentViewModel() {

    //메인 -> 탭 메뉴
    val tabContents: ListLiveData<ExoPagerItem> by lazy {
        ListLiveData<ExoPagerItem>().apply {
            add(ExoPagerItem(name = ExoPageType.MAIN_FOLLOWING.pageName, type = ExoPageType.MAIN_FOLLOWING, isFirstPlay = false))                  //탭_피드
            add(ExoPagerItem(name = ExoPageType.MAIN_RECOMMEND.pageName, type = ExoPageType.MAIN_RECOMMEND, isFirstPlay = true))              //탭_추천
        }
    }
    val vpMainTabPosition: NonNullLiveData<Pair<Int, Boolean>> by lazy { NonNullLiveData(1 to false) }       //Tab viewPager 현재 position

    //검색 이동
    val _moveToSearch: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val moveToSearch: LiveData<Unit> get() = _moveToSearch

    // 메인 공지 팝업 노출 여부
    val isShowMainNoticePopup: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }
    val mainNoticePopupData: MutableLiveData<NoticeResponse> by lazy { MutableLiveData<NoticeResponse>() }
    val accountPrefOff: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // 알림 권한 비허용 시 pref n으로 바꾸게끔 하는 플래그 값

    fun onTabClick(pos: Int) {
        if (vpMainTabPosition.value.first != pos) {
            vpMainTabPosition.value = pos to true
        }
    }

    /**
     * 검색 이동
     */
    fun moveToSearchPage() {
        _moveToSearch.call()
    }

    /**
     * 공지 팝업
     */
    fun requestMainNoticeInfo() {
        DLogger.d("requestMainNoticeInfo isShowMainNoticePopup => ${isShowMainNoticePopup.value}")
        if (isShowMainNoticePopup.value) {
            return
        }

        apiService.fetchMainNoticePopup()
            .compose(handleNetworkErrorRetry())
            .applyApiScheduler()
            .request({ res ->
                DLogger.d("fetchMainNoticePopup=> ${res}")
                if (res.result != null) {
                    if (!res.result.svcPopMngCd.isEmpty() && res.result.imageList.size > 0) {
                        mainNoticePopupData.value = res
                        isShowMainNoticePopup.value = true
                    }
                }
            }, { error ->
                DLogger.d("Throwable ${error}")
            })
    }

    fun updateNightTimePushAgree(isOnSwitch: Boolean) {
        var updateValue: String

        if (isOnSwitch) {
            updateValue = AppData.Y_VALUE
        } else {
            updateValue = AppData.N_VALUE
        }

        apiService.updateSettings(
            UploadSettingBody(
                alRecTimeYn = updateValue
            )
        )
            .applyApiScheduler()
            .request({
                if (it.status == HttpStatusType.SUCCESS.status) {
                    DLogger.d("updateNightTimePushAgree Success")
                } else {
                    DLogger.d("updateNightTimePushAgree Fail")
                }
            }, {
                DLogger.d("updateNightTimePushAgree Error")
            })
    }


    /**
     * 알림 권한 거부 시
     * 푸시 전부 N으로 api 호출
     */
    fun updatePushSetting() {
        DLogger.d("updatePushSetting 탐")
        if (!deviceProvider.isPermissionsCheck(Manifest.permission.POST_NOTIFICATIONS)) {
            DLogger.d("deviceProvider.isPermissionsCheck(Manifest.permission.POST_NOTIFICATIONS) is PERMISSION_GRANTED x")
            apiService.updateSettings(
                UploadSettingBody(
                    alRecAllYn = AppData.N_VALUE,
                    alRecUplPrgYn = AppData.N_VALUE,
                    alRecUplFailYn = AppData.N_VALUE,
                    alRecUplComYn = AppData.N_VALUE,
                    alRecDorYn = AppData.N_VALUE,
                    alRecSuspYn = AppData.N_VALUE,
                    alRecMarketYn = AppData.N_VALUE,
                    alRecNorEvtYn = AppData.N_VALUE,
                    alRecFnVoteYn = AppData.N_VALUE,
                    alRecSeasonYn = AppData.N_VALUE,
                    alRecAllFlowYn = AppData.N_VALUE,
                    alRecAllFeedLikeYn = AppData.N_VALUE,
                    alRecLoungeLikeYn = AppData.N_VALUE,
                    alRecAllLikeRepYn = AppData.N_VALUE,
                    alRecDuetComYn = AppData.N_VALUE,
                    alRecBattleComYn = AppData.N_VALUE,
                    alRecFollowFeedYn = AppData.N_VALUE,
                    alRecFollowConYn = AppData.N_VALUE,
                    alRecDmYn = AppData.N_VALUE,
                    alRecAllDmYn = AppData.N_VALUE,
                    alRecTimeYn = AppData.N_VALUE
                )
            )
                .applyApiScheduler()
                .request({
                    if (it.status == HttpStatusType.SUCCESS.status) {
                        accountPrefOff.call()
                    } else {
                        DLogger.d("updatePushSetting Fail")
                    }
                }, {
                    DLogger.d("updatePushSetting Error")
                })

        } else {
            DLogger.d("deviceProvider.isPermissionsCheck(Manifest.permission.POST_NOTIFICATIONS) is PERMISSION_GRANTED")
        }
    }
}