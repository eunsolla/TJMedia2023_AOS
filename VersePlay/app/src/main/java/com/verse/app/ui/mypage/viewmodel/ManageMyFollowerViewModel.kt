package com.verse.app.ui.mypage.viewmodel

import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.BlockType
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.SortType
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.BlockUserListData
import com.verse.app.model.param.BlockBody
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.ui.login.activity.LoginActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ManageMyFollowerViewModel @Inject constructor(
    val apiService: ApiService,
    val repository: Repository,
    val resourceProvider: ResourceProvider,
    val deviceProvider: DeviceProvider,
    val accountPref: AccountPref,
    val loginManager: LoginManager,

    ) : ActivityViewModel(){
    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }// 뒤로가기

    // 팔로워 관리 (차단관리) 데이터
    private val _blockData: MutableLiveData<PagingData<BlockUserListData>> by lazy { MutableLiveData() }
    val blockData: MutableLiveData<PagingData<BlockUserListData>> get() = _blockData
    val refresh: SingleLiveEvent<Pair<Int, BlockUserListData>> by lazy { SingleLiveEvent() }
    val startOneButtonPopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }      // popup
    val isLoadingShow: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }               // 로딩바 show

    /**
     * 로딩바를 보여주지 않아야 하는 조건이 있어서 값에 따라 처리하는 함수
     */
    private fun handleLoadShow() {
        if (isLoadingShow.value) {
            onLoadingShow()
        }
    }

    /**
     * 뒤로 선택
     */
    fun back() {
        backpress.call()
    }

    /**
     * 차단 목록 리스트 데이터
     */
    fun requestBlockData(){
        repository.fetchMypageBlockList(SortType.DESC.name,this)
            .cachedIn(viewModelScope)
            .applyApiScheduler()
            .request({ response ->
                _blockData.value = response
            })

    }

    /**
     * 차단 해제
     */
    fun unBlock(position: Int, data: BlockUserListData){
        // 로그인 상태 확인
        if (!loginManager.isLogin()) {
            val page = ActivityResult(
                targetActivity = LoginActivity::class,
                data = bundleOf()
            )
            moveToPage(page)

            return
        }

        apiService.updateBlock(
            BlockBody(
                blockContentCode = data.conMngCd,
                blockYn = AppData.N_VALUE,
                blockType = BlockType.USER.code
            )
        )
            .applyApiScheduler()
            .doLoading()
            .request({ res ->
                if (res.status == HttpStatusType.SUCCESS.status) {
                    refresh.value = position to data
                }
            }, {
                DLogger.d("Error onBlock=>${it.message}")
            })
    }

}