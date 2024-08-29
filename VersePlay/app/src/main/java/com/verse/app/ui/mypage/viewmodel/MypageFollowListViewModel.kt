package com.verse.app.ui.mypage.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.verse.app.R
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.*
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.base.BaseModel
import com.verse.app.model.mypage.FollowIntentModel
import com.verse.app.model.mypage.GetFollowCountData
import com.verse.app.model.mypage.GetMyFollowListData
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.*
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.provider.ResourceProvider
import com.verse.app.widget.pagertablayout.PagerTabItem
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MypageFollowListViewModel @Inject constructor(
    val deviceProvider: DeviceProvider,
    val apiService: ApiService,
    val accountPref: AccountPref,
    val repository: Repository,
    val resourceProvider: ResourceProvider,
    val loginManager: LoginManager,

    ) : FragmentViewModel() {

    private val _startFinishEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinishEvent: LiveData<Unit> get() = _startFinishEvent

    private val _uiList: ListLiveData<BaseModel> by lazy { ListLiveData() }
    val uiList: ListLiveData<BaseModel> get() = _uiList

    //[s]가로 -> Fragment ViewPager  (Following/Follower ) - Horizontal
    val tabList: ListLiveData<PagerTabItem> by lazy {
        ListLiveData<PagerTabItem>().apply {
            add(PagerTabItem(title = resourceProvider.getString(R.string.search_filter_order_by_follower)))          // 팔로워
            add(PagerTabItem(title = resourceProvider.getString(R.string.set_update_follow_by_following)))           // 팔로잉
        }
    }
    val isVpTabAni: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }                             // ViewPager Ani 여부
    val vpTabPosition: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }                                           //Default tab position => 1

    //[s]followInfo
    private val memberData: MutableLiveData<GetFollowCountData> by lazy { MutableLiveData() }
    val followerCnt: LiveData<String> get() = Transformations.map(memberData) { it.followerCount.toString() }
    val followingCnt: LiveData<String> get() = Transformations.map(memberData) { it.followingCount.toString() }

    val _myNk: MutableLiveData<String> by lazy { MutableLiveData() }
    val myNk: LiveData<String> get() = _myNk                                                                 // 닉네임
    val _myCd: MutableLiveData<String> by lazy { MutableLiveData() }
    val myCd: LiveData<String> get() = _myCd                                                                 // 내 cd

    val userMemCd: SingleLiveEvent<String> by lazy { SingleLiveEvent() }                                            // 유저 코드

    private val _myFollowerList: MutableLiveData<PagingData<GetMyFollowListData>> by lazy { MutableLiveData() }     // 팔로워 리스트
    val myFollowerList: LiveData<PagingData<GetMyFollowListData>> get() = _myFollowerList

    private val _myFollowingList: MutableLiveData<PagingData<GetMyFollowListData>> by lazy { MutableLiveData() }     // 팔로잉 리스트
    val myFollowingList: LiveData<PagingData<GetMyFollowListData>> get() = _myFollowingList

    val isMypage: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }               // 마이페이지면 true 타유저페이지면 false
    val isLoadingShow: NonNullLiveData<Boolean> by lazy { NonNullLiveData(true) }               // 로딩바 show
    val goToUserMypage: SingleLiveEvent<Pair<Int, GetMyFollowListData>> by lazy { SingleLiveEvent() }   // 타 유저 마이페이지로 이동

