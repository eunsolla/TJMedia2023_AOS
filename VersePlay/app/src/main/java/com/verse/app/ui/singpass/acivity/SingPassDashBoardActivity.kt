package com.verse.app.ui.singpass.acivity

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.base.adapter.BaseFragmentPagerAdapter
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.LevelType
import com.verse.app.contants.TabPageType
import com.verse.app.databinding.ActivitySingPassDashBoardBinding
import com.verse.app.extension.getActivity
import com.verse.app.extension.initFragment
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.singpass.fragment.TabSingPassDailyMissionFragment
import com.verse.app.ui.singpass.fragment.TabSingPassPeriodMissionFragment
import com.verse.app.ui.singpass.fragment.TabSingPassSeasonMissionFragment
import com.verse.app.ui.singpass.viewmodel.SingPassDashBoardViewModel
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable

@AndroidEntryPoint
class SingPassDashBoardActivity :
    BaseActivity<ActivitySingPassDashBoardBinding, SingPassDashBoardViewModel>() {
    override val layoutId = R.layout.activity_sing_pass_dash_board
    override val viewModel: SingPassDashBoardViewModel by viewModels()
    override val bindingVariable = BR.viewModel
    private lateinit var singPassDisposable: Disposable

    var userMemCd: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.deviceProvider.setDeviceStatusBarColor(getActivity()!!.window, R.color.white)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        requestSingPassDashBoardInfo()

        with(viewModel) {
            startFinish.observe(this@SingPassDashBoardActivity) {
                finish()
            }

            vpTabPosition.observe(this@SingPassDashBoardActivity) {
                DLogger.d("vpTabPosition : ${vpTabPosition.value}")
            }

            singPassDashBoardData.observe(this@SingPassDashBoardActivity) {
                // 강제 종료
                when (it.seasonInfo.seaLevelNm) {
                    LevelType.LEVEL_1.code -> {
                        binding.ivLevel.setImageResource(R.drawable.sing_level_1)
                    }

                    LevelType.LEVEL_2.code -> {
                        binding.ivLevel.setImageResource(R.drawable.sing_level_2)
                    }

                    LevelType.LEVEL_3.code -> {
                        binding.ivLevel.setImageResource(R.drawable.sing_level_3)
                    }

                    LevelType.LEVEL_4.code -> {
                        binding.ivLevel.setImageResource(R.drawable.sing_level_4)
                    }

                    LevelType.LEVEL_5.code -> {
                        binding.ivLevel.setImageResource(R.drawable.sing_level_5)
                    }
                    else -> {
                        binding.ivLevel.setImageResource(R.drawable.sing_level_1)
                    }
                }
            }

            /**
             * 씽패스 Refresh
             */
            singPassDisposable = RxBus.listen(RxBusEvent.SingPassEvent::class.java).subscribe {
                DLogger.d("Request Refresh SingPassDashBoard : ${it.isRefresh}")
                if (it.isRefresh) {
                    requestSingPassDashBoardInfo()
                }
            }
        }
    }

    private fun requestSingPassDashBoardInfo() {
        this.userMemCd = intent.getStringExtra(ExtraCode.SING_PASS_USER_INFO).toString()
        DLogger.d("userMemCd : ${userMemCd}")

        if (userMemCd.isNotEmpty()) {
            viewModel.requestSingPassDashBoardInfo(userMemCd)
        } else {
            CommonDialog(this@SingPassDashBoardActivity)
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

    class MissionTabFragmentPagerAdapter(val ctx: Context, private val viewModel: BaseViewModel?) :
        BaseFragmentPagerAdapter<String>(ctx) {

        override fun onCreateFragment(pos: Int) = when (pos) {
            0 -> {
                initFragment<TabSingPassDailyMissionFragment>() {
                    putSerializable(ExtraCode.TAB_TYPE, TabPageType.DAILY_MISSION)
                }
            }

            1 -> {
                initFragment<TabSingPassPeriodMissionFragment>() {
                    putSerializable(ExtraCode.TAB_TYPE, TabPageType.PERIOD_MISSION)
                }
            }

            else -> {
                initFragment<TabSingPassSeasonMissionFragment>() {
                    putSerializable(ExtraCode.TAB_TYPE, TabPageType.SEASON_MISSION)
                }
            }
        }
    }

//    private fun showAddChildFragment(fragment: Fragment) {
//        addChildFragment(binding.myContainerFrameLayout.id, fragment)
//    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }
}