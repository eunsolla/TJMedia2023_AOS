package com.verse.app.ui.push

import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.repository.preferences.AccountPref
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : push message
 *
 * Created by esna on 2023-04-18
 */

@HiltViewModel
class PushRouterViewModel @Inject constructor(
    private val accountPref: AccountPref
) : ActivityViewModel() {

}