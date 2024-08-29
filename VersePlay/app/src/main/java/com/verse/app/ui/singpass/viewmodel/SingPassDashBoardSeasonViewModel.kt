package com.verse.app.ui.singpass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.verse.app.R
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.*
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.param.SkipMissionBody
import com.verse.app.model.singpass.MissionItemData
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.utility.*
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : 씽패스 ViewModel
 *
 * Created by jhlee on 2023-03-20
 */
@HiltViewModel
class SingPassDashBoardSeasonViewModel @Inject constructor(
    val apiService: ApiService,
    val repository: Repository,
    val deviceProvider: DeviceProvider,
    val loginManager: LoginManager,
    val resourceProvider: ResourceProvider
) : FragmentViewModel() {

    val startFinish: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    private val _singPassMissionData: MutableLiveData<PagingData<MissionItemData>> by lazy { MutableLiveData() }
    val singPassMissionData: MutableLiveData<PagingData<MissionItemData>> get() = _singPassMissionData

    val requestSkipMission: MutableLiveData<MissionItemData> by lazy { MutableLiveData() }
    private val _miSkTpCd: MutableLiveData<String> by lazy { MutableLiveData() }
    val miSkTpCd: LiveData<String> get() = _miSkTpCd

    val startOneButtonPopup: SingleLiveEvent<String> by lazy { SingleLiveEvent() }      // popup

    fun requestSingPassMissionInfo(tabPageType: TabPageType) {
        var missionType: String? = null

        when (tabPageType) {
            TabPageType.DAILY_MISSION -> {
                missionType = MissionType.DAILY_MISSION.code
                _miSkTpCd.value = SingPassSkipMissionType.SK003.name
            }

            TabPageType.PERIOD_MISSION -> {
                missionType = MissionType.PERIOD_MISSION.code
                _miSkTpCd.value = SingPassSkipMissionType.SK003.name
            }

            TabPageType.SEASON_MISSION -> {
                missionType = MissionType.SEASON_MISSION.code
                _miSkTpCd.value = SingPassSkipMissionType.SK004.name
            }

            else -> {}
        }

        DLogger.d("missionType : $missionType")

        missionType?.let {
            repository.fetchSingPassMissionInfo(missionType!!)
                .cachedIn(viewModelScope)
                .doLoading()
                .applyApiScheduler()
                .request({ response ->
                    _singPassMissionData.value = response
                })
        }
    }

    /**
     * 미션 스킵 처리 요청
     */
    fun selectedMission(missionItemData: MissionItemData) {
        DLogger.d("Request Skip Mission Code : ${missionItemData.svcMiMngCd}")
        DLogger.d("Request Skip Mission Name : ${missionItemData.miMngNm}")
        DLogger.d("Request Skip Mission miSkTpCd : ${miSkTpCd.value}")

        if (missionItemData.fgCompletedYn.uppercase().equals(AppData.N_VALUE)) {
            missionItemData.let {
                requestSkipMission.value = it
            }
        } else {
            showToastIntMsg.value = R.string.season_sing_pass_mission_join_completed
        }
    }

    fun requestSkipMission(
        svcSeaMngCd: String,
        svcMiMngCd: String,
        svcMiRegTpCd: String,
        msTpCd: String,
    ) {

        miSkTpCd.value?.let {
            apiService.requestSkipMission(
                SkipMissionBody(
                    svcSeaMngCd = svcSeaMngCd,
                    svcMiMngCd = svcMiMngCd,
                    svcMiRegTpCd = svcMiRegTpCd,
                    miSkTpCd = it,
                ))
                .doLoading()
                .applyApiScheduler()
                .request({ res ->
                    if (res.status == HttpStatusType.SUCCESS.status) {
                        RxBus.publish(RxBusEvent.SingPassEvent(isRefresh = true))
                        requestSingPassMissionInfo(TabPageType.SEASON_MISSION)
                    } else {
                        startOneButtonPopup.value = res.message
                        DLogger.d("Fail Skip Mission => ${res.message}")
                    }
                }, { t ->
                    showToastStringMsg.value = t.message
                    DLogger.d("Error Skip Mission => ${t.message}")
                })

        }


    }

    fun close() {
        startFinish.call()
    }
}