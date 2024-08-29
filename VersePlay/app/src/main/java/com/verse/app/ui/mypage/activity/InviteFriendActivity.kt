package com.verse.app.ui.mypage.activity

import android.content.ClipboardManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.text.HtmlCompat
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.databinding.ActivityMypageInviteFriendsActivityBinding
import com.verse.app.ui.mypage.viewmodel.MypageInviteFriendsViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.content.ClipData
import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.verse.app.contants.AppData
import com.verse.app.ui.dialog.CommonDialog


@AndroidEntryPoint
class InviteFriendActivity :
    BaseActivity<ActivityMypageInviteFriendsActivityBinding, MypageInviteFriendsViewModel>() {
    override val layoutId = R.layout.activity_mypage_invite_friends_activity
    override val viewModel: MypageInviteFriendsViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        with(viewModel) {
            myNk.value = loginManager.getUserLoginData().memNk

            getInvitableCount()

            // 뒤로가기
            backpress.observe(this@InviteFriendActivity) {
                finish()
            }

            clickClipboard.observe(this@InviteFriendActivity){
                val clip = ClipData.newPlainText("label", myNk.value)
                clipboard.setPrimaryClip(clip)
            }

            showToastStringMsg.observe(this@InviteFriendActivity) {
                showToast(it)
            }

            showToastIntMsg.observe(this@InviteFriendActivity) {
                showToast(it)
            }

            couponStatusSuccess.observe(this@InviteFriendActivity){
                CommonDialog(this@InviteFriendActivity)
                    .setContents(it)
                    .setIcon(AppData.POPUP_COMPLETE)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                backpress.call()
                            }
                        }
                    })
                    .show()
            }

            couponStatusFail.observe(this@InviteFriendActivity){
                CommonDialog(this@InviteFriendActivity)
                    .setContents(it)
                    .setIcon(AppData.POPUP_WARNING)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .show()
            }

            binding.editReccommendNickname.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int
                ) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable) {
                    viewModel.recUserNk.value =
                        binding.editReccommendNickname.text.toString()
                }
            })
        }
    }


    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event?.action === MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

}