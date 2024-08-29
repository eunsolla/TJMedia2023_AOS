package com.verse.app.ui.singpass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.verse.app.base.viewmodel.BaseFeedViewModel
import com.verse.app.contants.*
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.singpass.GenreRankingList
import com.verse.app.model.singpass.MemberRanking
import com.verse.app.model.singpass.SingPassData
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.exo.ExoProvider
import com.verse.app.utility.exo.ExoStyledPlayerView
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import javax.inject.Inject

/**
 * Description : 씽패스 ViewModel
 *
 * Created by jhlee on 2023-03-20
 */
@HiltViewModel
class SingPassViewModel @Inject constructor(
    val exoProvider: ExoProvider
) : BaseFeedViewModel() {

    init {
        _exoPageType.value = ExoPageType.MAIN_SING_PASS
    }

    private val _SingPassDataList: MutableLiveData<SingPassData> by lazy { MutableLiveData() }
    val singPassDataList: LiveData<SingPassData> get() = _SingPassDataList

    val requestDetailMyInfo: SingleLiveEvent<MemberRanking> by lazy { SingleLiveEvent() }
    val requestDetailUserInfo: SingleLiveEvent<GenreRankingList> by lazy { SingleLiveEvent() }
    val requestMoreRanking: SingleLiveEvent<String> by lazy { SingleLiveEvent() }
    val requestSingPassDashBoard: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val requestSingMain: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }

    // 멤버십 이용안내 페이지 이동 처리
    val requestShowMembershipPopup: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val detailMyInfo: SingleLiveEvent<MemberRanking> by lazy { SingleLiveEvent() }

    fun initStartRefresh() {
        RxBus.listen(RxBusEvent.RefreshDataEvent::class.java).subscribe({
            requestSingPass()
        }, {
            //error
        }).addTo(compositeDisposable)
    }

    /**
     * api call - sing pss
     */
    fun requestSingPass() {
        apiService.getSingPassMain()
            .doLoading()
            .applyApiScheduler()
            .doOnSuccess { response ->

                if (response.result.genreList.isNotEmpty()) {

                    val dataList = arrayListOf<String>().apply {
                        response.result.genreList.forEach { a ->
                           a.videoList?.forEach {
                               this.add(it.highConPath)
                           }
                        }
                    }

                    // 씽패스 진입 시 회원 상태 및 이용권 유형이 다를 경우 최신 상태로 변경 처리
                    response.result.genreList[0].memberRanking?.let {
                        if (loginManager.getUserLoginData().memStCd != it.memStCd) {
                            loginManager.getUserLoginData().memStCd = it.memStCd
                        }

                        if (loginManager.getUserLoginData().subscTpCd != it.subscTpCd) {
                            loginManager.getUserLoginData().subscTpCd = it.subscTpCd
                        }
                    }

                    goPreLoad(dataList)
                    _SingPassDataList.value = response.result
                } else {
                    _SingPassDataList.value = null
                    exoPlayerLock(true)
                }
                onLoadingDismiss()
            }
            .doOnError { error ->
                onLoadingDismiss()
                exoPlayerLock(true)
                DLogger.d("##!!! Fail SingPass  ${error} ")
                _SingPassDataList.value = null
            }
            .subscribe().addTo(compositeDisposable)
    }

    override fun setPlayerView(playerView: ExoStyledPlayerView) {
        super.setPlayerView(playerView)
        playerView?.let {
            if (it.getFeedItem().position == 0) {
                exoManager.onStartPlayer(0)
            }
        }
    }

    fun moveToDetailMyInfo(userInfo: MemberRanking) {
        requestDetailMyInfo.value = userInfo
    }

    fun moveToDetailUserInfo(userInfo: GenreRankingList) {
        if (!checkNoticeMembershipPopup()) {
            requestShowMembershipPopup.call()
        } else {
            requestDetailUserInfo.value = userInfo
        }
    }

    fun clickMoreButton(index: Int) {
        val genreCd = _SingPassDataList.value?.genreList?.get(index)?.genreCd
        DLogger.d("랭킹 더보기 버튼 클릭 : ${genreCd}")
        if (!checkNoticeMembershipPopup()) {
            requestShowMembershipPopup.call()
        }else {
            requestMoreRanking.value = genreCd
        }
    }

    fun clickJoinSingPassButton() {
        DLogger.d("부르기 메인 이동 요청")
        requestSingMain.call()
    }

    fun moveToSingPassDashBoard() {
        if (!checkNoticeMembershipPopup()) {
            requestShowMembershipPopup.call()
        } else {
            requestSingPassDashBoard.call()
        }
    }

    fun setData(singPassData: SingPassData) {
        _SingPassDataList.value = singPassData
    }

    fun checkNoticeMembershipPopup(): Boolean {
        return _SingPassDataList.value?.seasonInfo?.let {
            it.fgVipYn == AppData.Y_VALUE
        } ?: run { false }
    }

    /**
     * singpass 새로운 시즌인 경우 재생중인 영상 pause
     */
    fun exoPlayerLock(isLock: Boolean) {
        exoProvider.setExoLock(isLock)
    }
}