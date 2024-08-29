package com.verse.app.ui.sing.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.Fragment
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.MediaType
import com.verse.app.contants.SingPageType
import com.verse.app.contants.SingType
import com.verse.app.contants.VideoUploadPageType
import com.verse.app.databinding.ActivitySingBinding
import com.verse.app.extension.clearNotificationMessage
import com.verse.app.extension.initFragment
import com.verse.app.extension.onMain
import com.verse.app.extension.popBackStackFragment
import com.verse.app.extension.replaceFragment
import com.verse.app.extension.startAct
import com.verse.app.permissions.SPermissions
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.dialog.CommonVerticalDialog
import com.verse.app.ui.dialog.EarphoneDialog
import com.verse.app.ui.main.MainActivity
import com.verse.app.ui.mypage.activity.MemberShipActivity
import com.verse.app.ui.sing.fragment.PrepareSingFragment
import com.verse.app.ui.sing.fragment.SectionFragment
import com.verse.app.ui.sing.fragment.SingingAudioFragment
import com.verse.app.ui.sing.fragment.SingingVideoFragment
import com.verse.app.ui.sing.fragment.SyncSingFragment
import com.verse.app.ui.sing.fragment.UploadFeedCompletedFragment
import com.verse.app.ui.sing.viewmodel.SingViewModel
import com.verse.app.ui.song.activity.RelatedSoundSourceActivity
import com.verse.app.ui.videoupload.VideouploadActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.NetworkConnection
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay


/**
 * 노래 콘텐츠 업로드 -> 노래 목록 - > 파트선택, 부르기
 *
 * Created by jhlee on 2023-04-05
 */
@AndroidEntryPoint
class SingActivity : BaseActivity<ActivitySingBinding, SingViewModel>() {

    override val layoutId = R.layout.activity_sing
    override val viewModel: SingViewModel by viewModels()
    override val bindingVariable = BR.viewModel
    var commonVerticalDialog: CommonVerticalDialog? = null
    var isFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppData.IS_SING_ING = true
        clearNotificationMessage()

