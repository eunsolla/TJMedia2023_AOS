package com.verse.app.ui.search.viewholders

import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.verse.app.R
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.BookMarkType
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.SingType
import com.verse.app.databinding.ItemSearchPopularSongBinding
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.getFragmentActivity
import com.verse.app.model.param.BookMarkBody
import com.verse.app.model.search.SearchResultPopSongData
import com.verse.app.model.search.SearchResultSongData
import com.verse.app.repository.http.ApiService
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.login.activity.LoginActivity
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.manager.UserSettingManager
import com.verse.app.utility.moveToLoginAct
import com.verse.app.utility.moveToSingAct
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Description : 검색 결과 > 노래 ViewHolder
 *
 * Created by juhongmin on 2023/05/11
 */
class SearchSongViewHolder(
    parent: ViewGroup,
    viewModel: BaseViewModel,
    private val itemBinding: ItemSearchPopularSongBinding = createBinding(
        parent,
        R.layout.item_search_popular_song,
        viewModel
    )
) : BaseViewHolder<SearchResultPopSongData>(itemBinding), LifecycleEventObserver {

    private val loginManager: LoginManager by lazy { entryPoint.loginManager()}
    private val apiService: ApiService by lazy { entryPoint.apiService() }

    private var bookMarkDisposable: Disposable? = null

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

        itemBinding.ivFavorite.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            handleBookMark(model)
        }

        itemBinding.root.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            moveToSong(model)
        }

        itemBinding.tvSong.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            moveToSong(model)
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_PAUSE) {
            closeDisposable()
        }
    }

    private fun handleBookMark(data: SearchResultSongData) {
        // 로그인 상태 확인
        if (!loginManager.isLogin()) {
            val page = ActivityResult(
                targetActivity = LoginActivity::class,
                data = bundleOf()
            )

            if (itemView.getFragmentActivity() != null) {
                itemView.getFragmentActivity()!!.moveToLoginAct()
            }

            return
        }

        if (bookMarkDisposable != null) {
            closeDisposable()
        }

        bookMarkDisposable = apiService.updateBookMark(
            BookMarkBody(
                bookMarkType = BookMarkType.SONG.code,
                bookMarkYn = if (data.isBookMark) AppData.N_VALUE else AppData.Y_VALUE,
                contentsCode = data.songMngCd
            )
        )
            .applyApiScheduler()
            .doOnSuccess {
                if (it.status == HttpStatusType.SUCCESS.status) {
                    if (UserSettingManager.isPrivateUser() && !data.isBookMark) {
                        itemView.getFragmentActivity()?.let { act ->
                            CommonDialog(act)
                                .setContents(R.string.comment_private_account_popup)
                                .setPositiveButton(R.string.str_confirm)
                                .show()
                        }

                    } else {
                        data.isBookMark = !data.isBookMark
                        itemBinding.invalidateAll()
                    }

                }
            }.subscribe()
    }

    private fun moveToSong(data: SearchResultSongData) {
        // 로그인 상태 확인
        if (!loginManager.isLogin()) {
            val page = ActivityResult(
                targetActivity = LoginActivity::class,
                data = bundleOf()
            )

            if (itemView.getFragmentActivity() != null) {
                itemView.getFragmentActivity()!!.moveToLoginAct()
            }

            return
        }
        itemView.getFragmentActivity()?.moveToSingAct(SingType.SOLO.code,data.songMngCd)
    }

    /**
     * Rx 작업 하던거 취소하는 함수
     */
    private fun closeDisposable() {
        bookMarkDisposable?.dispose()
        bookMarkDisposable = null
    }
}
