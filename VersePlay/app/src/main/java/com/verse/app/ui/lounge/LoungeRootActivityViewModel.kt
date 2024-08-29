package com.verse.app.ui.lounge

import androidx.lifecycle.LiveData
import com.verse.app.base.viewmodel.ActivityViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.livedata.SingleLiveEvent
import com.verse.app.model.lounge.LoungeIntentModel
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.manager.UserSettingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/17
 */
@HiltViewModel
class LoungeRootActivityViewModel @Inject constructor(
    val loginManager: LoginManager
) : ActivityViewModel() {

    private val _startMoveLoungePage: SingleLiveEvent<LoungeIntentModel> by lazy { SingleLiveEvent() }
    val startMoveLoungePage: LiveData<LoungeIntentModel> get() = _startMoveLoungePage

    private val _startFinishEvent: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinishEvent: LiveData<Unit> get() = _startFinishEvent

    val startCheckPrivateAccount: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() } // 비공개 계정 여부 확인


    fun start() {

        val data: LoungeIntentModel? = savedStateHandle[ExtraCode.WRITE_LOUNGE_DATA]
        if (data == null) {
            _startFinishEvent.call()
            return
        }

        if (data.type == LoungeIntentModel.Type.WRITE && UserSettingManager.isPrivateUser()) {
            startCheckPrivateAccount.call()
        } else {
            _startMoveLoungePage.value = data
        }
    }
}
