package com.verse.app.ui.mypage.viewholder

import android.view.ViewGroup
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.BookMarkType
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.SingType
import com.verse.app.databinding.ItemAccompanimentBinding
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.getFragmentActivity
import com.verse.app.model.feed.CommonAccompanimentData
import com.verse.app.model.param.BookMarkBody
import com.verse.app.repository.http.ApiService
import com.verse.app.ui.mypage.bookmark.MyPageBookmarkAccompanimentViewModel
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.moveToLoginAct
import com.verse.app.utility.moveToSingAct
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Description : 마이 페이지 > 반주음 ViewHolder
 *
 * Created by juhongmin on 2023/06/10
 */
class MyPageAccompanimentViewHolder(
    parent: ViewGroup,
    viewModel: BaseViewModel,
    private val itemBinding: ItemAccompanimentBinding = createBinding(
        parent,
        R.layout.item_accompaniment,
        viewModel
    )
) : BaseViewHolder<CommonAccompanimentData>(itemBinding), LifecycleEventObserver {

    private val apiService: ApiService by lazy { entryPoint.apiService() }
    private val loginManager: LoginManager by lazy { entryPoint.loginManager() }
    private var apiDisposable: Disposable? = null

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

        if (viewModel is MyPageBookmarkAccompanimentViewModel) {
            if (loginManager.getUserLoginData().memCd == viewModel.memberCode) {
                itemBinding.setVariable(BR.isUser, false)
            } else {
                // User
                itemBinding.setVariable(BR.isUser, true)
            }
        }

        itemBinding.ivFavorite.setOnClickListener {
            val data = itemBinding.data ?: return@setOnClickListener
            handleFavorite(data)
        }

        itemBinding.tvSong.setOnClickListener {
            val data = itemBinding.data ?: return@setOnClickListener
            moveToSong(data)
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_PAUSE) {
            closeDisposable()
        }
    }

    private fun handleFavorite(data: CommonAccompanimentData) {
        if (loginManager.isLogin()) {
            val bookMarkYn = if (data.isBookMark) AppData.N_VALUE else AppData.Y_VALUE
            val body = BookMarkBody(
                bookMarkType = BookMarkType.SONG.code,
                bookMarkYn = bookMarkYn,
                contentsCode = data.songMngCd
            )
            if (apiDisposable != null) {
                closeDisposable()
            }
            apiDisposable = apiService.updateBookMark(body)
                .applyApiScheduler()
                .doOnSuccess {
                    if (it.status == HttpStatusType.SUCCESS.status) {
                        data.isBookMark = !data.isBookMark
                        itemBinding.invalidateAll()
                    }
                }
                .subscribe()
        } else {
            val fragmentActivity = itemView.getFragmentActivity() ?: return
            fragmentActivity.moveToLoginAct()
        }
    }

    private fun moveToSong(data: CommonAccompanimentData) {
        val fragmentActivity = itemView.getFragmentActivity() ?: return
        fragmentActivity.moveToSingAct(SingType.SOLO.code, data.songMngCd, "")
    }

    private fun closeDisposable() {
        if (apiDisposable != null) {
            apiDisposable?.dispose()
            apiDisposable = null
        }
    }
}
