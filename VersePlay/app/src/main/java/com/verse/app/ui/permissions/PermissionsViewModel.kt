package com.verse.app.ui.permissions

import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.utility.provider.DeviceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : 퍼미션 ViewModel
 *
 * Created by jhlee on 2023-01-13
 */
@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val accountPref: AccountPref,
    val deviceProvider: DeviceProvider,
) : ActivityViewModel() {

    val startPermissions: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }

    fun onConfirm() {
        accountPref.setPermissionsPage(true)
        startPermissions.call()
    }
}