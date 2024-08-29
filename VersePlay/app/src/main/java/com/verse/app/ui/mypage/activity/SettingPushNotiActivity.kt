package com.verse.app.ui.mypage.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.databinding.ActivityMypageSettingPushNotiBinding
import com.verse.app.extension.getActivity
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.mypage.viewmodel.SettingPushNotiViewModel
import com.verse.app.utility.manager.UserSettingManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingPushNotiActivity :
    BaseActivity<ActivityMypageSettingPushNotiBinding, SettingPushNotiViewModel>() {
    override val layoutId = R.layout.activity_mypage_setting_push_noti
    override val viewModel: SettingPushNotiViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.deviceProvider.setDeviceStatusBarColor(getActivity()!!.window, R.color.white)

        with(viewModel) {


            isNotificationOn.observe(this@SettingPushNotiActivity){
                setPushToggle(UserSettingManager.getSettingInfo())
            }

            accountPrefOff.observe(this@SettingPushNotiActivity) {
                UserSettingManager.getSettingInfo()?.let {
                    it.alRecAllYn = alRecAllYnF
                    it.alRecUplPrgYn = alRecUplPrgYnF
                    it.alRecUplFailYn = alRecUplFailYnF
                    it.alRecUplComYn = alRecUplComYnF
                    it.alRecDorYn = alRecDorYnF
                    it.alRecSuspYn = alRecSuspYnF
                    it.alRecMarketYn = alRecMarketYnF
                    it.alRecNorEvtYn = alRecNorEvtYnF
                    it.alRecFnVoteYn = alRecFnVoteYnF
                    it.alRecSeasonYn = alRecSeasonYnF
                    it.alRecAllFlowYn = alRecAllFlowYnF
                    it.alRecAllFeedLikeYn = alRecAllFeedLikeYnF
                    it.alRecLoungeLikeYn = alRecLoungeLikeYnF
                    it.alRecAllLikeRepYn = alRecAllLikeRepYnF
                    it.alRecDuetComYn = alRecDuetComYnF
                    it.alRecBattleComYn = alRecBattleComYnF
                    it.alRecFollowFeedYn = alRecFollowFeedYnF
                    it.alRecFollowConYn = alRecFollowConYnF
                    it.alRecDmYn = alRecDmYnF
                    it.alRecAllDmYn = alRecAllDmYnF
                    it.alRecTimeYn = alRecTimeYnF
                }
            }
            // 뒤로가기
            backpress.observe(this@SettingPushNotiActivity) {
                if (AppData.IS_MYPAGE_EDIT) {
                    UserSettingManager.getSettingInfo()?.let {
                        it.alRecAllYn = alRecAllYnF
                        it.alRecUplPrgYn = alRecUplPrgYnF
                        it.alRecUplFailYn = alRecUplFailYnF
                        it.alRecUplComYn = alRecUplComYnF
                        it.alRecDorYn = alRecDorYnF
                        it.alRecSuspYn = alRecSuspYnF
                        it.alRecMarketYn = alRecMarketYnF
                        it.alRecNorEvtYn = alRecNorEvtYnF
                        it.alRecFnVoteYn = alRecFnVoteYnF
                        it.alRecSeasonYn = alRecSeasonYnF
                        it.alRecAllFlowYn = alRecAllFlowYnF
                        it.alRecAllFeedLikeYn = alRecAllFeedLikeYnF
                        it.alRecLoungeLikeYn = alRecLoungeLikeYnF
                        it.alRecAllLikeRepYn = alRecAllLikeRepYnF
                        it.alRecDuetComYn = alRecDuetComYnF
                        it.alRecBattleComYn = alRecBattleComYnF
                        it.alRecFollowFeedYn = alRecFollowFeedYnF
                        it.alRecFollowConYn = alRecFollowConYnF
                        it.alRecDmYn = alRecDmYnF
                        it.alRecAllDmYn = alRecAllDmYnF
                        it.alRecTimeYn = alRecTimeYnF
                    }
                }
                AppData.IS_MYPAGE_EDIT = false
                finish()
            }

            showToastStringMsg.observe(this@SettingPushNotiActivity) {
                Toast.makeText(this@SettingPushNotiActivity, it, Toast.LENGTH_SHORT).show()
            }

            showDialog.observe(this@SettingPushNotiActivity) {

                if (!this@SettingPushNotiActivity.isFinishing) {

                    CommonDialog(this@SettingPushNotiActivity)
                        .setContents(getString(R.string.get_push_set_popup))
                        .setIcon(AppData.POPUP_WARNING)
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.str_confirm))
                        .setNegativeButton(getString(R.string.str_cancel))
                        .setListener(object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (which == CommonDialog.POSITIVE) {

                                    try {
                                        val appDetail = Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")
                                        )
                                        appDetail.addCategory(Intent.CATEGORY_DEFAULT)
                                        appDetail.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(appDetail)
                                    } catch (e: ActivityNotFoundException) {
                                        showToastStringMsg.value = "지원하지 않는 기기입니다."
                                    }

                                } else {
                                    showToastStringMsg.value = resources.getString(R.string.get_push_set_popup_deny_str)
                                }
                            }
                        }).show()
                }
            }

            isPushBtnAllOn.observe(this@SettingPushNotiActivity){
                if (push_uploading.value
                    && push_upload_fail.value
                    && push_upload_complete.value
                    && push_dormant_user.value
                    && push_stop_user.value
                    && push_marketing.value
                    && push_new_event.value
                    && push_vote.value
                    && push_season.value
                    && push_follow_me.value
                    && like_my_contents.value
                    && like_my_post.value
                    && like_my_comments.value
                    && complete_my_duet.value
                    && complete_my_battle.value
                    && my_follower_new_contents.value
                    && my_follower_new_post.value
                    && receive_my_follower.value
                    && get_message.value
                    && manner_mode_description.value) {

                    all_push.value = true
                    alRecAllYnF = AppData.Y_VALUE
                } else{
                    return@observe
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 권한 체크
        checkPermission()
    }
    private fun checkPermission() {
        // 노티 권한 활성화 체크
        viewModel.isNotificationOn.value =
            NotificationManagerCompat.from(this@SettingPushNotiActivity).areNotificationsEnabled()
    }
}