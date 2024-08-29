package com.verse.app.ui.singpass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.verse.app.R
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.onMain
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.singpass.SeasonInfo
import com.verse.app.model.singpass.SeasonItemData
import com.verse.app.model.singpass.SingPassDashBoardData
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import com.verse.app.widget.pagertablayout.PagerTabItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Description : 씽패스 ViewModel
 *
 * Created by jhlee on 2023-03-20
 */
@HiltViewModel
class SingPassDashBoardViewModel @Inject constructor(
    val apiService: ApiService,
    val repository: Repository,
    val deviceProvider: DeviceProvider,
    val loginManager: LoginManager,
    val resourceProvider: ResourceProvider
) : ActivityViewModel() {

    val startFinish: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    private val _singPassDashBoardData: MutableLiveData<SingPassDashBoardData> by lazy { MutableLiveData() }
    val singPassDashBoardData: LiveData<SingPassDashBoardData> get() = _singPassDashBoardData

    private val _seasonItemData: ListLiveData<SeasonItemData> by lazy { ListLiveData() }
    val seasonItemData: ListLiveData<SeasonItemData> get() = _seasonItemData

    val seasonInfo: LiveData<SeasonInfo> get() = Transformations.map(singPassDashBoardData) { it.seasonInfo }

    val vpTabState: MutableLiveData<Int> by lazy { MutableLiveData() }
    val vpTabPosition: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }

    val tabList: ListLiveData<PagerTabItem> by lazy {
        ListLiveData<PagerTabItem>().apply {
            add(PagerTabItem(title = resourceProvider.getString(R.string.season_sing_pass_daily_mission_title)))
            add(PagerTabItem(title = resourceProvider.getString(R.string.season_sing_pass_period_mission_title)))
            add(PagerTabItem(title = resourceProvider.getString(R.string.season_sing_pass_season_mission_title)))
        }
    }

    private val _isLoading: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }
    val isLoading: LiveData<Boolean> get() = _isLoading

    /**
     * api call - sing pss
     */
    fun requestSingPassDashBoardInfo(userMemCd: String) {

        apiService.getSingPassDashBoard(userMemCd)
            .doOnSubscribe { onLoadingShow() }
            .applyApiScheduler()
            .request(
                { response ->
                    _singPassDashBoardData.value = response.result
                    _seasonItemData.clear()
                    // 0823 획득 가능한 아이템 썸네일 없는 경우 기본 이미지로 대체하기 위해 필터 삭제함
                    _seasonItemData.value = response.result.seasonItemList.toMutableList()
                },
                { error ->
                    DLogger.d("##!!! Fail SingPass  ${error} ")
                    //startFinish.call()
                }
            )
    }

    /**
     * Tab ViewPager 상태 - Horizontal
     */
    fun pageTabState(state: Int) {
        vpTabState.value = state
    }

    fun setLoadFinished() {
        if (_isLoading.value) {
            onMain {
                delay(1000)
                _isLoading.value = false
                onLoadingDismiss()
            }
        }
    }

    fun close() {
        startFinish.call()
    }
}