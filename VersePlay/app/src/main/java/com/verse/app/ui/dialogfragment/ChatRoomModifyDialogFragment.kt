package com.verse.app.ui.dialogfragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseBottomSheetDialogFragment
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.databinding.DialogFragmentChatRoomModifyBinding
import com.verse.app.model.chat.ChatMemberRoomModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 채팅 > 방 컨트롤 하는 Dialog Fragment
 * [삭제] -> 빨간색
 * [차단]
 * [신고]
 *
 * Created by juhongmin on 2023/06/22
 */
@AndroidEntryPoint
class ChatRoomModifyDialogFragment
    : BaseBottomSheetDialogFragment<DialogFragmentChatRoomModifyBinding, FragmentViewModel>() {

    override val layoutId: Int = R.layout.dialog_fragment_chat_room_modify
    override val viewModel: FragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    private var listener: Listener? = null
    private var data: ChatMemberRoomModel? = null

    enum class Type {
        DELETE, BLOCKING, REPORT
    }

    interface Listener {
        fun onClick(type: Type, data: ChatMemberRoomModel)
    }

    fun setListener(listener: Listener): ChatRoomModifyDialogFragment {
        this.listener = listener
        return this
    }

    fun setData(data: ChatMemberRoomModel): ChatRoomModifyDialogFragment {
        this.data = data
        return this
    }

    override fun onStart() {
        super.onStart()
        if (dialog is BottomSheetDialog) {
            (dialog as BottomSheetDialog).runCatching {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val data = this.data ?: return

        binding.tvDelete.setOnClickListener {
            this.listener?.onClick(Type.DELETE, data)
            dismiss()
        }

        binding.tvBlocking.setOnClickListener {
            this.listener?.onClick(Type.BLOCKING, data)
            dismiss()
        }

        binding.tvReport.setOnClickListener {
            this.listener?.onClick(Type.REPORT, data)
            dismiss()
        }
    }

    override fun onClick(v: View?) {
    }

    fun simpleShow(fm: FragmentManager) {
        super.show(fm, "ChatRoomModifyDialogFragment")
    }
}