        with(viewModel) {

            singPathProvider.deleteSongDir()

            //페이지 전환
            singPageType.observe(this@SingActivity) {
                DLogger.d("PageType $it")
                when (it) {
                    SingPageType.PREPARE -> { //부르기 설정 페이지
                        showChildFragment(initFragment<PrepareSingFragment>())
                    }

                    SingPageType.SECTION -> { //구간 부르기 페이지
                        showSectionDialogFragment()
                    }

                    SingPageType.SING_ING -> {//부르기 페이지
                        handlePermissions(curMediaType.value, false)
                    }

                    SingPageType.SYNC_SING -> showChildFragment(initFragment<SyncSingFragment>()) //완곡 싱크 설정
                    SingPageType.OFF_FEED_COMPLETE,
                    SingPageType.UPLOAD_FEED -> {
//                        startAct<VideouploadActivity> {
//                            putExtra(ExtraCode.SING_ENCODE_ITEM, viewModel.encodeData.value)
//                            putExtra(ExtraCode.UPLOAD_PAGE_TYPE, VideoUploadPageType.SING_CONTENTS)
//                        }
//                        finish()
                    } //업로드
                    SingPageType.UPLOAD_FEED_COMPLETE -> showChildFragment(initFragment<UploadFeedCompletedFragment>()) //업로드 완료
                    else -> {}
                }
            }

            showSection.observe(this@SingActivity) {
                if (it) {
                    showSectionDialogFragment()
                }
            }

            startUploadPage.observe(this@SingActivity) {
                startAct<VideouploadActivity> {
                    putExtra(ExtraCode.SING_ENCODE_ITEM, viewModel.encodeData.value)
                    putExtra(ExtraCode.UPLOAD_PAGE_TYPE, VideoUploadPageType.SING_CONTENTS)
                }
                finish()
            }

            //연관 음원 이동
            moveToRelatedSoundSource.observe(this@SingActivity) {
                if (it.isEmpty()) return@observe
                startAct<RelatedSoundSourceActivity>() {
                    putExtra(ExtraCode.SINGING_SONG_MNG_CD, it)
                }
            }

            //error
            showErrorDialog.observe(this@SingActivity) {

                val msg = if (!it.fromServerErrMsg.isNullOrEmpty()) {
                    it.fromServerErrMsg
                } else {
                    getString(it.fromResErrMsg)
                }

                CommonDialog(this@SingActivity)
                    .setContents(msg)
                    .setIcon(AppData.POPUP_WARNING)
                    .setPositiveButton(getString(R.string.str_confirm))
                    .setListener(
                        object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                finish()
                            }
                        })
                    .show()
            }

            showPausePopup.observe(this@SingActivity) {
                if (it) {
                    when (singPageType.value) {
                        SingPageType.SING_ING -> {
                            showPausePopup()
                        }

                        SingPageType.SYNC_SING -> {
                            showUploadCancelPopup()
                        }

                        else -> {
                            finish()
                        }
                    }
                }
            }

            //total ms
            curTotalMs.observe(this@SingActivity) {
                if (it >= 0) {
                    totalMsText.value = getTime(it.toInt())
                }
            }

            //닫기
            finish.observe(this@SingActivity) {
                if (singPageType.value == SingPageType.PREPARE) {
                    finish()
                } else {
                    checkPlaying()
                }
            }

            //이어폰
            showEarphonePopup.observe(this@SingActivity) {
                showEarphoneDialog()
            }

            //45초 부른 후  팝업
            showNotWholeSongPopup.observe(this@SingActivity) {
                CommonDialog(this@SingActivity)
                    .setIcon("")
                    .setContents(resources.getString(R.string.singing_popup_not_whole_song))
                    .setPositiveButton(R.string.str_confirm)
                    .setListener(
                        object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                finish()
                            }
                        })
                    .show()
            }

            //vip 팝업
            showVipPopup.observe(this@SingActivity) {
                CommonDialog(this@SingActivity)
                    .setIcon("")
                    .setContents(resources.getString(R.string.singing_popup_vip))
                    .setPositiveButton(R.string.membership_title)
                    .setNegativeButton(getString(R.string.str_confirm))
                    .setListener(
                        object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (which == CommonDialog.POSITIVE) {
                                    startAct<MemberShipActivity>()

                                } else {
                                    moveToPage(SingPageType.SING_ING)
                                }
                            }
                        })
                    .show()
            }

            songMainData.observe(this@SingActivity) {
                handlePermissions("", true)
            }

            setEarphoneRegisterReceiver()
            setNetWorkReceiver()
            start()
            initStartRefresh()
        }
    }

    private fun showSectionDialogFragment() {
        with(viewModel) {
            curMrAndXtfPath.value?.let {
                SectionFragment()
                    .setDto(
                        it.third,
                        Pair(sectionCurStartIndex.value, sectionCurEndIndex.value),
                        Pair(songMainData.value?.songNm, songMainData.value?.artNm),
                        songMainData.value?.albImgPath
                    )
                    .setListener(viewModel)
                    .show(supportFragmentManager)
            }
        }
    }

    private fun handlePermissions(type: String, isFirst: Boolean) {

        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE
            )
        } else {
            listOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE
            )
        }
        SPermissions(this)
            .requestPermissions(*permissions.toTypedArray())
            .build { b, _ ->
                if (b) {
                    if (isFirst) {
                        //record files path init
                        viewModel.initPaths()
                    } else {
                        if (type == MediaType.VIDEO.code) {
                            showChildFragment(initFragment<SingingVideoFragment>()) //비디오 부르기
                        } else {
                            showChildFragment(initFragment<SingingAudioFragment>())//오디오 부르기
                        }
                        seCallRegisterReceiver()
                    }
                } else {
                    // 권한 거부 팝업
                    CommonDialog(this)
                        .setContents(resources.getString(R.string.singing_permission_need_all))
                        .setPositiveButton(R.string.str_confirm)
                        .setListener(object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (isFirst) {
                                    finish()
                                }
                            }
                        })
                        .show()
                }
            }
    }


    /**
     * 이어폰 팝업.
     */
    private fun showEarphoneDialog() {
        viewModel.curSingType.value?.let {
            EarphoneDialog(this@SingActivity)
                .setSingType(it)
                .setListener(object : EarphoneDialog.Listener {
                    override fun onClick(which: Int) {
                        onMain {
                            delay(500)
                            viewModel.onPlay()
                        }
                    }
                }).show()
        }
    }

    /**
     * 부르기 상태별 정지 팝업
     */
    private fun showPausePopup() {

        if (commonVerticalDialog != null) {
            commonVerticalDialog = null
        }

        with(viewModel) {
            DLogger.d("showPasuePopup showPausePopup=> ${isOneMinute.value}")
            if (!isOneMinute.value) {
                commonVerticalDialog = CommonVerticalDialog(this@SingActivity)
                    .setContents(resources.getString(R.string.singing_popup_pause_message))
                    .setBtnOne(resources.getString(R.string.singing_popup_sing_continue))
                    .setBtnTwo(resources.getString(R.string.singing_popup_sing_again))
                    .setBtnThree(resources.getString(R.string.singing_popup_sing_different))
                    .setBtnFour(resources.getString(R.string.singing_popup_sing_part_change))
                    .setCanDismiss(isNetwork())
                    .setListener(object : CommonVerticalDialog.Listener {
                        override fun onClick(which: Int) {
                            when (which) {
                                CommonVerticalDialog.POSITION_1 -> {
                                    if (!isNetwork()) {
                                        return
                                    }
                                    dismissPausePopup()
                                    onContinueSinging()
                                }

                                CommonVerticalDialog.POSITION_2 -> {
                                    if (!isNetwork()) {
                                        return
                                    }
                                    dismissPausePopup()
                                    onSingAgain()
                                }

                                CommonVerticalDialog.POSITION_3 -> {
                                    finish()
                                }

                                CommonVerticalDialog.POSITION_4 -> {
                                    dismissPausePopup()
                                    changePart()
                                }

                                else -> {
                                    if (!isNetwork()) {
                                        return
                                    }
                                    onContinueSinging()
                                }
                            }
//                            showPausePopup(false)
                        }
                    })

                commonVerticalDialog?.show()

            } else {

                if (curSingType.value == SingType.SOLO && off.value == false) {

                    commonVerticalDialog = CommonVerticalDialog(this@SingActivity)
                        .setContents(resources.getString(R.string.singing_popup_pause_message))
                        .setBtnOne(resources.getString(R.string.singing_popup_sing_continue))
                        .setBtnTwo(resources.getString(R.string.singing_popup_sing_again))
                        .setBtnThree(resources.getString(R.string.singing_popup_sing_different))
                        .setBtnFour(resources.getString(R.string.singing_popup_sing_stop_and_save))
                        .setBtnFive(resources.getString(R.string.singing_popup_sing_part_change))
                        .setCanDismiss(isNetwork())
                        .setListener(object : CommonVerticalDialog.Listener {
                            override fun onClick(which: Int) {
                                when (which) {
                                    CommonVerticalDialog.POSITION_1 -> {
                                        if (!isNetwork()) {
                                            return
                                        }
                                        dismissPausePopup()
                                        onContinueSinging()
                                    }

                                    CommonVerticalDialog.POSITION_2 -> {
                                        if (!isNetwork()) {
                                            return
                                        }
                                        dismissPausePopup()
                                        onSingAgain()
                                    }

                                    CommonVerticalDialog.POSITION_3 -> {
                                        finish()
                                    }

                                    CommonVerticalDialog.POSITION_4 -> {

                                        if (!isNetwork()) {
                                            return
                                        }

                                        onSingingEditAndSave()
                                    }

                                    CommonVerticalDialog.POSITION_5 -> {
                                        dismissPausePopup()
                                        changePart()
                                    }

                                    else -> {
                                        if (!isNetwork()) {
                                            return
                                        }
                                        onContinueSinging()
                                    }
                                }
//                                showPausePopup(false)
                            }
                        })

                    commonVerticalDialog?.show()

                } else {

                    commonVerticalDialog = CommonVerticalDialog(this@SingActivity)
                        .setContents(resources.getString(R.string.singing_popup_pause_message))
                        .setBtnOne(resources.getString(R.string.singing_popup_sing_continue))
                        .setBtnTwo(resources.getString(R.string.singing_popup_sing_again))
                        .setBtnThree(resources.getString(R.string.singing_popup_sing_different))
                        .setBtnFour(resources.getString(R.string.singing_popup_sing_part_change))
                        .setCanDismiss(isNetwork())
                        .setListener(object : CommonVerticalDialog.Listener {
                            override fun onClick(which: Int) {
                                when (which) {
                                    CommonVerticalDialog.POSITION_1 -> {
                                        if (!isNetwork()) {
                                            return
                                        }
                                        dismissPausePopup()
                                        onContinueSinging()
                                    }

                                    CommonVerticalDialog.POSITION_2 -> {
                                        if (!isNetwork()) {
                                            return
                                        }
                                        dismissPausePopup()
                                        onSingAgain()
                                    }

                                    CommonVerticalDialog.POSITION_3 -> {
                                        finish()
                                    }

                                    CommonVerticalDialog.POSITION_4 -> {
                                        dismissPausePopup()
                                        changePart()
                                    }

                                    else -> {
                                        if (!isNetwork()) {
                                            return
                                        }
                                        onContinueSinging()
                                    }
                                }
//                                showPausePopup(false)
                            }
                        })
                    commonVerticalDialog?.show()
                }
            }
        }
    }

    private fun showUploadCancelPopup() {
        CommonVerticalDialog(this@SingActivity)
            .setContents(resources.getString(R.string.singing_popup_preview_message))
            .setBtnOne(resources.getString(R.string.singing_popup_preview_other_sing))
            .setBtnTwo(resources.getString(R.string.singing_popup_preview_exit))
            .setListener(object : CommonVerticalDialog.Listener {
                override fun onClick(which: Int) {
                    when (which) {
                        CommonVerticalDialog.POSITION_1 -> finish()
                        CommonVerticalDialog.POSITION_2 -> {
                            startAct<MainActivity>(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                            ) {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            }

                            finish()
                        }
                    }
                    viewModel.showPausePopup(false)
                }
            }).show()
    }

    private fun showChildFragment(fragment: Fragment) {
        hideChildFragment()
        replaceFragment(binding.singContainerFrameLayout.id, fragment)
    }

    private fun hideChildFragment() {
        popBackStackFragment()
    }

    /**
     * 이어폰 리시버 등록
     */
    private fun setEarphoneRegisterReceiver() {
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        registerReceiver(earphoneBroadcastReceiver, filter)
    }

    /**
     * 네트워크 상태 체크
     *  isConnected false : 부르기중 팝업 노출
     */
    private fun setNetWorkReceiver() {
        NetworkConnection(this@SingActivity).apply {
            this.observe(this@SingActivity) { isConnected ->
                viewModel.setNetWorkState(isConnected)
                setPausePopupNetworkState(isConnected)
                if (!isConnected) {
                    showPauseAfterPopup()
                }
            }
        }
    }

    /**
     * 이어폰 리시버 해제
     */
    private fun removeEarphoneRegisterReceiver() {
        try {
            unregisterReceiver(earphoneBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
            DLogger.d("Earphone removeEarphoneRegisterReceiver => ${e}")
        } catch (e: Exception) {
            DLogger.d("Earphone removeEarphoneRegisterReceiver => ${e}")
        }
    }

    /**
     * 전화 리시버 등록
     */
    private fun seCallRegisterReceiver() {
        val intentFilter = IntentFilter(TelephonyManager.EXTRA_STATE).apply {
            addAction(TelephonyManager.EXTRA_STATE_RINGING)
            addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        }

        registerReceiver(incomingCallReceiver, intentFilter)
    }


    /**
     * 전화 리시버 해제
     */
    private fun removeCallRegisterReceiver() {
        try {
            unregisterReceiver(incomingCallReceiver)
        } catch (e: IllegalArgumentException) {
            DLogger.d("Remove IncomingCallReceiver => ${e}")
        } catch (e: Exception) {
            DLogger.d("Remove IncomingCallReceiver => ${e}")
        }
    }


    /**
     * 이어폰 리시버 Call back
     */
    private var earphoneBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            var action = intent?.action
            DLogger.d("earphoneBroadcastReceiver action=> ${action}")
            if (action == Intent.ACTION_HEADSET_PLUG) {
                val isEarphoneOn = intent.getIntExtra("state", 0) > 0
                val isMicrophone = intent.getIntExtra("microphone", 0) > 0
                val name = intent.getStringExtra("name")
                val isEnable = isEarphoneOn && isMicrophone
                DLogger.d("earphoneBroadcastReceiver ACTION_HEADSET_PLUG=> ${isEarphoneOn} / ${isMicrophone} / ${name} / ${isEnable}")
                viewModel.setSpEnableVoice(isEnable)
            }
        }
    }

    /**
     * 전화 Call back
     */
    private var incomingCallReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        private var mLastState: String? = null

        override fun onReceive(context: Context, intent: Intent) {

            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

            if (state == mLastState) {
                return
            } else {
                mLastState = state
            }

            if (TelephonyManager.EXTRA_STATE_RINGING == state) {
                showPauseAfterPopup()
            }
        }
    }

    //부르기 중 pause 시  show
    private fun showPauseAfterPopup() {
        viewModel.clearContinueSinging()
        if (commonVerticalDialog != null && commonVerticalDialog?.isShowing == true) {
            return
        }
        viewModel.showPausePopup(false)
        viewModel.showPauseAfterPopup()
    }

    /**
     * 팝업 show 중이면 네트워크 상태 set
     */
    private fun setPausePopupNetworkState(state: Boolean) {
        if (commonVerticalDialog != null) {
            if (commonVerticalDialog?.isShowing == true) {
                commonVerticalDialog?.setCanDismiss(state)
            }
        }
    }

    /**
     * 팝업 show 중이면 네트워크 상태 set
     */
    private fun dismissPausePopup() {
        if (commonVerticalDialog != null) {
            if (commonVerticalDialog?.isShowing == true) {
                commonVerticalDialog?.dismiss()
            }
        }
    }

    override fun onResume() {
        viewModel.setLifeState(true)
        super.onResume()
    }

    override fun onPause() {
        viewModel.setLifeState(false)

        if (!isFinished) {
            if (viewModel.isLoading.value == false) {
                showPauseAfterPopup()
            }
        }
        super.onPause()
    }


    override fun finish() {
        isFinished = true
        dismissPausePopup()
        AppData.IS_SING_ING = false
        viewModel.onExitSing()
        removeEarphoneRegisterReceiver()
        removeCallRegisterReceiver()
        super.finish()
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down)
    }

}