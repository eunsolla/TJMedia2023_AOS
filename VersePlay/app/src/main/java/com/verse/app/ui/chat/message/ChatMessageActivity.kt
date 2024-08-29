package com.verse.app.ui.chat.message

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.AChatMessageBinding
import com.verse.app.extension.hideKeyboard
import com.verse.app.extension.initActivityResult
import com.verse.app.gallery.ui.GalleryBottomSheetDialog
import com.verse.app.model.chat.ChatMessageIntentModel
import com.verse.app.model.mypage.MyPageIntentModel
import com.verse.app.permissions.SPermissions
import com.verse.app.repository.tcp.NettyClient
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.mypage.activity.MyPageRootActivity
import com.verse.app.utility.DLogger
import com.verse.app.widget.scroller.CustomLinearScroller
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Description : 채팅 > [메시지_보내기]
 *
 * Created by juhongmin on 2023/06/14
 */
@AndroidEntryPoint
class ChatMessageActivity : BaseActivity<AChatMessageBinding, ChatMessageActivityViewModel>() {
    override val layoutId: Int = R.layout.a_chat_message
    override val viewModel: ChatMessageActivityViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    @Inject
    lateinit var nettyClient: NettyClient

    private val smoothScroller: CustomLinearScroller by lazy {
        CustomLinearScroller(this).apply {
            interceptor = FastOutSlowInInterpolator()
        }
    }

    private val profilePageResult = initActivityResult {
        DLogger.d("다시 돌아왔습니다!@!@!@ ")
        viewModel.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(viewModel) {
            startFinishEvent.observe(this@ChatMessageActivity) {
                finish()
            }

            startGalleryDialogEvent.observe(this@ChatMessageActivity) {
                handleGalleryDialog()
            }

            startFocusingPositionEvent.observe(this@ChatMessageActivity) {
                handleFocusingPosition(it)
            }

            startPayloadEvent.observe(this@ChatMessageActivity) {
                handlePayloadPosition(it)
            }

            startInvalidPageEvent.observe(this@ChatMessageActivity) {
                showInvalidDialog()
            }

            startMoveToProfileEvent.observe(this@ChatMessageActivity) {
                handleMoveToProfile(it)
            }

            start()
        }
    }

    private fun handleGalleryDialog() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        SPermissions(this)
            .requestPermissions(*permissions.toTypedArray())
            .build { b, _ ->
                if (b) {
                    GalleryBottomSheetDialog()
                        .setMaxPicker(1)
                        .setListener(viewModel)
                        .simpleShow(supportFragmentManager)
                } else {
                    // 권한 거부 팝업
                    CommonDialog(this)
                        .setContents(resources.getString(R.string.str_permission_denied))
                        .setPositiveButton(R.string.str_confirm)
                        .show()
                }
            }
    }

    override fun finish() {
        setResult(Activity.RESULT_OK)
        super.finish()
        nettyClient.closeClient()
        nettyClient.clearListener()
    }

    private fun handleFocusingPosition(position: Int) {
        val rv = binding.rvContents
        val itemCount = rv.adapter?.itemCount ?: 0
        if (itemCount > position) {
            val lm = rv.layoutManager as LinearLayoutManager
            smoothScroller.targetPosition = position
            lm.startSmoothScroll(smoothScroller)
        }
    }

    private fun handlePayloadPosition(position: Int) {
        val rv = binding.rvContents
        rv.adapter?.notifyItemChanged(position, true)
    }

    private fun showInvalidDialog() {
        CommonDialog(this)
            .setContents(R.string.chat_server_error_msg)
            .setPositiveButton(R.string.chat_server_retry)
            .setNegativeButton(R.string.str_cancel)
            .setListener(object : CommonDialog.Listener {
                override fun onClick(which: Int) {
                    if (which == CommonDialog.POSITIVE) {
                        viewModel.start()
                    } else {
                        finish()
                    }
                }
            })
            .show()
    }

    private fun handleMoveToProfile(model: ChatMessageIntentModel) {
        nettyClient.closeClient()
        nettyClient.clearListener()

        hideKeyboard()
        val intent = Intent(this, MyPageRootActivity::class.java)
        intent.putExtra(ExtraCode.MY_PAGE_DATA, MyPageIntentModel(model.targetMemberCode))
        profilePageResult.launch(
            intent,
            ActivityOptionsCompat.makeCustomAnimation(
                this,
                R.anim.in_right_to_left,
                R.anim.out_right_to_left
            )
        )
    }
}
