package com.verse.app.ui.singpass.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.TabPageType
import com.verse.app.repository.http.ApiService
import com.verse.app.repository.paging.Repository
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.provider.ResourceProvider
import com.verse.app.utility.exo.ExoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : Main ViewModel
 *
 * Created by jhlee on 2023-03-20
 */
@HiltViewModel
class TabSingPassMissionViewModel @Inject constructor(
    val apiService: ApiService,
    val repository: Repository,
    val resourceProvider: ResourceProvider,
    val deviceProvider: DeviceProvider,
    val exoManager: ExoManager,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel() {

    //현재 페이지 타입
    //MY_PAGE_UPLOAD
    //MY_PAGE_LIKE
    //MY_PAGE_FAVORIT
    val pageParam = savedStateHandle.getLiveData<TabPageType>(ExtraCode.TAB_TYPE) // 탭 페이지 타입


}