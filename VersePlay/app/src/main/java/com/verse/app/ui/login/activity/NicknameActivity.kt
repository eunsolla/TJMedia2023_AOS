package com.verse.app.ui.login.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.MainEntryType
import com.verse.app.databinding.ActivityNicknameBinding
import com.verse.app.extension.getActivity
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.login.viewmodel.NicknameViewModel
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.moveToUserStAct
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NicknameActivity : BaseActivity<ActivityNicknameBinding, NicknameViewModel>() {
    override val layoutId = R.layout.activity_nickname
    override val viewModel: NicknameViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    var tempNickName: String = ""
    var tempEmail: String = ""
    var tempBeforeText: String = ""
    var isBeforText: Boolean = false
    var beforeText: String = ""

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.deviceProvider.setDeviceStatusBarColor(getActivity()!!.window, R.color.white)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        getIntentData()

        with(viewModel) {
            // 닫기
            startFinish.observe(this@NicknameActivity) {
                CommonDialog(this@NicknameActivity)
                    .setContents(resources.getString(R.string.sign_location_back_message))
                    .setNegativeButton(resources.getString(R.string.str_cancel))
                    .setPositiveButton(resources.getString(R.string.str_confirm))
                    .setIcon(AppData.POPUP_WARNING)
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                viewModel.loginManager.initLoginPreference()
                                moveMain()
                            }
                        }
                    })
                    .show()
            }

            showOneButtonDialogPopup.observe(this@NicknameActivity) {
                showOneButtonDialogPopup.value?.let {
                    var text = showOneButtonDialogPopup.value

                    CommonDialog(this@NicknameActivity)
                        .setContents(text!!)
                        .setIcon(AppData.POPUP_WARNING)
                        .setPositiveButton(getString(R.string.str_confirm))
                        .show()
                }
            }

            startHelp.observe(this@NicknameActivity) {
                CommonDialog(this@NicknameActivity)
                    .setIcon(AppData.POPUP_HELP)
                    .setContents(R.string.sign_location_back_help)
                    .setPositiveButton(R.string.str_confirm)
                    .show()
            }

            autoCreateNickName.observe(this@NicknameActivity) {
                binding.editTextEditNickname.setText(viewModel.autoCreateNickName.value)
                checkRegNickName = false

                binding.tvConfirmBgColor.setBackgroundResource(R.color.color_eaeaea)
                binding.tvConfirmText.setTextColor(getColor(R.color.color_8c8c8c))
                setDoubleCheckUi(true)
            }

            startVerifyCheckNickName.observe(this@NicknameActivity) {
                if (!checkRegNickName) {
                    if (binding.editTextEditNickname.text.trim().isNotEmpty()) {
                        verifyCheckNickName(binding.editTextEditNickname.text.trim().toString())
                        binding.llCheckNickName.setBackgroundResource(R.drawable.bg_square_round_butten_gray)
                        binding.tvCheckNickName.setTextColor(getColor(R.color.color_8c8c8c))
                    }
                }
            }

            startVerifyCheckNickNameSuccess.observe(this@NicknameActivity) {
                if (viewModel.fgProhibitYn) {
                    CommonDialog(this@NicknameActivity)
                        .setContents(getString(R.string.str_prohibit_notice))
                        .setIcon(AppData.POPUP_WARNING)
                        .setPositiveButton(getString(R.string.str_confirm))
                        .setListener(object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (which == CommonDialog.POSITIVE) {
                                }
                            }
                        })
                        .show()
                }

                binding.tvCheckNickNameMessage.setTextColor(getColor(R.color.color_2fc2ff))
                binding.tvCheckNickNameMessage.text = it
                checkRegNickName = true

                binding.tvConfirmBgColor.setBackgroundResource(R.color.color_2fc2ff)
                binding.tvConfirmText.setTextColor(getColor(R.color.white))
                binding.tvConfirm.isEnabled = true
                binding.tvConfirm.isSelected = true
            }

            startVerifyCheckNickNameFail.observe(this@NicknameActivity) {
                binding.tvConfirmBgColor.setBackgroundResource(R.color.color_eaeaea)
                binding.tvConfirmText.setTextColor(getColor(R.color.color_8c8c8c))
                binding.tvCheckNickNameMessage.setTextColor(getColor(R.color.color_ff3d33))
                binding.tvCheckNickNameMessage.text = it
                checkRegNickName = false

                updateNickNameEditBorder(true)
            }

            startJoinUser.observe(this@NicknameActivity) {
                if (checkRegNickName) {
                    registerMember(viewModel.nickName, tempEmail)
                }
            }

            //유저 상태에 따라 화면 이동 처리
            memStCd.observe(this@NicknameActivity) {
                if (it.isNotEmpty()) {
                    moveToUserStAct(
                        this@NicknameActivity,
                        viewModel.accountPref,
                        viewModel.loginManager,
                        false,
                        it,
                        "",
                        ""
                    )
                }
            }

            binding.editTextEditNickname.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    binding.tvCheckNickNameMessage.text = ""
                    tempBeforeText = s.toString()
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    checkRegNickName = false

                    updateNickNameEditBorder(false)

                    if (!checkRegNickName) {
                        binding.tvConfirmBgColor.setBackgroundResource(R.color.color_eaeaea)
                        binding.tvConfirmText.setTextColor(getColor(R.color.color_8c8c8c))
                    }

                    setDoubleCheckUi(s.trim().isNotEmpty())
                }

                override fun afterTextChanged(s: Editable) {
                    if (s.toString().isNotEmpty()) {

                        // 공백 있을 경우 공백 입력은 포함되지 않습니다 알럿 노출
                        if (binding.editTextEditNickname.text.contains(" ")) {
                            setDoubleCheckUi(false)
                            binding.tvCheckNickNameMessage.text =
                                getString(R.string.str_nickname_validation)
                        }
                    }

                    // 50자 이상 불가능 팝업 노출
                    if (binding.editTextEditNickname.text.length > 50) {
                        if (!isBeforText) {
                            beforeText = tempBeforeText
                            isBeforText = true

                            CommonDialog(this@NicknameActivity)
                                .setContents(R.string.nickname_enter_characters_limit)
                                .setIcon(AppData.POPUP_WARNING)
                                .setPositiveButton(R.string.str_confirm)
                                .setListener(object : CommonDialog.Listener {
                                    override fun onClick(which: Int) {
                                        if (which == CommonDialog.POSITIVE) {
                                            s.delete(beforeText.length, s.length)
                                            isBeforText = false
                                        }
                                    }
                                })
                                .show()
                        }
                        return
                    }
                }
            })
        }
    }

    private fun setDoubleCheckUi(isEnabled: Boolean) {
        if (isEnabled) {
            binding.llCheckNickName.setBackgroundResource(R.drawable.bg_square_round_butten_bule)
            binding.llCheckNickName.isEnabled = true
            binding.tvCheckNickName.setTextColor(getColor(R.color.white))
        } else {
            binding.llCheckNickName.setBackgroundResource(R.drawable.bg_square_round_butten_gray)
            binding.llCheckNickName.isEnabled = false
            binding.tvCheckNickName.setTextColor(getColor(R.color.color_8c8c8c))
        }
    }

    fun getIntentData() {
        this.tempNickName = intent.getStringExtra(ExtraCode.TEMP_NICK_NAME).toString()
        this.tempEmail = intent.getStringExtra(ExtraCode.TEMP_EMAIL).toString()
        DLogger.d("tempNickName : ${tempNickName}")
        DLogger.d("tempEmail : ${tempEmail}")

        viewModel.autoCreateNickName.value = this.tempNickName
    }

    fun updateNickNameEditBorder(isError: Boolean) {
        if (isError) {
            binding.editTextEditNickname.setBackgroundResource(R.drawable.bg_square_border_red)
        } else {
            binding.editTextEditNickname.setBackgroundResource(R.drawable.bg_square_border_bule)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    fun moveMain() {
        super.finish()
        RxBus.publish(RxBusEvent.MainEnterEvent(MainEntryType.VERSION, true))
    }


    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            viewModel.startFinish.call()
        }
    }

    // 링크 유효성 검사
    private fun isValidNickName(target: CharSequence): Boolean {
        return target.toString().replace(" ", "") == "" || target.toString() == ""
    }

}