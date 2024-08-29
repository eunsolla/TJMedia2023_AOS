package com.verse.app.ui.mypage.like

import androidx.lifecycle.LiveData
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.livedata.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/29
 */
@HiltViewModel
class MyPageLikeTabFragmentViewModel @Inject constructor(

) : FragmentViewModel() {
    // 0 -> 공연
    // 1 -> 개인소장
    val tabPosition: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }
    private val _startFilterDialogEvent: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }
    val startFilterDialogEvent: LiveData<Int> get() = _startFilterDialogEvent

    fun onTabClick(pos: Int) {
        if (tabPosition.value != pos) {
            tabPosition.value = pos
        }
    }


    fun showFilter() {
        _startFilterDialogEvent.value = tabPosition.value
    }
}
