package com.verse.app.ui.community.event

import androidx.lifecycle.MutableLiveData
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.utility.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : 커뮤니티 > [이벤트]
 *
 * Created by juhongmin on 2023/05/16
 */
@HiltViewModel
class CommunityEventFragmentViewModel @Inject constructor(
    private val resProvider: ResourceProvider
) : FragmentViewModel() {

    val tabPosition: MutableLiveData<Int> by lazy { MutableLiveData() }

    fun moveTabPosition(pos: Int) {
        if (tabPosition.value != pos) {
            tabPosition.value = pos
        }
    }
}