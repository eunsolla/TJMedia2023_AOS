package com.verse.app.ui.mypage.viewmodel

import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.PlayStatus
import com.verse.app.contants.VolumeType
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.provider.DeviceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingPlayerViewModel @Inject constructor(
    val accountPref: AccountPref,
    val deviceProvider: DeviceProvider,
    val apiService: ApiService,

    ) : ActivityViewModel(){
    val mute: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }
    val auto: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }
    val typeBtnNotSelected: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val curVolume : NonNullLiveData<String> by lazy { NonNullLiveData("0") }
    val startClickChecked: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val curVolumeType: NonNullLiveData<VolumeType> by lazy { NonNullLiveData(VolumeType.VOLUME2) }                               // 현재 볼륨값 (1,2,3)
    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val saveData: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // 저장

    fun back() {
        backpress.call()
    }

    fun clickChecked(status: PlayStatus){
        when (status) {
            PlayStatus.SETTING_AUTO -> {
                auto.value = !auto.value

                if (auto.value){
                    mute.value = false
                    startClickChecked.call()
                } else {
                    typeBtnNotSelected.call()
                }
            }

            PlayStatus.SETTING_MUTE -> {
                mute.value = !mute.value

                if (mute.value){
                    auto.value = false
                    typeBtnNotSelected.call()
                }
            }
        }
//        changeSaveBtn.call()
    }

    fun saveData() {
        saveData.call()
    }

    /**
     * 자동 볼륨조절이 on 상태일때만 select 가능함
     * 자동볼륨조절 on 토글 선택 시 활성화 되며 기본값은 볼륨2
     */
    fun onVolumeType(volumeType: VolumeType) {
//        if (auto.value) changeSaveBtn.call()
        curVolumeType.value = volumeType
    }
    
}