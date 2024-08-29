package com.verse.app.ui.chat.rooms

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.AChatRoomsBinding
import com.verse.app.extension.initActivityResult
import com.verse.app.model.chat.ChatMemberRoomModel
import com.verse.app.model.chat.ChatMessageIntentModel
import com.verse.app.ui.chat.message.ChatMessageActivity
import com.verse.app.ui.dialogfragment.ChatRoomModifyDialogFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 채팅 > [메시지함]
 *
 * Created by juhongmin on 2023/06/14
 */
@AndroidEntryPoint
class ChatRoomsActivity :
    BaseActivity<AChatRoomsBinding, ChatRoomsActivityViewModel>() {
    override val layoutId: Int = R.layout.a_chat_rooms
    override val viewModel: ChatRoomsActivityViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    private val chatMessageResult = initActivityResult {
        if (it.resultCode == Activity.RESULT_OK) {
            viewModel.start()
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(viewModel) {
            onBackPressedDispatcher.addCallback(this@ChatRoomsActivity, onBackPressedCallback)
            viewModel.deviceProvider.setDeviceStatusBarColor(window, R.color.white)

            startBackEvent.observe(this@ChatRoomsActivity) {
                finish()
            }

            startDialogEvent.observe(this@ChatRoomsActivity) {
                handleRoomModifyDialog(it)
            }

            startMoveToMessageEvent.observe(this@ChatRoomsActivity) {
                handleMoveToChatMessage(it)
            }
            start()
        }
    }

    private fun handleRoomModifyDialog(data: ChatMemberRoomModel) {
        ChatRoomModifyDialogFragment()
            .setData(data)
            .setListener(viewModel)
            .simpleShow(supportFragmentManager)
    }

    private fun handleMoveToChatMessage(model: ChatMemberRoomModel) {
        val intent = Intent(this, ChatMessageActivity::class.java)
        intent.putExtra(ExtraCode.CHAT_MESSAGE_ROOM_DATA, ChatMessageIntentModel(model))
        chatMessageResult.launch(
            intent,
            ActivityOptionsCompat.makeCustomAnimation(
                this,
                R.anim.in_right_to_left,
                R.anim.out_right_to_left
            )
        )
    }
}
