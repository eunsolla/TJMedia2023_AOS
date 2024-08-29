package com.verse.app.ui.search.viewholders

import android.view.ViewGroup
import android.widget.TextView
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
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.HttpStatusType
import com.verse.app.databinding.ItemSearchUserBinding
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.getFragmentActivity
import com.verse.app.extension.startAct
import com.verse.app.extension.toIntOrDef
import com.verse.app.model.mypage.MyPageIntentModel
import com.verse.app.model.param.FollowBody
import com.verse.app.model.search.SearchResultUserData
import com.verse.app.ui.login.activity.LoginActivity
import com.verse.app.ui.mypage.activity.MyPageRootActivity
import com.verse.app.usecase.PostFollowUseCase
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.moveToLoginAct
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Description : 검색 결과 > 사용자 팔로잉 정보 ViewHolder
 *
 * Created by juhongmin on 2023/05/11
 */
class SearchUserViewHolder(
    parent: ViewGroup,
    viewModel: BaseViewModel,
    private val itemBinding: ItemSearchUserBinding = createBinding(
        parent,
        R.layout.item_search_user,
        viewModel
    )
) : BaseViewHolder<SearchResultUserData>(itemBinding), LifecycleEventObserver {

    private val loginManager: LoginManager by lazy { entryPoint.loginManager() }
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

        itemBinding.ivSearchProfile.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            handleMoveMypage(model)
        }

        itemBinding.rvSearchUser.setOnClickListener {
            val model = itemBinding.data ?: return@setOnClickListener
            handleMoveMypage(model)
        }

    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_PAUSE) {
            closeDisposable()
        }
    }

    private fun handleFollow(data: SearchResultUserData, followerCnt: TextView) {
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
                        val followCount = data.followCount.toIntOrDef(0)
                        if (data.isFollowYn) {
                            data.followCount = followCount.plus(1).toString()
                        } else {
                            if (followCount > 0) {
                                data.followCount = followCount.minus(1).toString()
                            }
                        }
                        data.followCount
                    }
                    itemBinding.invalidateAll()
                }
            }.subscribe()
    }

    private fun handleMoveMypage(data: SearchResultUserData) {
        val fragmentActivity = itemView.getFragmentActivity()
        if (fragmentActivity != null) {
            if (loginManager.isLogin()) {
                fragmentActivity.startAct<MyPageRootActivity> {
                    putExtra(ExtraCode.MY_PAGE_DATA, MyPageIntentModel(data.memCd))
                }
            } else {
                fragmentActivity.moveToLoginAct()
            }
        }
    }

    /**
     * Rx 작업 하던거 취소하는 함수
     */
    private fun closeDisposable() {
        followerDisposable?.dispose()
        followerDisposable = null
    }
}
