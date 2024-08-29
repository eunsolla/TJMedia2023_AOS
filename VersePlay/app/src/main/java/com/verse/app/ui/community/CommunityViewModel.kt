package com.verse.app.ui.community

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.verse.app.R
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.ExtraCode.WRITE_LOUNGE_DATA
import com.verse.app.extension.applyApiScheduler
import com.verse.app.livedata.ListLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.community.CommunityMainBannerData
import com.verse.app.model.lounge.LoungeIntentModel
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.ui.login.activity.LoginActivity
import com.verse.app.ui.lounge.LoungeRootActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.provider.ResourceProvider
import com.verse.app.widget.pagertablayout.PagerTabItemV2
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import javax.inject.Inject

/**
 * Description : Main ViewModel
 *
 * Created by jhlee on 2023-03-20
 */
@HiltViewModel
class CommunityViewModel @Inject constructor(
    val apiService: ApiService,
    val repository: Repository,
    val resProvider: ResourceProvider,
    val deviceProvider: DeviceProvider,
    val loginManager: LoginManager,
) : FragmentViewModel() {

    val tabList: ListLiveData<PagerTabItemV2> by lazy { ListLiveData() }

    val tabPosition: MutableLiveData<Int> by lazy { MutableLiveData() }

    private val _bannerList: ListLiveData<CommunityMainBannerData> by lazy { ListLiveData() }
    val bannerList: ListLiveData<CommunityMainBannerData> get() = _bannerList
    private val _isEmpty: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val isEmpty: LiveData<Boolean> get() = _isEmpty

    val clickBannerEvent: SingleLiveEvent<CommunityMainBannerData> by lazy { SingleLiveEvent() }

    fun start() {
        tabList.add(PagerTabItemV2(title = R.string.community_tab_lounge))
        tabList.add(PagerTabItemV2(title = R.string.community_tab_event))
        tabList.add(PagerTabItemV2(title = R.string.community_tab_vote))
        val naviTabPostion: Int? = savedStateHandle[ExtraCode.COMMUNITY_ENTER_TYPE]
        tabPosition.value = naviTabPostion

        repository.fetchCommunityMainBannerList()
            .applyApiScheduler()
            .doOnSuccess { handleOnSuccess(it) }
            .doOnError { onLoadingDismiss() }
            .subscribe().addTo(compositeDisposable)
    }

    private fun handleOnSuccess(list: List<CommunityMainBannerData>) {
        if (list.isEmpty()) {
            _isEmpty.value = true
        } else {
            _isEmpty.value = false
            _bannerList.clear()
            _bannerList.addAll(list)
        }
        onLoadingDismiss()
    }

    fun moveToBannerDetail(data: CommunityMainBannerData) {
        DLogger.d("배너 클릭 클릭! $data")
        val code = data.bannerCode ?: return
        clickBannerEvent.value = data
    }

    fun moveToWriteLounge() {
        if (loginManager.isLogin()) {
            moveToPage(
                ActivityResult(
                    targetActivity = LoungeRootActivity::class,
                    data = bundleOf(WRITE_LOUNGE_DATA to LoungeIntentModel())
                )
            )
        } else {
            moveToPage(
                ActivityResult(
                    targetActivity = LoginActivity::class,
                    data = bundleOf()
                )
            )
        }
    }
}