//    private val _refresh: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }
//    val refresh: SingleLiveEvent<Int> get() = _refresh

    /**
     * 로딩바를 보여주지 않아야 하는 조건이 있어서 값에 따라 처리하는 함수
     */
    private fun handleLoadShow() {
        if (isLoadingShow.value) {
            onLoadingShow()
        }
    }

    /**
     * memcd 존재 여부 판단
     * memcd 있으면 타유저 팔로우 페이지
     * 없으면 내 팔로우 페이지
     * isFollowing true -> 팔로잉 페이지
     * isFollowing false -> 팔로워 페이지
     */
    fun start() {
        val intentData = getIntentData<FollowIntentModel>(ExtraCode.FOLLOW_DATA)
        if (intentData == null) {
            _startFinishEvent.call()
            return
        }

        // IntentData 를 가져와서 처리합니다.
        vpTabPosition.value = intentData.initTabPosition // 팔로워 팔로잉 포지션
        isMypage.value = intentData.isMyPage // 마이페이지인지 유저페이지인지

        if (isMypage.value) {
            _myNk.value = intentData.memberNickname
            _myCd.value = intentData.memberCode
            getMyFollowCountApi()
        } else {
            _myNk.value = intentData.memberNickname
            userMemCd.value = intentData.memberCode
            getUserFollowCountApi(userMemCd.value.toString())

        }
    }

    /**
     * 뒤로 선택
     */
    fun back() {
        _startFinishEvent.call()
    }

    fun onTabClick(pos: Int) {
        if (vpTabPosition.value != pos) {
            vpTabPosition.value = pos
        }

        if (vpTabPosition.value == 0){
            if (isMypage.value) {
                getFollowerApi()
            } else {
                getUserFollowerApi(userMemCd.value.toString())
            }

        } else {
            if (isMypage.value) {
                getFollowingApi()
            } else {
                getUserFollowingApi(userMemCd.value.toString())
            }
        }
    }

    /**
     * page create
     */
    fun pageTabState(state: Int) {
        if (state == 0) {
            if (isMypage.value) {
                getFollowerApi()
            } else {
                getUserFollowerApi(userMemCd.value.toString())
            }

        } else {
            if (isMypage.value) {
                getFollowingApi()
            } else {
                getUserFollowingApi(userMemCd.value.toString())
            }
        }
    }

    /**
     * 내 팔로워 팔로잉 카운트 get
     */
    fun getMyFollowCountApi() {
        apiService.getMyFollowCount()
            .applyApiScheduler()
            .request({
                memberData.value = it.result
            }, { error ->
                DLogger.d("Error  ${error.message}")
            })
    }

    /**
     * 내 팔로잉 리스트 get
     */
    fun getFollowingApi() {
        repository.fetchMyFollowing(SortType.DESC.name, FollowType.FOLLOWING.code,this)
            .cachedIn(viewModelScope)
            .applyApiScheduler()
            .request({ response ->
                _myFollowingList.value = response
            })
    }

    /**
     * 내 팔로워 리스트 get
     */
    fun getFollowerApi() {
        repository.fetchMyFollower(SortType.DESC.name, FollowType.FOLLOWER.code,this)
            .cachedIn(viewModelScope)
            .applyApiScheduler()
            .request({ response ->
                _myFollowerList.value = response
            })
    }

    /**
     * 상대방 팔로워 팔로잉 카운트 get
     */
    fun getUserFollowCountApi(memcd: String) {
        apiService.getUserFollowCount(
            userMemCd = memcd
        )
            .applyApiScheduler()
            .request({
                memberData.value = it.result
            }, { error ->
                DLogger.d("Error  ${error.message}")
            })
    }

    /**
     * 타 유저 팔로잉 리스트 get
     */
    fun getUserFollowingApi(memcd: String) {
        repository.fetchUserFollowing(SortType.DESC.name, memcd,FollowType.FOLLOWING.code,this)
            .applyApiScheduler()
            .cachedIn(viewModelScope)
            .request({ response ->
                _myFollowingList.value = response
            })

    }

    /**
     * 타 유저 팔로워 리스트 get
     */
    fun getUserFollowerApi(memcd: String) {
        repository.fetchUserFollower(SortType.DESC.name, memcd, FollowType.FOLLOWER.code,this)
            .applyApiScheduler()
            .cachedIn(viewModelScope)
            .request({ response ->
                _myFollowerList.value = response
            })
    }

    /**
     * 사용자 상세 페이지 이동
     */
    fun moveUserMypage(position: Int, data: GetMyFollowListData) {
        goToUserMypage.value = position to data
    }

    /**
     * 팔로잉/팔로우 해제 시 숫자 갱신
     */
    fun initFollowingList() {
        RxBus.listen(RxBusEvent.FollowRefreshEvent::class.java)
            .delay(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                if (isMypage.value) {
                    getMyFollowCountApi()
                    isLoadingShow.value = false
                } else {
                    getUserFollowCountApi(userMemCd.value.toString())
                    isLoadingShow.value = false
                }
            }
            .subscribe().addTo(compositeDisposable)
    }

    /**
     * 다른 유저 마이페이지 -> 팔로잉/팔로우 해제 시 숫자 및 팔로우 목록 갱신
     */
    fun initMypageFollowingList() {
        RxBus.listen(RxBusEvent.MypageFollowRefreshEvent::class.java)
            .delay(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                if (isMypage.value) {
                    getMyFollowCountApi()
                    isLoadingShow.value = false
                    if (vpTabPosition.value == 0){
                        getFollowerApi()
                    } else {
                        getFollowingApi()
                    }
                } else {
                    getUserFollowCountApi(userMemCd.value.toString())
                    isLoadingShow.value = false
                    if (vpTabPosition.value == 0){
                        getUserFollowerApi(userMemCd.value.toString())
                    } else {
                        getUserFollowingApi(userMemCd.value.toString())
                    }
                }
            }
            .subscribe().addTo(compositeDisposable)
    }

    /**
     * 목록에 본인 있으면 팔로우 버튼 gone
     */
    fun isMine(data: GetMyFollowListData): Boolean {
        return data.memCd == loginManager.getUserLoginData().memCd
    }



}