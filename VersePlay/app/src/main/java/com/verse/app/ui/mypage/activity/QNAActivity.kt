package com.verse.app.ui.mypage.activity

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.contants.ShowImageDetailType
import com.verse.app.databinding.ActivityMypageSettingQnaBinding
import com.verse.app.extension.getActivity
import com.verse.app.gallery.ui.GalleryBottomSheetDialog
import com.verse.app.gallery.ui.GalleryImageEditBottomSheetDialog
import com.verse.app.permissions.SPermissions
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.dialog.SelectDialog
import com.verse.app.ui.mypage.viewmodel.QNAViewModel
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.UserSettingManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QNAActivity :
    BaseActivity<ActivityMypageSettingQnaBinding, QNAViewModel>() {
    override val layoutId = R.layout.activity_mypage_setting_qna
    override val viewModel: QNAViewModel by viewModels()
    override val bindingVariable = BR.viewModel
    var mInputMethodManager: InputMethodManager? = null
    var cate_seq: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.deviceProvider.setDeviceStatusBarColor(getActivity()!!.window, R.color.white)

        mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        with(viewModel) {
            setCategoryDataFromServer()

            binding.apply {
                btnSend.isEnabled = false
                btnSend.isClickable = false
            }

            myEmail.value = UserSettingManager.getSettingInfo()?.memEmail.toString()

            if (myEmail.value.isNotEmpty()) {
                binding.editTextSendFeedbackEmail.setText(myEmail.value)
            }

            // 뒤로가기
            backpress.observe(this@QNAActivity) {
                finish()
            }

            callGalleryDialog.observe(this@QNAActivity) {
                selectAlbum()
            }

            reqCompletedInquiryInfo.observe(this@QNAActivity) {
                if (viewModel.fgProhibitYn) {
                    CommonDialog(this@QNAActivity)
                        .setContents(getString(R.string.str_prohibit_notice))
                        .setIcon(AppData.POPUP_WARNING)
                        .setPositiveButton(getString(R.string.str_confirm))
                        .setListener(object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (which == CommonDialog.POSITIVE) {
                                    finish()
                                }
                            }
                        })
                        .show()
                } else {
                    CommonDialog(this@QNAActivity)
                        .setContents(getString(R.string.str_poopup_success))
                        .setIcon(AppData.POPUP_COMPLETE)
                        .setPositiveButton(getString(R.string.str_confirm))
                        .setListener(object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (which == CommonDialog.POSITIVE) {
                                    finish()
                                }
                            }
                        })
                        .show()
                }
            }

            delete.observe(this@QNAActivity) {
                attUri.value = ""
                binding.layoutAfterAttachImage.visibility = View.GONE
                binding.btnAttachImage.visibility = View.VISIBLE
            }

            startEditAttachImage.observe(this@QNAActivity) {
                if (it != null) {
                    DLogger.d("Attach Image ==> $it")
                    GalleryImageEditBottomSheetDialog(ShowImageDetailType.EDIT_ATTACH_IMAGE.code)
                        .setListener(viewModel)
                        .setImageUrl(it)
                        .simpleShow(supportFragmentManager)
                }
            }

            attUri.observe(this@QNAActivity) {
                binding.tvAttachedImageName.text = it
                binding.layoutAfterAttachImage.visibility = View.VISIBLE
                binding.btnAttachImage.visibility = View.GONE
            }

            showSelectBox.observe(this@QNAActivity) {
                SelectDialog(this@QNAActivity)
                    .setTitle(resources.getString(R.string.mypage_send_feedback_contact_type_text_string))
                    .setList(selectBoxDataArrayList, mSelectCallback)
                    .setListener(object : SelectDialog.Listener {
                        override fun onClick(which: Int) {}
                    })
                    .setCallBack(object : SelectDialog.SelectCallback {
                        override fun selectedCallback(
                            selectBoxData: String,
                            selectBoxCode: String
                        ) {
                        }
                    })
                    .setCancelable(true)
                    .show()
            }

            binding.editTextSendFeedbackComment.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable) {
                    viewModel.editQNAText.value =
                        binding.editTextSendFeedbackComment.text.toString()

                    // editText_send_feedback_comment, editText_send_feedback_email 내용 유무 확인
                    if (checkAllWritten()) {
                        binding.btnSend.isSelected = true
                        binding.btnSend.isEnabled = true
                    } else {
                        binding.btnSend.isSelected = false
                        binding.btnSend.isEnabled = false
                    }
                }
            })

            binding.editTextSendFeedbackEmail.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                    editEmail()
                }

                override fun afterTextChanged(s: Editable) {
                    viewModel.myEmail.value =
                        binding.editTextSendFeedbackEmail.text.toString()
                    // editText_send_feedback_comment, editText_send_feedback_email 내용 유무 확인
                    if (checkAllWritten()) {
                        binding.btnSend.isSelected = true
                        binding.btnSend.isEnabled = true


                    } else {
                        binding.btnSend.isSelected = false
                        binding.btnSend.isEnabled = false
                    }
                }
            })

        }
    }

    fun selectBoxCallback(selectBoxData: String) {
        binding.tvSendFeedbackCommentsPlace.text = selectBoxData
        viewModel.qnaTitle.value = selectBoxData

        // editText_send_feedback_comment, editText_send_feedback_email 내용 유무 확인
        if (checkAllWritten()) {
            binding.btnSend.isSelected = true
            binding.btnSend.isEnabled = true
        } else {
            binding.btnSend.isSelected = false
            binding.btnSend.isEnabled = false
        }
    }

    private fun checkAllWritten(): Boolean {
        return cate_seq != null && viewModel.editQNAText.value.isNotEmpty() && isValidEmail(binding.editTextSendFeedbackEmail.text)
    }

    private var mSelectCallback: SelectDialog.SelectCallback = object :
        SelectDialog.SelectCallback {
        override fun selectedCallback(selectBoxData: String, selectBoxCode: String) {
            DLogger.d("콜백들어옴 : $selectBoxData")
            selectBoxCallback(selectBoxData)
            cate_seq = selectBoxData
            viewModel.qnaCode.value = selectBoxCode
            if (checkAllWritten()) {
                binding.btnSend.isSelected = true
                binding.btnSend.isEnabled = true
            } else {
                binding.btnSend.isSelected = false
                binding.btnSend.isEnabled = false
            }
        }
    }

    /**
     * 앨범에서 선택
     */
    fun selectAlbum() {
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
                        .simpleShow(this@QNAActivity.supportFragmentManager)
                } else {
                    // 권한 거부 팝업
                    CommonDialog(this@QNAActivity)
                        .setIcon(AppData.POPUP_WARNING)
                        .setContents(R.string.str_permission_denied)
                        .setPositiveButton(R.string.str_confirm)
                        .show()
                }
            }
    }

    // 이메일 관련 text watcher
    fun editEmail() {
        // text null 아닐경우
        if (binding.editTextSendFeedbackEmail.text.isNotEmpty()) {

            // 유효성 검사 실패했을 경우 저장버튼 비활성화, 올바른 이메일을 입력해주세요 텍스트 visible
            if (!isValidEmail(binding.editTextSendFeedbackEmail.text)) {
                binding.btnSend.isSelected = false
                binding.btnSend.isEnabled = false
            }

            // text null
        } else if (binding.editTextSendFeedbackEmail.text.isEmpty()) {
            binding.btnSend.isSelected = false
            binding.btnSend.isEnabled = false
        }
    }

    // 이메일 유효성 검사
    private fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

}