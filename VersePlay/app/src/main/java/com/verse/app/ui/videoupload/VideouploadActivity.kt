package com.verse.app.ui.videoupload

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.ActivityVideouploadBinding
import com.verse.app.extension.hideKeyboard
import com.verse.app.extension.initFragment
import com.verse.app.extension.isServiceRunning
import com.verse.app.extension.replaceFragment
import com.verse.app.extension.startAct
import com.verse.app.extension.startSongEncodeService
import com.verse.app.gallery.ui.GalleryBottomSheetDialog
import com.verse.app.permissions.SPermissions
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.main.MainActivity
import com.verse.app.ui.videoupload.fragment.VideouploadFragment
import com.verse.app.ui.videoupload.viewmodel.VideouploadViewModel
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.SongEncodeService
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable

@AndroidEntryPoint
class VideouploadActivity : BaseActivity<ActivityVideouploadBinding, VideouploadViewModel>() {
    override val layoutId = R.layout.activity_videoupload
    override val viewModel: VideouploadViewModel by viewModels()
    override val bindingVariable = BR.viewModel
    private lateinit var encodeDisposable: Disposable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(viewModel) {

            // 닫기
            startFinish.observe(this@VideouploadActivity) {
                startAct<MainActivity>() {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                finish()
            }

            startFinishPopup.observe(this@VideouploadActivity) {
                showUploadCancelPopup()
            }
            //check service
            checkEncoding.observe(this@VideouploadActivity) {
                if (!isServiceRunning(SongEncodeService::class.java) && !AppData.IS_ENCODE_ING) {
                    startUploadFiles()
                } else {
                    showToast(getString(R.string.encode_ing_msg))
                }
            }

            encodeData.observe(this@VideouploadActivity) {
                when (it.score) {
                    0 -> {
                        songScoreTxt.value =
                            resourceProvider.getString(R.string.upload_completed_star_grade)
                    }

                    1 -> {
                        songScoreTxt.value =
                            resourceProvider.getString(R.string.upload_completed_star_1)
                    }

                    2 -> {
                        songScoreTxt.value =
                            resourceProvider.getString(R.string.upload_completed_star_2)
                    }

                    else -> {
                        songScoreTxt.value =
                            resourceProvider.getString(R.string.upload_completed_star_3)
                    }
                }
            }

            //Start Encoding
            startService.observe(this@VideouploadActivity) { encodeDataList ->
                hideKeyboard()
                encodeDataList?.let {
                    DLogger.d("Start Encode Service")
                    startSongEncodeService() {
                        this.putParcelableArrayListExtra(ExtraCode.SING_ENCODE_ITEM, ArrayList(it))
                    }
                }
            }

            message.observe(this@VideouploadActivity) {
                showToast(it)
            }

            //다시선택
            startGalleryDialog.observe(this@VideouploadActivity) {
                handlePermissions()
            }

            //toast
            showToastStringMsg.observe(this@VideouploadActivity) {
                showToast(it)
            }

            showToastIntMsg.observe(this@VideouploadActivity) {
                showToast(it)
            }

            startCheckProhibit.observe(this@VideouploadActivity) {
                CommonDialog(this@VideouploadActivity)
                    .setContents(getString(R.string.str_prohibit_notice))
                    .setIcon(AppData.POPUP_WARNING)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                viewModel.checkSongEncodeService()
                            }
                        }
                    })
                    .show()
            }

            /**
             * 인코딩 결과
             */
            encodeDisposable = RxBus.listen(RxBusEvent.EncodeEvent::class.java).subscribe {
                DLogger.d("@@ 인코딩 결과 ${it.isSuccess}")

                encodeData.value?.let { curEncodeData ->

                    DLogger.d("@@ 인코딩 결과 path=> ${it.encodeData.encodeDirPath} / ${curEncodeData.encodeDirPath}")

                    if (it.encodeData.encodeDirPath == curEncodeData.encodeDirPath) {
                        if (it.isSuccess) {
                            showUploadProgress(false)
                            setUploadResult(true)
                            showToast(getString(R.string.encode_success))
                        } else {
                            //업로드 진행중 뷰 hide
                            showUploadProgress(false)
                            showToast(getString(R.string.encode_fail))
                        }
                    }
                }
            }
            start()
        }

        replaceFragment(
            binding.videouploadContainerFrameLayout.id,
            initFragment<VideouploadFragment>()
        )
    }

    private fun showUploadCancelPopup() {
        CommonDialog(this@VideouploadActivity)
            .setContents(resources.getString(R.string.singing_popup_preview_message))
            .setCancelable(true)
            .setPositiveButton(R.string.str_confirm)
            .setNegativeButton(R.string.str_cancel)
            .setListener(
                object : CommonDialog.Listener {
                    override fun onClick(which: Int) {
                        if (which == 1) {
                            finish()
                        }
                    }
                }).show()
    }

    private fun handlePermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        SPermissions(this)
            .requestPermissions(*permissions.toTypedArray())
            .build { b, _ ->
                if (b) {
                    showGalleryDialog()
                } else {
                    // 권한 거부 팝업
                    CommonDialog(this)
                        .setContents(resources.getString(R.string.str_permission_denied))
                        .setPositiveButton(R.string.str_confirm)
                        .show()
                }
            }
    }

    /**
     * 앨범에서 선택
     * 1 고정
     */
    private fun showGalleryDialog() {
        GalleryBottomSheetDialog()
            .setMaxPicker(1)
            .setVideoType()
            .setMaxSelectText(getString(R.string.gallery_invalid_file_max_confirm_message))
            .setConfirmInvalidText(getString(R.string.gallery_invalid_file_confirm_message))
            .setListener(viewModel)
            .simpleShow(supportFragmentManager)
    }


    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!encodeDisposable.isDisposed) encodeDisposable.dispose()
    }


    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event?.action === MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}