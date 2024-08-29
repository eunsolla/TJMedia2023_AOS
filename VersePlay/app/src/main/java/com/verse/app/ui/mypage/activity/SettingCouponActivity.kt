package com.verse.app.ui.mypage.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.databinding.ActivityMypageSettingCouponBinding
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.mypage.viewmodel.SettingCouponViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern

@AndroidEntryPoint
class SettingCouponActivity :
    BaseActivity<ActivityMypageSettingCouponBinding, SettingCouponViewModel>() {
    override val layoutId = R.layout.activity_mypage_setting_coupon
    override val viewModel: SettingCouponViewModel by viewModels()
    override val bindingVariable = BR.viewModel
    var mInputMethodManager: InputMethodManager? = null
    var popupFlag = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        viewModel.deviceProvider.setDeviceStatusBarColor(window, R.color.white)

        AppData.IS_MYPAGE_EDIT = false

        with(viewModel) {

            binding.apply {

                editTextCoupon.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(
                        s: CharSequence, start: Int, before: Int, count: Int) {

                        // text null 아님
                        if (binding.editTextCoupon.text.isNotEmpty()) {

                            binding.saveSettingData.isSelected = false
                            binding.saveSettingData.isEnabled = false
                            binding.layoutEditCouponBorder.background = ContextCompat.getDrawable(
                                this@SettingCouponActivity,
                                R.drawable.edit_error_border
                            )
                            binding.tvInvalidCoupon.visibility = View.VISIBLE
                            // text 있으면 전체삭제 버튼 항상 visible
                            binding.btnCouponInputDelete.visibility = View.VISIBLE

                            // text null
                        } else if (binding.editTextCoupon.text.isEmpty()) {
                            AppData.IS_MYPAGE_EDIT = false
                            binding.saveSettingData.isSelected = false
                            binding.saveSettingData.isEnabled = false
                            binding.btnCouponInputDelete.visibility = View.GONE
                            binding.layoutEditCouponBorder.background = ContextCompat.getDrawable(
                                this@SettingCouponActivity,
                                R.drawable.edit_border
                            )
                            binding.tvInvalidCoupon.visibility = View.GONE
                        }
                    }

                    override fun afterTextChanged(s: Editable) {
                        editCouponText.value = binding.editTextCoupon.text.toString()

                        // 16자 && 유효성검사 성공 시 저장 버튼 활성화
                        if (binding.editTextCoupon.text.toString().length == 16 && isValidCoupon(binding.editTextCoupon.text)) {
                            binding.saveSettingData.isSelected = true
                            binding.saveSettingData.isEnabled = true
                            binding.layoutEditCouponBorder.background = ContextCompat.getDrawable(
                                this@SettingCouponActivity,
                                R.drawable.edit_complete_border
                            )
                            binding.tvInvalidCoupon.visibility = View.GONE
                        }
                    }
                })

            }

            // 뒤로가기
            backpress.observe(this@SettingCouponActivity) {
                finish()
            }

            startOneButtonPopup.observe(this@SettingCouponActivity) {
                CommonDialog(this@SettingCouponActivity)
                    .setContents(getString(R.string.str_help_setting_popup))
                    .setIcon(AppData.POPUP_HELP)
                    .setPositiveButton(R.string.str_confirm)
                    .show()
            }

            onDelete.observe(this@SettingCouponActivity) {
                binding.editTextCoupon.setText("")
                hidekeyboard()
            }

            couponStatusSuccess.observe(this@SettingCouponActivity) {
                // 성공팝업
                CommonDialog(this@SettingCouponActivity)
                    .setContents(getString(it))
                    .setIcon(AppData.POPUP_COMPLETE)
                    .setPositiveButton(R.string.str_confirm)
                    .setListener(
                        object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (which == 1) {
                                    backpress.call()
                                }
                            }
                        })
                    .show()
            }

            couponStatusFail.observe(this@SettingCouponActivity) {
                // 실패팝업
                CommonDialog(this@SettingCouponActivity)
                    .setContents(it)
                    .setIcon(AppData.POPUP_WARNING)
                    .setPositiveButton(R.string.str_confirm)
                    .show()
            }
            
        }
    }

    private fun hidekeyboard() {
        mInputMethodManager?.hideSoftInputFromWindow(
            binding.editTextCoupon.windowToken,
            0
        )

    }

    // 쿠폰 유효성 검사하는 로직
    private fun isValidCoupon(target: CharSequence): Boolean {
        var isResult = false
        if (!TextUtils.isEmpty(target)) {
            val patternVerify = "^[A-Z0-9]*$"
            isResult = Pattern.matches(patternVerify, target)
        }
        return isResult
    }

}