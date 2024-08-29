package com.verse.app.ui.community.viewholders

import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.verse.app.R
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewholder.BaseViewHolder
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.ItemCommunityEventBinding
import com.verse.app.extension.getFragmentActivity
import com.verse.app.extension.startAct
import com.verse.app.model.community.CommunityEventData
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.events.EventDetailActivity
import com.verse.app.ui.login.activity.LoginActivity
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.moveToLoginAct

class CommunityEventViewHolder(
    parent: ViewGroup,
    viewModel: BaseViewModel,
    itemBinding: ItemCommunityEventBinding = createBinding(
        parent,
        R.layout.item_community_event,
        viewModel
    )
) : BaseViewHolder<CommunityEventData>(
    itemBinding
) {
    private val loginManager: LoginManager by lazy { entryPoint.loginManager()}

    init {
        itemBinding.root.setOnClickListener {
            val data = itemBinding.data ?: return@setOnClickListener
            moveToEvent(data)
        }
    }

    private fun moveToEvent(data: CommunityEventData) {
        val act = itemView.getFragmentActivity() ?: return

        if (data.isEventEnd) {
            CommonDialog(itemView.context)
                .setIcon(AppData.POPUP_WARNING)
                .setContents(R.string.event_end_message)
                .setPositiveButton(R.string.str_confirm)
                .show()
        } else {
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
                act.startAct<EventDetailActivity> {
                    putExtra(ExtraCode.EVENT_DETAIL_CODE, data.code)
                }
            }
        }
    }
}