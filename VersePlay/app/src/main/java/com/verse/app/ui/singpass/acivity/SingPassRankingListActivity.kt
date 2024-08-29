package com.verse.app.ui.singpass.acivity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.ActivitySingPassRankingListBinding
import com.verse.app.extension.startAct
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.singpass.viewmodel.SingPassRankingListViewModel
import com.verse.app.utility.DLogger
import com.verse.app.utility.moveToLoginAct
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SingPassRankingListActivity :
    BaseActivity<ActivitySingPassRankingListBinding, SingPassRankingListViewModel>() {
    override val layoutId = R.layout.activity_sing_pass_ranking_list
    override val viewModel: SingPassRankingListViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    var seasonCd: String = ""
    var genreCd: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.deviceProvider.setDeviceStatusBarColor(window, R.color.white)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        requestSingPassRankingList()

        with(viewModel) {
            startFinish.observe(this@SingPassRankingListActivity) {
                finish()
            }

            requestDetailMyInfo.observe(this@SingPassRankingListActivity) {
                if (loginManager.isLogin()) {
                    startAct<SingPassUserInfoActivity>(
                        enterAni = R.anim.slide_up,
                        exitAni = R.anim.slide_down
                    ) {
                        putExtra(ExtraCode.SING_PASS_USER_INFO, it.memCd)
                        putExtra(ExtraCode.SING_PASS_GENRE_INFO, it.genreCd)
                        putExtra(ExtraCode.SING_PASS_SEASON_INFO, seasonCd)
                    }
                } else {
                    moveToLoginAct()
                }
            }

            requestDetailUserInfo.observe(this@SingPassRankingListActivity) {
                if (loginManager.isLogin()) {
                    startAct<SingPassUserInfoActivity>(
                        enterAni = R.anim.slide_up,
                        exitAni = R.anim.slide_down
                    ) {
                        putExtra(ExtraCode.SING_PASS_USER_INFO, it.memCd)
                        putExtra(ExtraCode.SING_PASS_GENRE_INFO, it.genreCd)
                        putExtra(ExtraCode.SING_PASS_SEASON_INFO, seasonCd)
                    }
                } else {
                    moveToLoginAct()
                }
            }
        }
    }

    fun requestSingPassRankingList() {
        this.seasonCd = intent.getStringExtra(ExtraCode.SING_PASS_SEASON_INFO).toString()
        this.genreCd = intent.getStringExtra(ExtraCode.SING_PASS_GENRE_INFO).toString()

        DLogger.d("seasonCd : ${seasonCd}")
        DLogger.d("genreCd : ${genreCd}")

        if (!seasonCd.isNullOrEmpty() && !genreCd.isNullOrEmpty()) {
            viewModel.requestSingPassRankingList(genreCd, AppData.Y_VALUE)
            viewModel.requestSingPassRankingList(genreCd, AppData.N_VALUE)
        } else {
            CommonDialog(this@SingPassRankingListActivity)
                .setContents(getString(R.string.network_status_rs004))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.str_confirm))
                .setListener(object : CommonDialog.Listener {
                    override fun onClick(which: Int) {
                        if (which == CommonDialog.POSITIVE) {
                            finish()
                        }
                    }
                }).show()
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }
}