package com.verse.app.ui.mypage.viewholder

import android.view.ViewGroup
import com.verse.app.R
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.ItemRecommendUserBinding
import com.verse.app.extension.getFragmentActivity
import com.verse.app.extension.startAct
import com.verse.app.model.mypage.MyPageIntentModel
import com.verse.app.model.mypage.RecommendUserData
import com.verse.app.ui.mypage.activity.MyPageRootActivity

/**
 * Description : 마이페이지 > 추천 유저 아이템 ViewHolder
 *
 * Created by juhongmin on 2023/05/31
 */
class MyPageRecommendUserViewHolder(
    parent: ViewGroup,
    viewModel: BaseViewModel,
    private val itemBinding: ItemRecommendUserBinding = createBinding(
        parent,
        R.layout.item_recommend_user,
        viewModel
    )
) : BaseViewHolder<RecommendUserData>(itemBinding) {

    init {
        itemBinding.root.setOnClickListener {
            val data = itemBinding.data ?: return@setOnClickListener
            moveToUserPage(data)
        }
    }

    private fun moveToUserPage(data: RecommendUserData) {
        itemView.getFragmentActivity()?.let {
            it.startAct<MyPageRootActivity> {
                putExtra(ExtraCode.MY_PAGE_DATA, MyPageIntentModel(data.memCd))
            }
        }
    }
}
