package com.verse.app.ui.community.vote

import androidx.lifecycle.MutableLiveData
import com.verse.app.base.viewmodel.FragmentViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : 커뮤니 > [투포]
 *
 * Created by juhongmin on 2023/05/16
 */
@HiltViewModel
class CommunityVoteFragmentViewModel @Inject constructor(

) : FragmentViewModel(){

    val tabPosition: MutableLiveData<Int> by lazy { MutableLiveData() }

    fun moveTabPosition(pos: Int) {
        if (tabPosition.value != pos) {
            tabPosition.value = pos
        }
    }
}
