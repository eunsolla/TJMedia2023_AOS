package com.verse.app.ui.mypage.viewholder

import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.verse.app.R
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.HttpStatusType
import com.verse.app.databinding.ItemFollowListBinding
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.request
import com.verse.app.model.mypage.GetMyFollowListData
import com.verse.app.model.param.FollowBody
import com.verse.app.repository.http.ApiService
import com.verse.app.usecase.PostFollowUseCase
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Description : 마이페이지 > 팔로잉/팦로우 목록 아이템 ViewHolder
 *
 * Created by esna
 */
class MyPageFollowListViewHolder(
    parent: ViewGroup,
    viewModel: BaseViewModel,
    private val itemBinding: ItemFollowListBinding = createBinding(
        parent,
        R.layout.item_follow_list,
        viewModel
    )
) : BaseViewHolder<GetMyFollowListData>(itemBinding), LifecycleEventObserver {

    private val apiService: ApiService by lazy { entryPoint.apiService() }
    private val postFollowUseCase: PostFollowUseCase by lazy { entryPoint.postFollowUseCase() }

    private var followerDisposable: Disposable? = null

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

        itemBinding.tvFollower.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            handleFollow(model, itemBinding.followerCnt)
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_PAUSE) {
            closeDisposable()
        }
    }

    private fun handleFollow(data: GetMyFollowListData, followerCnt: TextView) {
        if (followerDisposable != null) {
            closeDisposable()
        }

        followerDisposable = postFollowUseCase(
            FollowBody(
                data.memCd,
                !data.isFollowYn
            )
        ).applyApiScheduler()
            .doOnSuccess {
                if (it.status == HttpStatusType.SUCCESS.status) {
                    data.isFollowYn = !data.isFollowYn

                    followerCnt.text = run {
                        if (data.isFollowYn) {
                            data.followerCnt = data.followerCnt + 1
                        } else {
                            if (data.followerCnt > 0) {
                                data.followerCnt = data.followerCnt - 1
                            }
                        }
                        data.followerCnt.toString()
                    }
                    RxBus.publish(RxBusEvent.FollowRefreshEvent(data.memCd))
                    reqRefreshMyProfile()
                    itemBinding.invalidateAll()
                }
            }.subscribe()
    }

    /**
     * 마이페이지에서 팔로우/팔로워 수 갱신 처리하기 위해 api 호출
     */
    fun reqRefreshMyProfile() {
        apiService.fetchMyProfileInfo()
            .map { it.result }
            .applyApiScheduler()
            .request(success = {
                RxBus.publish(RxBusEvent.MyPageRefreshEvent(it))
            })
    }

    /**
     * Rx 작업 하던거 취소하는 함수
     */
    private fun closeDisposable() {
        followerDisposable?.dispose()
        followerDisposable = null
    }

}