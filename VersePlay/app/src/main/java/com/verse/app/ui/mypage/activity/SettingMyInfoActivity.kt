package com.verse.app.ui.mypage.activity

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.databinding.library.baseAdapters.BR
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.contants.ShowImageDetailType
import com.verse.app.databinding.ActivityMypageSettingMyInfoBinding
import com.verse.app.extension.startAct
import com.verse.app.gallery.ui.GalleryBottomSheetDialog
import com.verse.app.gallery.ui.GalleryImageEditBottomSheetDialog
import com.verse.app.permissions.SPermissions
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.dialog.CommonVerticalDialog
import com.verse.app.ui.mypage.viewmodel.SettingMyInfoViewModel
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.UserSettingManager
import com.verse.app.utility.provider.DeviceProvider
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class SettingMyInfoActivity :
    BaseActivity<ActivityMypageSettingMyInfoBinding, SettingMyInfoViewModel>() {
    override val layoutId = R.layout.activity_mypage_setting_my_info
    override val viewModel: SettingMyInfoViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    var tempFile: File? = null
    private var realUri: Uri? = null

    @Inject
    lateinit var accountPref: AccountPref

    var tempImg: String? = null
    var tempImg_bg: String? = null
    var resultImg: File? = null
    var shape = 0
    var edit_type = false
    var crop: Bitmap? = null
    var crop_bg: Bitmap? = null
    var isDefault = false
    var isProfileImage = false
    var isProfileBackImage = false
    var isProfileImageDefault = false
    var isProfileBackImageDefault = false

    private val requestManager: RequestManager by lazy { Glide.with(this@SettingMyInfoActivity) }

    /**
     * 상태메세지 콜백
     */
    var activityResultLauncherEditAboutMe: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                var myEditBio = result.data!!.getStringExtra("mypageSettingDetailBio")
                myEditBio = myEditBio!!.replace("\n".toRegex(), "")
                if (myEditBio.isNullOrEmpty()) {
                    viewModel.myBio.value = getString(R.string.mypage_setting_bio_description)
                } else {
                    viewModel.myBio.value = myEditBio
                }
            }
        }

    /**
     * 이메일 콜백
     */
    var activityResultLauncherEditEmail: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                var myEditEmail = result.data!!.getStringExtra("mypageSettingDetailEmail")
                myEditEmail = myEditEmail!!.replace("\n".toRegex(), "")
                if (myEditEmail.isNullOrEmpty()) {
                    viewModel.myEmail.value = getString(R.string.mypage_setting_email_description)
                } else {
                    viewModel.myEmail.value = myEditEmail
                }
            }
        }

    /**
     * 외부링크 콜백
     */
    var activityResultLauncherEditLink: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                var myEditLinks = result.data!!.getStringExtra("mypageSettingDetailLinks")
                myEditLinks = myEditLinks!!.replace("\n".toRegex(), "")
                if (myEditLinks.isNullOrEmpty()) {
                    viewModel.myLinks.value = getString(R.string.mypage_setting_links_description)
                } else {
                    viewModel.myLinks.value = myEditLinks
                }
            }
        }

    /**
     * 카메라 촬영 - 권한 처리
     */
    var activityResultLauncherCamera: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                DLogger.d("realUri -> $realUri")

                if (viewModel.clickProfileType == 1) {

                    if (realUri != null) {
                        DLogger.d("_profileImg ==> $realUri")
                        GalleryImageEditBottomSheetDialog(ShowImageDetailType.EDIT_FR_PROFILE.code)
                            .setListener(viewModel)
                            .setImageUrl(realUri!!)
                            .simpleShow(supportFragmentManager)
                    }
                } else {

                    if (realUri != null) {
                        DLogger.d("_bgProfileImg ==> $realUri")
                        GalleryImageEditBottomSheetDialog(ShowImageDetailType.EDIT_BG_PROFILE.code)
                            .setListener(viewModel)
                            .setImageUrl(realUri!!)
                            .simpleShow(supportFragmentManager)
                    }
                }

                viewModel.isChangeProfile.call()
            }
        }

    @Inject
    lateinit var deviceProvider: DeviceProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(viewModel) {

            with(binding) {
                setVariable(BR.requestManager, Glide.with(this@SettingMyInfoActivity))

                // 비공개계정
                if (UserSettingManager.isPrivateUser()) {
                    privateYn = true
                    imgChangePrivate.setImageResource(R.drawable.ic_on)
                } else {
                    privateYn = false
                }

                // 내계정 추천
                if (UserSettingManager.getSettingInfo().recUserYn == AppData.Y_VALUE) {
                    recUserYn = true
                    imgChangeRecMyProfile.setImageResource(R.drawable.ic_on)
                } else {
                    recUserYn = false
                }

                _profileImg.value = UserSettingManager.getSettingInfo().pfFrImgPath
                _bgProfileImg.value = UserSettingManager.getSettingInfo().pfBgImgPath

                if (UserSettingManager.getSettingInfo().memEmail?.isNotEmpty() == true) {
                    myEmail.value = UserSettingManager.getSettingInfo().memEmail
                } else {
                    myEmail.value = getString(R.string.mypage_setting_email_description)
                }

                if (UserSettingManager.getSettingInfo().instDesc?.isNotEmpty() == true) {
                    myBio.value = UserSettingManager.getSettingInfo().instDesc
                } else {
                    myBio.value = getString(R.string.mypage_setting_bio_description)
                }

                if (UserSettingManager.getSettingInfo().outLinkUrl?.isNotEmpty() == true) {
                    myLinks.value = UserSettingManager.getSettingInfo().outLinkUrl
                } else {
                    myLinks.value = getString(R.string.mypage_setting_links_description)
                }

            }

            // 뒤로가기
            backpress.observe(this@SettingMyInfoActivity) {
                finish()
            }

            changeMyEmail.observe(this@SettingMyInfoActivity) {
                startAct<SettingMyInfoDetailActivity> {
                    putExtra(
                        "mypageSettingDetailTitle",
                        resources.getString(R.string.mypage_setting_email)
                    )
                    if (UserSettingManager.getSettingInfo().memEmail?.isEmpty() == true) {
                        putExtra("mypageSettingDetailEmail", "")
                    } else {
                        putExtra("mypageSettingDetailEmail", myEmail.value)
                    }
                    activityResultLauncherEditEmail.launch(intent)
                    finish()
                }

            }

            changeMyBio.observe(this@SettingMyInfoActivity) {
                startAct<SettingMyInfoDetailActivity> {
                    putExtra(
                        "mypageSettingDetailTitle",
                        resources.getString(R.string.mypage_setting_bio)
                    )
                    if (UserSettingManager.getSettingInfo().instDesc?.isEmpty() == true) {
                        putExtra("mypageSettingDetailBio", "")
                    } else {
                        putExtra("mypageSettingDetailBio", myBio.value)
                    }
                    activityResultLauncherEditAboutMe.launch(intent)
                    finish()
                }
            }

            changeMyLink.observe(this@SettingMyInfoActivity) {
                startAct<SettingMyInfoDetailActivity> {
                    putExtra(
                        "mypageSettingDetailTitle",
                        resources.getString(R.string.mypage_setting_links)
                    )
                    if (UserSettingManager.getSettingInfo().outLinkUrl?.isEmpty() == true) {
                        putExtra("mypageSettingDetailLinks", "")
                    } else {
                        putExtra("mypageSettingDetailLinks", myLinks.value)
                    }
                    activityResultLauncherEditLink.launch(intent)
                    finish()
                }
            }

            changeProfile.observe(this@SettingMyInfoActivity) {
                CommonVerticalDialog(this@SettingMyInfoActivity)
                    .setBtnOne(getString(R.string.change_profile_take_picture))
                    .setBtnTwo(getString(R.string.change_profile_my_album))
                    .setBtnThree(getString(R.string.change_profile_empty))
                    .setLastText(getString(R.string.str_cancel))
                    .setListener(object : CommonVerticalDialog.Listener {
                        override fun onClick(which: Int) {
                            when (which) {
                                1 -> {
                                    try {
                                        tempFile = File.createTempFile("JPEG_img", ".jpg", cacheDir)
                                    } catch (e: IOException) {
                                        finish()
                                        e.printStackTrace()
                                    }
                                    clickProfileType = 1
                                    callCamera()
                                }

                                2 -> {
                                    clickProfileType = 1
                                    callAlbum()
                                }

                                3 -> {
                                    //기본이미지
                                    clickProfileType = 1
                                    _profileImg.value = ""
                                    changeProfileYn.value = true
                                    isChangeProfile.call()
                                }
                            }
                        }
                    }).show()
            }

            changeBackgroundProfile.observe(this@SettingMyInfoActivity) {
                CommonVerticalDialog(this@SettingMyInfoActivity)
                    .setBtnOne(getString(R.string.change_profile_take_picture))
                    .setBtnTwo(getString(R.string.change_profile_my_album))
                    .setBtnThree(getString(R.string.change_profile_empty))
                    .setLastText(getString(R.string.str_cancel))
                    .setListener(object : CommonVerticalDialog.Listener {
                        override fun onClick(which: Int) {
                            when (which) {
                                1 -> {
                                    try {
                                        tempFile = File.createTempFile("JPEG_img", ".jpg", cacheDir)
                                    } catch (e: IOException) {
                                        finish()
                                        e.printStackTrace()
                                    }
                                    clickProfileType = 2
                                    callCamera()
                                }

                                2 -> {
                                    clickProfileType = 2
                                    callAlbum()
                                }

                                3 -> {
                                    //기본이미지
                                    clickProfileType = 2
                                    _bgProfileImg.value = ""
                                    changeBackgroundProfileYn.value = true
                                    isChangeProfile.call()
                                }
                            }
                        }
                    }).show()
            }

            startOneButtonPopup.observe(this@SettingMyInfoActivity) {
                CommonDialog(this@SettingMyInfoActivity)
                    .setContents(getString(R.string.str_poopup_success))
                    .setCancelable(true)
                    .setPositiveButton(R.string.str_confirm)
                    .setIcon(AppData.POPUP_COMPLETE)
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                binding.saveSettingData.isSelected = false
                                binding.saveSettingData.isEnabled = false
                            }
                        }
                    }).show()
            }

            isChangeProfile.observe(this@SettingMyInfoActivity) {
                binding.saveSettingData.isSelected = true
                binding.saveSettingData.isEnabled = true
            }

            changePrivateAccount.observe(this@SettingMyInfoActivity) {
                if (UserSettingManager.getSettingInfo() != null) {
                    UserSettingManager.setPrivateYn(isPrivateAccount.value.toString())
                }
            }

            recommendMyProfile.observe(this@SettingMyInfoActivity) {
                if (UserSettingManager.getSettingInfo() != null) {
                    UserSettingManager.getSettingInfo().recUserYn =
                        isRecommendMyProfile.value.toString()
                }
            }

            startChangePopup.observe(this@SettingMyInfoActivity) {
                CommonDialog(this@SettingMyInfoActivity)
                    .setContents(it)
                    .setCancelable(false)
                    .setIcon(AppData.POPUP_WARNING)
                    .setPositiveButton(R.string.str_confirm)
                    .setNegativeButton(R.string.str_cancel)
                    .setListener(
                        object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (which == 1) {
                                    viewModel.privateYn = true
                                    viewModel.recUserYn = false
                                    binding.imgChangePrivate.setImageResource(R.drawable.ic_on)
                                    binding.imgChangeRecMyProfile.setImageResource(R.drawable.ic_off)
                                    changeAllAccountState(AppData.Y_VALUE, AppData.N_VALUE)
                                }
                            }
                        })
                    .show()
            }

            startClickChecked.observe(this@SettingMyInfoActivity) {
                when (viewModel.clickCheckedType) {

                    SettingMyInfoViewModel.AccountStatus.CHANGE_ACCOUNT_PRIVACY.name -> {
                        if (viewModel.privateYn) {
                            viewModel.privateYn = false
                            binding.imgChangePrivate.setImageResource(R.drawable.ic_off)
                            changePrvAccYN(AppData.N_VALUE)

                        } else {
                            startChangePopup.value =
                                resourceProvider.getString(R.string.change_myaccount_block)
                        }
                    }

                    SettingMyInfoViewModel.AccountStatus.RECOMMEND_MY_PROFILE.name -> {
                        if (viewModel.recUserYn) {
                            viewModel.recUserYn = false
                            binding.imgChangeRecMyProfile.setImageResource(R.drawable.ic_off)
                            changeRecUserYn(AppData.N_VALUE)
                        } else {
                            if (viewModel.privateYn) {
                                viewModel.privateYn = false
                                binding.imgChangePrivate.setImageResource(R.drawable.ic_off)
                                viewModel.recUserYn = true
                                binding.imgChangeRecMyProfile.setImageResource(R.drawable.ic_on)
                                changeAllAccountState(AppData.N_VALUE, AppData.Y_VALUE)
                            } else {
                                viewModel.recUserYn = true
                                binding.imgChangeRecMyProfile.setImageResource(R.drawable.ic_on)
                                changeRecUserYn(AppData.Y_VALUE)
                            }
                        }
                    }

                    else -> {}
                }
            }

            //Toast
            showToastStringMsg.observe(this@SettingMyInfoActivity) {
                Toast.makeText(this@SettingMyInfoActivity, it, Toast.LENGTH_SHORT).show()
            }

            showToastIntMsg.observe(this@SettingMyInfoActivity) {
                Toast.makeText(this@SettingMyInfoActivity, it, Toast.LENGTH_SHORT).show()
            }

            startEditFrProfile.observe(this@SettingMyInfoActivity) {
                if (it != null) {
                    DLogger.d("_profileFrImg ==> $it")
                    GalleryImageEditBottomSheetDialog(ShowImageDetailType.EDIT_FR_PROFILE.code)
                        .setListener(viewModel)
                        .setImageUrl(it)
                        .simpleShow(supportFragmentManager)
                }
            }

            startEditBgProfile.observe(this@SettingMyInfoActivity) {
                if (it != null) {
                    DLogger.d("_profileBgImg ==> $it")
                    GalleryImageEditBottomSheetDialog(ShowImageDetailType.EDIT_BG_PROFILE.code)
                        .setListener(viewModel)
                        .setImageUrl(it)
                        .simpleShow(supportFragmentManager)
                }
            }
        }
    }

    /**
     * 카메라 촬영 - 권한 처리
     */
    fun callCamera() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
            )
        } else {
            listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        SPermissions(this)
            .requestPermissions(*permissions.toTypedArray())
            .build { b, _ ->
                if (b) {
                    val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                    if (tempFile != null) {
                        realUri = FileProvider.getUriForFile(
                            this@SettingMyInfoActivity,
                            "${this@SettingMyInfoActivity.packageName}.provider",
                            tempFile!!
                        )

                        Log.d("filePathTest", "uri : $realUri")
                        Log.d("filePathTest", "file : $tempFile")
                        i.putExtra(MediaStore.EXTRA_OUTPUT, realUri)
                    }
                    activityResultLauncherCamera.launch(i)

                } else {
                    // 권한 거부 팝업
                    CommonDialog(this@SettingMyInfoActivity)
                        .setIcon(AppData.POPUP_WARNING)
                        .setContents(R.string.str_permission_denied)
                        .setPositiveButton(R.string.str_confirm)
                        .show()
                }
            }
    }

    /**
     * 앨범에서 선택
     */
    fun callAlbum() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
            )
        } else {
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        SPermissions(this)
            .requestPermissions(*permissions.toTypedArray())
            .build { b, _ ->
                if (b) {
                    showGalleryDialog(1)
                } else {
                    // 권한 거부 팝업
                    CommonDialog(this@SettingMyInfoActivity)
                        .setIcon(AppData.POPUP_WARNING)
                        .setContents(R.string.str_permission_denied)
                        .setPositiveButton(R.string.str_confirm)
                        .show()
                }
            }
    }

    fun showGalleryDialog(maxCount: Int) {
        GalleryBottomSheetDialog()
            .setMaxPicker(maxCount)
            .setListener(viewModel)
            .simpleShow(this@SettingMyInfoActivity.supportFragmentManager)
    }
}