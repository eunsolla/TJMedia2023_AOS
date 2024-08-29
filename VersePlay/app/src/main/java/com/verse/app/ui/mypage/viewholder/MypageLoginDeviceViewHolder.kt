package com.verse.app.ui.mypage.viewholder

import android.view.ViewGroup
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.verse.app.R
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.databinding.ItemLoginDeviceBinding
import com.verse.app.model.mypage.GetRecentLoginHistoryData
import com.verse.app.utility.manager.LoginManager
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Description : 마이페이지 > 로그인 기기 ViewHolder
 *
 * Created by esna
 */
class MypageLoginDeviceViewHolder(
    parent: ViewGroup,
    viewModel: BaseViewModel,
    private val itemBinding: ItemLoginDeviceBinding = createBinding(parent, R.layout.item_login_device, viewModel)
) : BaseViewHolder<GetRecentLoginHistoryData>(itemBinding), LifecycleEventObserver {

    private var disposable: Disposable? = null

    init {
        itemView.doOnAttach { v ->
            val owner = getLifecycleOwner(v) ?: return@doOnAttach
            owner.lifecycle.addObserver(this)
        }

        itemView.doOnDetach { v ->
            val owner = getLifecycleOwner(v) ?: return@doOnDetach
            owner.lifecycle.removeObserver(this)
            closeDisposable()
        }

        changeLoginType(itemBinding.data!!)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_PAUSE) {
            closeDisposable()
        }
    }

    private fun changeLoginType(data: GetRecentLoginHistoryData) {
        when (data.authTpCd) {
            LoginManager.LoginType.facebook.code -> {
                itemBinding.loginType.setText(R.string.common_login_popup_sign_in_with_facebook)
            }
            LoginManager.LoginType.google.code -> {
                itemBinding.loginType.setText(R.string.common_login_popup_sign_in_with_google)
            }
            LoginManager.LoginType.kakao.code -> {
                itemBinding.loginType.setText(R.string.common_login_popup_sign_in_with_kakao)
            }
            LoginManager.LoginType.naver.code -> {
                itemBinding.loginType.setText(R.string.common_login_popup_sign_in_with_naver)
            }
            LoginManager.LoginType.twitter.code -> {
                itemBinding.loginType.setText(R.string.common_login_popup_sign_in_with_Twitter)
            }
            else -> {
                itemBinding.loginType.setText(R.string.common_login_popup_sign_in_with_Snapchat)
            }
        }
    }

    /**
     * Rx 작업 하던거 취소하는 함수
     */
    private fun closeDisposable() {
        disposable?.dispose()
        disposable = null
    }

}