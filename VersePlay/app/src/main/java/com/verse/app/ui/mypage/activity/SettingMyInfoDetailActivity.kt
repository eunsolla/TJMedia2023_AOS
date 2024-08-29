package com.verse.app.ui.mypage.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.databinding.ActivityMypageSettingDetailMyInfoBinding
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.mypage.viewmodel.SettingMyInfoDetailViewModel
import com.verse.app.utility.manager.UserSettingManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingMyInfoDetailActivity :
    BaseActivity<ActivityMypageSettingDetailMyInfoBinding, SettingMyInfoDetailViewModel>() {
    override val layoutId = R.layout.activity_mypage_setting_detail_my_info
    override val viewModel: SettingMyInfoDetailViewModel by viewModels()
    override val bindingVariable = BR.viewModel
    var mInputMethodManager: InputMethodManager? = null

    var edit_type_email: Boolean = false
    var edit_type_bio: Boolean = false
    var edit_type_links: Boolean = false

    var email: String = ""
    var bio: String = ""
    var links: String = ""
    var tempBeforeText: String = ""
    var isBeforText: Boolean = false
    var beforeText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        AppData.IS_MYPAGE_EDIT = false

        with(viewModel) {

            binding.apply {

                // 상단 텍스트
                binding.mypageSettingDetailTitle.text =
                    intent.getStringExtra("mypageSettingDetailTitle")

                // layout visible /gone 세팅
                if (binding.mypageSettingDetailTitle.text == resources.getString(R.string.mypage_setting_email)) {
                    edit_type_email = true
                    binding.layoutAboutEmail.visibility = View.VISIBLE
                }

                if (binding.mypageSettingDetailTitle.text == resources.getString(R.string.mypage_setting_bio)) {
                    edit_type_bio = true
                    binding.layoutAboutMe.visibility = View.VISIBLE
                    binding.layoutAboutMeCount.visibility = View.VISIBLE
                }

                if (binding.mypageSettingDetailTitle.text == resources.getString(R.string.mypage_setting_links)) {
                    edit_type_links = true
                    binding.layoutAboutLink.visibility = View.VISIBLE
                }
            }

            // 뒤로가기
            backpress.observe(this@SettingMyInfoDetailActivity) {
                if (AppData.IS_MYPAGE_EDIT) {
                    UserSettingManager.getSettingInfo()?.let {
                        if (edit_type_bio){
                            if (editBioText.value == " "){
                                it.instDesc = ""
                            } else {
                                it.instDesc = editBioText.value
                            }
                        } else if (edit_type_links){
                            if (editLinkText.value == " "){
                                it.outLinkUrl = ""
                            } else {
                                it.outLinkUrl = editLinkText.value
                            }
                        } else {
                            if (editEmailText.value == " "){
                                it.memEmail = ""
                            } else {
                                it.memEmail = editEmailText.value
                            }
                        }
                    }
                }
                finish()
            }

            onComplete.observe(this@SettingMyInfoDetailActivity) {
                // 저장 api 호출
                AppData.IS_MYPAGE_EDIT = true
                hidekeyboard()

                if (edit_type_email) {
                    checkEmail()
                } else if (edit_type_links) {
                    checkLinks()
                } else if (edit_type_bio) {
                    checkBio()
                }
            }

            // 금칙어 관련
            onEmptyViewClick.observe(this@SettingMyInfoDetailActivity) {
                if (edit_type_email) {
                    showKeyboard()
                    binding.viewEditTextEmail.visibility = View.GONE
                }
                if (edit_type_links) {
                    showKeyboard()
                    binding.viewEditTextLink.visibility = View.GONE
                }
            }

            onDelete.observe(this@SettingMyInfoDetailActivity) {
                if (edit_type_email) binding.editTextEditEmail.setText("")
                if (edit_type_links) binding.editTextEditLink.setText("")
                if (edit_type_bio) binding.editTextEditBio.setText("")
            }

            //Toast
            showToastStringMsg.observe(this@SettingMyInfoDetailActivity) {
                Toast.makeText(this@SettingMyInfoDetailActivity, it, Toast.LENGTH_SHORT).show()
            }

            showToastIntMsg.observe(this@SettingMyInfoDetailActivity) {
                Toast.makeText(this@SettingMyInfoDetailActivity, it, Toast.LENGTH_SHORT).show()
            }

            startOneButtonPopup.observe(this@SettingMyInfoDetailActivity) {
                CommonDialog(this@SettingMyInfoDetailActivity)
                    .setContents(getString(R.string.str_poopup_success))
                    .setIcon(AppData.POPUP_COMPLETE)
                    .setPositiveButton(R.string.str_confirm)
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            backpress.call()
                        }
                    })
                    .show()
            }

            startCheckProhibit.observe(this@SettingMyInfoDetailActivity) {
                CommonDialog(this@SettingMyInfoDetailActivity)
                    .setContents(getString(R.string.str_prohibit_notice))
                    .setIcon(AppData.POPUP_WARNING)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                viewModel.reqRefreshMyProfile()
                            }
                        }
                    })
                    .show()
            }

        }

        bindView()
        setDefaultView()
    }

    private fun bindView() {
        email = intent.getStringExtra("mypageSettingDetailEmail").toString()
        if (TextUtils.isEmpty(email) || email == " ") email = ""

        bio = intent.getStringExtra("mypageSettingDetailBio").toString()
        if (TextUtils.isEmpty(bio) || bio == " ") {
            bio = ""
        }

        links = intent.getStringExtra("mypageSettingDetailLinks").toString()
        if (TextUtils.isEmpty(links) || links == " ") links = ""
    }

    private fun setDefaultView() {
        // 값 있는 경우 기존 이메일 및 x버튼 노출
        // 값 없는 경우 hint노출, x버튼 노출안함 -> x버튼 노출하는 것으로 변경함(23/06/19)

        if (edit_type_email) {
            binding.editTextEditEmail.setText(email)
            binding.btnEmailInputDelete.visibility = View.VISIBLE
        }

        if (edit_type_bio) {
            binding.editTextEditBio.setText(bio)
            binding.tvAboutMeLimitWordCurrentCount.text = bio.length.toString()
            binding.btnBioInputDelete.visibility = View.VISIBLE
            binding.editTextEditBio.setSelection(binding.editTextEditBio.length())
        }

        if (edit_type_links) {
            binding.editTextEditLink.setText(links)
            binding.btnLinkInputDelete.visibility = View.VISIBLE
        }

        binding.editTextEditEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editEmail()
            }

            override fun afterTextChanged(s: Editable) {
                if (binding.editTextEditEmail.text.toString().isEmpty()) {
                    viewModel.editEmailText.value = " "
                } else {
                    viewModel.editEmailText.value = binding.editTextEditEmail.text.toString()
                }
            }
        })

        binding.editTextEditBio.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                tempBeforeText = s.toString()
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editBio()
            }

            override fun afterTextChanged(s: Editable) {
                if (binding.editTextEditBio.text.toString().length > 60) {
                    // 60자까지밖에 안 된다는 팝업 노출
                    // 레드마인 #1940 65 -> 60자로 변경
                    if (!isBeforText) {
                        beforeText = tempBeforeText
                        isBeforText = true

                        CommonDialog(this@SettingMyInfoDetailActivity)
                            .setContents(R.string.enter_characters_limit)
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
                } else {
                    binding.tvAboutMeLimitWordCurrentCount.text = "" + binding.editTextEditBio.text.toString().length
                    if (binding.editTextEditBio.text.toString().isEmpty()) {
                        viewModel.editBioText.value = " "
                    } else {
                        viewModel.editBioText.value = binding.editTextEditBio.text.toString()
                    }
                }
            }
        })

        binding.editTextEditLink.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editLinks()
            }

            override fun afterTextChanged(s: Editable) {
                if (binding.editTextEditLink.text.toString().isEmpty()){
                    viewModel.editLinkText.value = " "
                } else {
                    viewModel.editLinkText.value = binding.editTextEditLink.text.toString()
                }

            }
        })
    }

    private fun showKeyboard() {
        if (edit_type_email) mInputMethodManager?.showSoftInput(binding.editTextEditEmail, 0)
        if (edit_type_links) mInputMethodManager?.showSoftInput(binding.editTextEditLink, 0)
    }

    private fun hidekeyboard() {
        if (edit_type_email) mInputMethodManager?.hideSoftInputFromWindow(
            binding.editTextEditEmail.windowToken,
            0
        )
        if (edit_type_links) mInputMethodManager?.hideSoftInputFromWindow(
            binding.editTextEditLink.windowToken,
            0
        )
    }

    // 이메일 유효성 검사
    private fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    // 링크 유효성 검사
    private fun isValidLink(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.WEB_URL.matcher(target).matches()
    }


    // 이메일 관련 text watcher
    fun editEmail() {
        // text null 아닐경우
        if (binding.editTextEditEmail.text.isNotEmpty()) {

            // 유효성 검사 성공했을 경우
            if (isValidEmail(binding.editTextEditEmail.text)) {

                // 기존 이메일과 같을 경우 변경점이 없다고 판단하고 저장버튼 비활성화
                if (email == binding.editTextEditEmail.text.toString()) {
                    binding.saveSettingData.isSelected = false
                    binding.saveSettingData.isEnabled = false
                    binding.layoutEditEmailBorder.background = ContextCompat.getDrawable(
                        this@SettingMyInfoDetailActivity,
                        R.drawable.edit_border
                    )
                    binding.tvInvalidEmail.visibility = View.GONE

                    // 기존 이메일과 다를 경우 저장버튼 활성화
                } else {
                    binding.saveSettingData.isSelected = true
                    binding.saveSettingData.isEnabled = true
                    binding.layoutEditEmailBorder.background = ContextCompat.getDrawable(
                        this@SettingMyInfoDetailActivity,
                        R.drawable.edit_complete_border
                    )
                }
                binding.tvInvalidEmail.visibility = View.GONE

                // 유효성 검사 실패했을 경우 저장버튼 비활성화, 올바른 이메일을 입력해주세요 텍스트 visible
            } else {
                binding.saveSettingData.isSelected = false
                binding.saveSettingData.isEnabled = false
                binding.layoutEditEmailBorder.background = ContextCompat.getDrawable(
                    this@SettingMyInfoDetailActivity,
                    R.drawable.edit_error_border
                )
                binding.tvInvalidEmail.visibility = View.VISIBLE
            }
            // text 있으면 전체삭제 버튼 항상 visible
            binding.btnEmailInputDelete.visibility = View.VISIBLE

            // text null
        } else if (binding.editTextEditEmail.text.isEmpty()) {

            // 기존 이메일과 다른 경우 이메일을 썼다 지운걸로 간주하고 저장버튼 활성화
            if (email != binding.editTextEditEmail.text.toString()) {
                binding.saveSettingData.isSelected = true
                binding.saveSettingData.isEnabled = true
                binding.layoutEditEmailBorder.background = ContextCompat.getDrawable(
                    this@SettingMyInfoDetailActivity,
                    R.drawable.edit_complete_border
                )
                binding.tvInvalidEmail.visibility = View.GONE

                // 기존 이메일과 같을 경우 변경점이 없다고 판단하고 저장버튼 비활성화
            } else {
                binding.saveSettingData.isSelected = false
                binding.saveSettingData.isEnabled = false
                binding.layoutEditEmailBorder.background = ContextCompat.getDrawable(
                    this@SettingMyInfoDetailActivity,
                    R.drawable.edit_border
                )
                binding.tvInvalidEmail.visibility = View.GONE
            }

            // text가 null이 아니고 기존 이메일과 같을 경우 변경점이 없다고 판단하여 저장버튼 비활성화
        } else if (binding.editTextEditEmail.text.isNotEmpty() && binding.editTextEditEmail.text.toString() == email) {
            binding.saveSettingData.isSelected = false
            binding.saveSettingData.isEnabled = false
            binding.layoutAboutEmail.background = ContextCompat.getDrawable(
                this@SettingMyInfoDetailActivity,
                R.drawable.edit_border
            )
            binding.tvInvalidEmail.visibility = View.GONE
        }
    }

    // 상메 관련 text watcher
    private fun editBio() {
        // text null 아님
        if (binding.editTextEditBio.text.isNotEmpty()) {

            // 기존 상태메세지와 같을 경우 변경점이 없다고 판단하여 저장버튼 비활성화
            if (bio == binding.editTextEditBio.text.toString()) {
                binding.saveSettingData.isSelected = false
                binding.saveSettingData.isEnabled = false
                binding.layoutEditBioBorder.background = ContextCompat.getDrawable(
                    this@SettingMyInfoDetailActivity,
                    R.drawable.edit_border
                )

                // 기존 상태메세지와 다른 경우 저장버튼 활성화
            } else {
                binding.saveSettingData.isSelected = true
                binding.saveSettingData.isEnabled = true

                binding.layoutEditBioBorder.background = ContextCompat.getDrawable(
                    this@SettingMyInfoDetailActivity,
                    R.drawable.edit_complete_border
                )
            }

            // text null
        } else {
            // 기존에 bio가 있었다가 지운 경우는 저장버튼 활성화
            if (bio != binding.editTextEditBio.text.toString()) {
                binding.saveSettingData.isSelected = true
                binding.saveSettingData.isEnabled = true

                binding.layoutEditBioBorder.background = ContextCompat.getDrawable(
                    this@SettingMyInfoDetailActivity,
                    R.drawable.edit_complete_border
                )
            } else {
                binding.saveSettingData.isSelected = false
                binding.saveSettingData.isEnabled = false
                binding.layoutEditBioBorder.background = ContextCompat.getDrawable(
                    this@SettingMyInfoDetailActivity,
                    R.drawable.edit_border
                )
            }
        }
    }

    // 링크 관련 text watcher
    private fun editLinks() {
        // text null 아님
        if (binding.editTextEditLink.text.isNotEmpty()) {

            // 기존 링크랑 같은 경우 변경점이 없다고 판단하여 저장버튼 비활성화
            if (links == binding.editTextEditLink.text.toString()) {
                binding.saveSettingData.isSelected = false
                binding.saveSettingData.isEnabled = false
                binding.layoutEditLinkBorder.background = ContextCompat.getDrawable(
                    this@SettingMyInfoDetailActivity,
                    R.drawable.edit_border
                )

            } else if (isValidLink(binding.editTextEditLink.text)) {
                // 기존 링크랑 다른 경우 저장버튼 활성화
                binding.saveSettingData.isSelected = true
                binding.saveSettingData.isEnabled = true
                binding.layoutEditLinkBorder.background = ContextCompat.getDrawable(
                    this@SettingMyInfoDetailActivity,
                    R.drawable.edit_complete_border
                )

            } else {
                binding.saveSettingData.isSelected = false
                binding.saveSettingData.isEnabled = false
                binding.layoutEditLinkBorder.background = ContextCompat.getDrawable(
                    this@SettingMyInfoDetailActivity,
                    R.drawable.edit_border
                )
            }

            // text null
        } else {
            //기존 링크가 null이 아닌 경우 썼다가 지운걸로 판단하여 저장버튼 활성화
            if (links.isNotEmpty()) {
                binding.saveSettingData.isSelected = true
                binding.saveSettingData.isEnabled = true
                binding.layoutEditLinkBorder.background = ContextCompat.getDrawable(
                    this@SettingMyInfoDetailActivity,
                    R.drawable.edit_complete_border
                )

                // 기존 링크도 null, 새로 입력한 값도 null인경우 저장버튼 비활성화
            } else {
                binding.saveSettingData.isSelected = false
                binding.saveSettingData.isEnabled = false
                binding.layoutEditLinkBorder.background = ContextCompat.getDrawable(
                    this@SettingMyInfoDetailActivity,
                    R.drawable.edit_border
                )
            }
        }
    }


}