package com.verse.app.ui.singpass.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.AppData
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.singpass.MemberRanking
import com.verse.app.model.singpass.SingPassRankingInfo
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Description : 씽패스 ViewModel
 *
 * Created by jhlee on 2023-03-20
 */
@HiltViewModel
class SingPassRankingListViewModel @Inject constructor(
    val apiService: ApiService,
    val repository: Repository,
    val deviceProvider: DeviceProvider,
    val loginManager: LoginManager,
    val resourceProvider: ResourceProvider
) : ActivityViewModel() {

    val startFinish: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    private val _singPassRankingData: MutableLiveData<PagingData<SingPassRankingInfo>> by lazy { MutableLiveData() }
    val singPassRankingData: MutableLiveData<PagingData<SingPassRankingInfo>> get() = _singPassRankingData

    private val _singPassMyRankingData: MutableLiveData<MemberRanking> by lazy { MutableLiveData() }
    val singPassMyRankingData: MutableLiveData<MemberRanking> get() = _singPassMyRankingData

    val requestDetailMyInfo: SingleLiveEvent<MemberRanking> by lazy { SingleLiveEvent() }
    val requestDetailUserInfo: SingleLiveEvent<SingPassRankingInfo> by lazy { SingleLiveEvent() }

    /**
     * api call - sing pss
     */
    fun requestSingPassRankingList(genreCd: String, listYn: String) {
        if (listYn.uppercase() == AppData.Y_VALUE) {
            repository.fetchSingPassRanking(genreCd, listYn)
                .doLoading()
                .delay(500, TimeUnit.MILLISECONDS)
                .applyApiScheduler()
                .cachedIn(viewModelScope)
                .request({ response ->
                    _singPassRankingData.value = response
                })

        } else {
            apiService.getSingPassRanking(
                genreCd,
                listYn,
                1,
            )
                .doLoading()
                .delay(500, TimeUnit.MILLISECONDS)
                .applyApiScheduler()
                .request(
                    { response ->
                        _singPassMyRankingData.value = response.result.memberRanking
                    },
                    { error ->
                        DLogger.d("##!!! Fail SingPass  ${error} ")
                        //startFinish.call()
                    }
                )
        }
    }

    fun moveToDetailMyInfo(userInfo: MemberRanking){
        DLogger.d("moveToDetailUserInfo ${userInfo.memCd}")
        requestDetailMyInfo.value = userInfo
    }

    fun moveToDetailUserInfo(userInfo: SingPassRankingInfo){
        DLogger.d("moveToDetailUserInfo ${userInfo.memCd}")
        requestDetailUserInfo.value = userInfo
    }

    fun close() {
        startFinish.call()
    }
}