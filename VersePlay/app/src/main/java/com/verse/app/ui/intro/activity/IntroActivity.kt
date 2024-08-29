package com.verse.app.ui.intro.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.messaging.FirebaseMessaging
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.Config
import com.verse.app.contants.MainEntryType
import com.verse.app.contants.UserStateType
import com.verse.app.databinding.ActivityIntroBinding
import com.verse.app.extension.exitApp
import com.verse.app.extension.startAct
import com.verse.app.repository.preferences.AccountPref
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.dialog.ServerCheckDialog
import com.verse.app.ui.intro.viewmodel.IntroViewModel
import com.verse.app.ui.login.activity.NicknameActivity
import com.verse.app.ui.mypage.activity.QNAActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.moveToUserStAct
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class IntroActivity : BaseActivity<ActivityIntroBinding, IntroViewModel>() {

    override val layoutId = R.layout.activity_intro
    override val viewModel: IntroViewModel by viewModels()
    override val bindingVariable = BR.viewModel
    private var mPushReady = false // 푸시 상태값

    @Inject
    lateinit var accountPref: AccountPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getInstanceIdToken()

        with(viewModel) {

            //국가/언어 설정 이동
            startCountryActivity.observe(this@IntroActivity) {
                startAct<SetCountryActivity>(
                    enterAni = android.R.anim.fade_in,
                    exitAni = android.R.anim.fade_out
                )
                finish()
            }

            //메인 이동
            startMain.observe(this@IntroActivity) {
                RxBus.publish(RxBusEvent.MainEnterEvent(MainEntryType.VERSION, true))
            }

            // 서버 점검 팝업
            startServiceCheckDialog.observe(this@IntroActivity) {
                ServerCheckDialog(this@IntroActivity)
                    .setDialogData(it)
                    .setCancelable(false)
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            exitApp()
                        }
                    })
                    .show()
            }

            // 선택 업데이트
            startOptionalUpdateDialog.observe(this@IntroActivity) {
                ServerCheckDialog(this@IntroActivity)
                    .setDialogData(it)
                    .setCancelable(false)
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Config.GOOGLE_PLAY_STORE_VERSE_PLAY_URL)))
                                exitApp()
                            } else {
                                if (!accountPref.getAuthTypeCd().isEmpty() && !accountPref.getJWTToken().isEmpty()) {
                                    val authTypeCd = accountPref.getAuthTypeCd() // sns 타입
                                    autoTokenLogin(authTypeCd)
                                } else {
                                    if (!accountPref.isIntroSettingPageShow()) {
                                        startCountryActivity.call()
                                    } else {
                                        startMain.call()
                                    }
                                }
                            }
                        }
                    })
                    .show()
            }

            // 강제 업데이트
            startRequireUpdateDialog.observe(this@IntroActivity) {
                ServerCheckDialog(this@IntroActivity)
                    .setDialogData(it)
                    .setCancelable(false)
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Config.GOOGLE_PLAY_STORE_VERSE_PLAY_URL)))
                                exitApp()
                            } else {
                                exitApp()
                            }
                        }
                    })
                    .show()
            }

            //유저 상태에 따라 화면 이동 처리
            memStCd.observe(this@IntroActivity) {
                if (it.isNotEmpty()) {
                    if (memStCd.value == UserStateType.NON_MEMBER.code) {
                        RxBus.publish(RxBusEvent.MainEnterEvent(MainEntryType.VERSION, true))
                    } else {
                        moveToUserStAct(this@IntroActivity, viewModel.accountPref, viewModel.loginManager, true, it, "", "")
                    }
                }
            }

            startSendFeedback.observe(this@IntroActivity) {
                finish()
                startAct<QNAActivity>()
            }

            startMoveNickname.observe(this@IntroActivity) {
                finish()
                startAct<NicknameActivity>(
                    enterAni = android.R.anim.fade_in, exitAni = android.R.anim.fade_out
                ) {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            startAppConfig()
            viewModel.startSplash()
        }
    }

    private fun getInstanceIdToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                mPushReady = true

                viewModel.accountPref.setFcmPushToken(token)
                DLogger.d("setFcmPushToken token : ${token}")
            })

            .addOnFailureListener(OnFailureListener { task ->
                DLogger.d("setFcmPushToken token failed")
            })
    }


    // 뒤로가기 버튼 막기
    override fun onBackPressed() {}

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    /**
     * 앱 실행 시 초기 설정
     */
    private fun startAppConfig() {
        settingVolume()
        deleteSongFiles()
    }

    private fun settingVolume() {
        var mute = accountPref.getVolumeMute(this@IntroActivity)
        var auto = accountPref.getVolumeAuto(this@IntroActivity)
        var curVtype = accountPref.getCurVolume(this@IntroActivity)
        DLogger.d("saveData=> curSettingMute $mute /curAutoSettingState  $auto / curVolume $curVtype")

        if (mute) {
            viewModel.deviceProvider.setVolume(0)
        }
        if (auto) {
            when (curVtype) {
                "1" -> {
                    viewModel.deviceProvider.setVolume((viewModel.deviceProvider.getMaxVolume() * 0.25).toInt())
                }

                "2" -> {
                    viewModel.deviceProvider.setVolume((viewModel.deviceProvider.getMaxVolume() * 0.5).toInt())
                }

                "3" -> {
                    viewModel.deviceProvider.setVolume((viewModel.deviceProvider.getMaxVolume() * 0.75).toInt())
                }
            }
        }
    }

    /**
     * 앱 실행 시 반주음/xtf, 앨범 Dir 삭제
     */
    private fun deleteSongFiles() {
        viewModel.singPathProvider.deleteUploadDir(true)
    }
}