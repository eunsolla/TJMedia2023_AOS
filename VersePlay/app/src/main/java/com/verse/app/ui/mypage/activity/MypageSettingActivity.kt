package com.verse.app.ui.mypage.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.contants.Config
import com.verse.app.contants.NaviType
import com.verse.app.databinding.ActivityMypageSettingActivityBinding
import com.verse.app.extension.startAct
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.intro.activity.TutorialActivity
import com.verse.app.ui.mypage.viewmodel.MypageSettingViewModel
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import com.verse.app.utility.ffmpegkit.Common
import com.verse.app.utility.manager.UserSettingManager
import com.verse.app.utility.moveToWebView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class MypageSettingActivity :
    BaseActivity<ActivityMypageSettingActivityBinding, MypageSettingViewModel>() {
    override val layoutId = R.layout.activity_mypage_setting_activity
    override val viewModel: MypageSettingViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.deviceProvider.setDeviceStatusBarColor(window, R.color.white)

        with(viewModel) {

            binding.apply {
                versionName.text = deviceProvider.getVersionName() + "(" + 202311271500 + ")"

                // 이메일 수신여부
                isEmailYn.value = UserSettingManager.getSettingInfo()?.alRecEmailYn == AppData.Y_VALUE
            }
            // 뒤로가기
            backpress.observe(this@MypageSettingActivity) {
                finish()
            }

            setting_security.observe(this@MypageSettingActivity) {
                startAct<MypageSettingOnlyViewerActivity>() {
                    putExtra("title", resources.getString(R.string.mypage_setting_security))
                }
            }

            setting_email.observe(this@MypageSettingActivity) {
                UserSettingManager.getSettingInfo()?.let {
                    it.alRecEmailYn = EmailYn
                }
            }

            setting_service_lan_country.observe(this@MypageSettingActivity) {
                startAct<MypageSettingOnlyViewerActivity>() {
                    putExtra("title", resources.getString(R.string.setting_service_lan_country))
                }
            }

            setting_faq.observe(this@MypageSettingActivity) {
                startAct<FAQActivity>() {
                    putExtra("title", resources.getString(R.string.setting_faq))
                }
            }

            user_guide.observe(this@MypageSettingActivity) {
                //이용가이드 이동
                startAct<TutorialActivity>() {
                    putExtra("isUserGuide", 1)
                }
            }

            open_source_license.observe(this@MypageSettingActivity) {
                moveToWebView(resourceProvider.getString(R.string.setting_open_source_license), Config.OPEN_SOURCE_LICENSE_PATH)
            }

            logout.observe(this@MypageSettingActivity) {
                viewModel.loginManager.requestLogout(this@MypageSettingActivity, it)
            }

            //메인 이동
            startMain.observe(this@MypageSettingActivity) {
                finish()
                CoroutineScope(Dispatchers.Main).launch {
                    delay(500)
                    RxBus.publish(RxBusEvent.NaviEvent(NaviType.MAIN))
                }
            }

            // 캐시 삭제 완료 / 로그아웃 완료 팝업
            startOneButtonPopup.observe(this@MypageSettingActivity) {
                CommonDialog(this@MypageSettingActivity)
                    .setContents(getString(R.string.complete_delete_cache_file))
                    .setIcon(AppData.POPUP_COMPLETE)
                    .setPositiveButton(R.string.str_confirm)
                    .show()
            }

            // 캐시 삭제 여부 팝업 / 로그아웃 여부 팝업
            startTwoButtonPopup.observe(this@MypageSettingActivity) {
                CommonDialog(this@MypageSettingActivity)
                    .setContents(getString(R.string.delete_cached_files))
                    .setNegativeButton(R.string.str_cancel)
                    .setIcon(AppData.POPUP_HELP)
                    .setPositiveButton(R.string.str_confirm)
                    .setListener(
                        object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (which == 1) {
                                    clearAppData(baseContext)
                                }
                            }
                        })
                    .show()
            }
        }
    }

    fun clearAppData(context: Context) {
        Common.removeFiles(context.cacheDir.absolutePath)
        viewModel.singPathProvider.deleteUploadDir(true)
        viewModel.cacheoneButtonPopup()

//        val cache: File = context.getCacheDir() //캐시 폴더 호출
//        val appDir = File(cache.parent) //App Data 삭제를 위해 캐시 폴더의 부모폴더까지 호출
//        if (appDir.exists()) {
//            val children = appDir.list()
//            for (s in children) {
//                //App Data 폴더의 리스트를 deleteDir 를 통해 하위 디렉토리 삭제
//                result = deleteDir(File(appDir, s))
//            }
//
//            if (result) {
//                viewModel.cacheoneButtonPopup()
//            }
//        }
    }

    fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()

            //파일 리스트를 반복문으로 호출
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }

        //디렉토리가 비어있거나 파일이므로 삭제 처리
        return dir!!.delete()
    }
}
