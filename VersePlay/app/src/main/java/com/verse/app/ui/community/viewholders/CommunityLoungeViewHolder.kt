package com.verse.app.ui.community.viewholders

import android.view.ViewGroup
import com.verse.app.R
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.HttpStatusType
import com.verse.app.contants.LikeType
import com.verse.app.databinding.ItemCommunityLoungeBinding
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.getFragmentActivity
import com.verse.app.model.community.CommunityLoungeData
import com.verse.app.model.param.LikeBody
import com.verse.app.repository.http.ApiService
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.moveToLoginAct
import io.reactivex.rxjava3.disposables.Disposable


/**
 * Description : 커뮤니티 -> 라운지 뷰홀더
 *
 * Created by esna 2023_08_08
 */
class CommunityLoungeViewHolder(
    parent: ViewGroup,
    private val viewModel: BaseViewModel,
    private val itemBinding: ItemCommunityLoungeBinding = createBinding(
        parent,
        R.layout.item_community_lounge,
        viewModel
    )
)  : BaseViewHolder<CommunityLoungeData>(itemBinding){

    private val loginManager: LoginManager by lazy { entryPoint.loginManager()}
    private var disposable: Disposable? = null
    private val apiService: ApiService by lazy { entryPoint.apiService() }
    private var apiDisposable: Disposable? = null

    init {

        itemBinding.lyLike.setOnClickListener {
            // 로그인 상태 확인
            if (!loginManager.isLogin()) {
                if (itemView.getFragmentActivity() != null) {
                    itemView.getFragmentActivity()!!.moveToLoginAct()
                }
                return@setOnClickListener
            } else {
                val data = itemBinding.data ?: return@setOnClickListener
                handleLike(data)
            }
        }
    }


    private fun handleLike(data: CommunityLoungeData) {
        // 로그인 상태 확인
        if (!loginManager.isLogin()) {
            if (itemView.getFragmentActivity() != null) {
                itemView.getFragmentActivity()!!.moveToLoginAct()
            }
            return
        }
        val likeYn = if (data.isLike) AppData.N_VALUE else AppData.Y_VALUE

        val body = LikeBody(
            likeType = LikeType.LOUNGE.code,
            likeYn = likeYn,
            contentsCode = data.code
        )

        if (apiDisposable != null) {
            closeDisposable()
        }

        apiDisposable = apiService.updateLike(body)
            .applyApiScheduler()
            .doOnSuccess{
                if (it.status == HttpStatusType.SUCCESS.status) {
                    DLogger.d("onLike 라운지 result => $likeYn ${data.likeCount}")

                    data.fgLikeYn = likeYn

                    if (data.isLike) {
                        data.likeCount = data.likeCount.plus(1)
                    } else {
                        data.likeCount = Math.max(0, data.likeCount.minus(1))
                    }
                    data.isLike = !data.isLike
                    itemBinding.invalidateAll()
                }
            }.subscribe()
    }

    /**
     * Rx 작업 하던거 취소하는 함수
     */
    fun closeDisposable() {
        disposable?.dispose()
        disposable = null
    }

}