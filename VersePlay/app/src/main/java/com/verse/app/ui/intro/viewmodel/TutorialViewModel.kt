package com.verse.app.ui.intro.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.verse.app.R
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.NationLanType
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.mypage.TutorialItemData
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : Tutorial Viewmodel Class
 *
 * Created by esna on 2023-03-17
 */

@HiltViewModel
class TutorialViewModel @Inject constructor(
    private val apiService: ApiService,
    val repository: Repository,
    val resourceProvider: ResourceProvider,
    val deviceProvider: DeviceProvider,
    val accountPref: AccountPref,

    ) : ActivityViewModel() {

    val startMain = SingleLiveEvent<Unit>()
    val startFinish: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val vpTutorialState: MutableLiveData<Int> by lazy { MutableLiveData() } // 튜토리얼 페이지 상태
    val vpTutorialPosition: NonNullLiveData<Int> by lazy { NonNullLiveData(0) } // 현재 튜토리얼 이미지 포지션
    // 현재 indicator select 유무 상태
    private val _isTextviewSelected = NonNullLiveData(false)
    val isTextviewSelected: LiveData<Boolean> get() = _isTextviewSelected
    val backpress: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }                       // 뒤로가기
    val isUserGuide: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }

    fun getImageList(): ArrayList<TutorialItemData> {
        var mList = ArrayList<TutorialItemData>()
        return mList.apply {
            if (accountPref.isPreferenceLocaleLanguage() == NationLanType.KO.code) add(TutorialItemData(R.drawable.s_singpass_kr, getTitle(0), getSubTitle(0))) else add(TutorialItemData(R.drawable.s_singpass_en, getTitle(0), getSubTitle(0)))
            if (accountPref.isPreferenceLocaleLanguage() == NationLanType.KO.code) add(TutorialItemData(R.drawable.sing_a_song_kr, getTitle(1), getSubTitle(1))) else add(TutorialItemData(R.drawable.sing_a_song_en, getTitle(1), getSubTitle(1)))
            if (accountPref.isPreferenceLocaleLanguage() == NationLanType.KO.code) add(TutorialItemData(R.drawable.upload_personal_album_kr, getTitle(2), getSubTitle(2))) else add(TutorialItemData(R.drawable.upload_personal_album_en, getTitle(2), getSubTitle(2)))
            if (accountPref.isPreferenceLocaleLanguage() == NationLanType.KO.code) add(TutorialItemData(R.drawable.community_kr, getTitle(3), getSubTitle(3))) else add(TutorialItemData(R.drawable.community_en, getTitle(3), getSubTitle(3)))
            if (accountPref.isPreferenceLocaleLanguage() == NationLanType.KO.code) add(TutorialItemData(R.drawable.my_page_kr, getTitle(4), getSubTitle(4))) else add(TutorialItemData(R.drawable.my_page_en, getTitle(4), getSubTitle(4)))
        }
    }

    /**
     * 튜토리얼 이동
     */
    fun startMain() {
        startMain.call()
    }

    fun getTitle(position: Int): String {
        var result: String = ""

        if (position == 0) {
            result = resourceProvider.getString(R.string.tutorial_singpass_title)
        } else if (position == 1) {
            result = resourceProvider.getString(R.string.tutorial_sing_title)
        } else if (position == 2) {
            result = resourceProvider.getString(R.string.tutorial_album_title)
        } else if (position == 3) {
            result = resourceProvider.getString(R.string.tutorial_community_title)
        } else if (position == 4) {
            result = resourceProvider.getString(R.string.tutorial_mypage_title)
        }

        return result
    }

    fun getSubTitle(position: Int): String {
        var result: String = ""

        if (position == 0) {
            result = resourceProvider.getString(R.string.tutorial_singpass_sub_title)
        } else if (position == 1) {
            result = resourceProvider.getString(R.string.tutorial_sing_sub_title)
        } else if (position == 2) {
            result = resourceProvider.getString(R.string.tutorial_album_sub_title)
        } else if (position == 3) {
            result = resourceProvider.getString(R.string.tutorial_community_sub_title)
        } else if (position == 4) {
            result = resourceProvider.getString(R.string.tutorial_mypage_sub_title)
        }

        return result
    }

    /**
     * 뒤로 선택
     */
    fun back() {
        backpress.call()
    }

    /**
     * ViewPager 상태
     * @param state ViewPager2.State
     */
    fun pageTutorialState(state: Int) {
        vpTutorialState.value = state
    }

    fun tvSelected(isSelected: Boolean){
        _isTextviewSelected.postValue(isSelected)
    }
}