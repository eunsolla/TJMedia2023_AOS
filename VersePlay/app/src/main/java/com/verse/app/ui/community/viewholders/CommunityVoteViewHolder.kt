package com.verse.app.ui.community.viewholders

import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.verse.app.R
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.ItemCommunityVoteBinding
import com.verse.app.extension.getFragmentActivity
import com.verse.app.extension.startAct
import com.verse.app.model.community.CommunityVoteData
import com.verse.app.model.vote.VoteIntentModel
import com.verse.app.ui.login.activity.LoginActivity
import com.verse.app.ui.vote.VoteRootActivity
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.moveToLoginAct

class CommunityVoteViewHolder(
    parent: ViewGroup,
    viewModel: BaseViewModel,
    itemBinding: ItemCommunityVoteBinding = createBinding(
        parent,
        R.layout.item_community_vote,
        viewModel
    )
) : BaseViewHolder<CommunityVoteData>(
    itemBinding
) {
    private val loginManager: LoginManager by lazy { entryPoint.loginManager()}

    init {
        itemBinding.root.setOnClickListener {
            // 로그인 상태 확인
            if (!loginManager.isLogin()) {
                val page = ActivityResult(
                    targetActivity = LoginActivity::class,
                    data = bundleOf()
                )

                if (itemView.getFragmentActivity() != null) {
                    itemView.getFragmentActivity()!!.moveToLoginAct()
                }
            } else {
                val data = itemBinding.data ?: return@setOnClickListener
                moveToVote(data)
            }
        }
    }

    private fun moveToVote(data: CommunityVoteData) {
        val act = itemView.getFragmentActivity() ?: return
        act.startAct<VoteRootActivity> {
            putExtra(ExtraCode.VOTE_DETAIL_CODE, VoteIntentModel(data))
        }
    }
}