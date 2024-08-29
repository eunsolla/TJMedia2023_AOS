package com.verse.app.ui.main.fragment

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.verse.app.R
import com.verse.app.base.adapter.BaseChildFragmentPagerAdapter
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.AppData
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.SingType
import com.verse.app.databinding.FragmentMainTabBinding
import com.verse.app.extension.getCurrentFragment
import com.verse.app.extension.initFragment
import com.verse.app.extension.reduceDragSensitivity
import com.verse.app.extension.startAct
import com.verse.app.permissions.SPermissions
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.dialog.EventPushAgreeListener
import com.verse.app.ui.dialog.EventPushAgreePopupDialog
import com.verse.app.ui.dialog.MainNoticePopupDialog
import com.verse.app.ui.main.viewmodel.MainTabViewModel
import com.verse.app.ui.main.viewmodel.MainViewModel
import com.verse.app.ui.search.activity.SearchMainActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.manager.UserSettingManager
import com.verse.app.widget.views.ExoPagerItem
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : MainTabFragment
 *
 * Created by jhlee on 2023-01-31
 */
@AndroidEntryPoint
class MainTabFragment : BaseFragment<FragmentMainTabBinding, MainTabViewModel>() {

    override val layoutId: Int = R.layout.fragment_main_tab
    override val viewModel: MainTabViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel
    private val activityViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vpTab.reduceDragSensitivity()

        with(viewModel) {

            //탭 클릭
            vpMainTabPosition.observe(viewLifecycleOwner) {
                binding.vpTab.setCurrentItem(it.first, it.second)
            }

            //tab change call back
            binding.vpTab.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (viewModel.vpMainTabPosition.value.first != position) {
                        viewModel.vpMainTabPosition.value = position to true
                    }

                    if (position > 0) {
                        //광고가 아니고 추천탭이면 메인 vp 활성화
                        getCurrentFragment<MainFeedFragment>(binding.vpTab)?.let {

                            if (it.viewModel._feedList.value.isNullOrEmpty()) return

                            it.viewModel.exoManager.getCurrentPlayer()?.let {
                                it.getFeedItem().feedContentsData?.let { feed ->
                                    if (feed.paTpCd == SingType.AD.code) {
                                        activityViewModel.setEnableMainViewpager(false)
                                    } else {
                                        activityViewModel.setEnableMainViewpager(true)
                                    }
                                }
                            }
                        } ?: run {
                            activityViewModel.setEnableMainViewpager(true)
                        }
                    } else {
                        activityViewModel.setEnableMainViewpager(false)
                    }

                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                }
            })

            //검색 이동
            moveToSearch.observe(viewLifecycleOwner) {
                requireActivity().startAct<SearchMainActivity>(
                    enterAni = R.anim.in_right_to_left,
                    exitAni = R.anim.out_right_to_left
                ) {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            // 메인 공지 팝업 Observe
            isShowMainNoticePopup.observe(viewLifecycleOwner) {
                if (it) {
                    showMainNoticePopup()
                }
            }
            accountPrefOff.observe(viewLifecycleOwner) {

                UserSettingManager.getSettingInfo()?.let {
                    it.alRecAllYn = AppData.N_VALUE
                    it.alRecUplPrgYn = AppData.N_VALUE
                    it.alRecUplFailYn = AppData.N_VALUE
                    it.alRecUplComYn = AppData.N_VALUE
                    it.alRecDorYn = AppData.N_VALUE
                    it.alRecSuspYn = AppData.N_VALUE
                    it.alRecMarketYn = AppData.N_VALUE
                    it.alRecNorEvtYn = AppData.N_VALUE
                    it.alRecFnVoteYn = AppData.N_VALUE
                    it.alRecSeasonYn = AppData.N_VALUE
                    it.alRecAllFlowYn = AppData.N_VALUE
                    it.alRecAllFeedLikeYn = AppData.N_VALUE
                    it.alRecLoungeLikeYn = AppData.N_VALUE
                    it.alRecAllLikeRepYn = AppData.N_VALUE
                    it.alRecDuetComYn = AppData.N_VALUE
                    it.alRecBattleComYn = AppData.N_VALUE
                    it.alRecFollowFeedYn = AppData.N_VALUE
                    it.alRecFollowConYn = AppData.N_VALUE
                    it.alRecDmYn = AppData.N_VALUE
                    it.alRecAllDmYn = AppData.N_VALUE
                    it.alRecTimeYn = AppData.N_VALUE
                }
            }
        }

        viewModel.requestMainNoticeInfo()
    }

    private fun showMainNoticePopup() {
        if (viewModel.accountPref.getRecentMainPopupMngCd() != viewModel.mainNoticePopupData.value!!.result!!.svcPopMngCd
            || viewModel.accountPref.getShowMainPopup()
        ) {
            viewModel.accountPref.setShowMainPopup(true)
            MainNoticePopupDialog(requireContext(), viewModel.mainNoticePopupData.value, viewModel.accountPref, mMainNoticePopupClickListener).show()
        } else {
            if (viewModel.loginManager.isLogin() && !viewModel.accountPref.getNightTimeAgreePushPopup()) {
                EventPushAgreePopupDialog(requireContext(), viewModel.accountPref, mEventPushAgreeListener).show()
            }
        }
    }

    private val mMainNoticePopupClickListener = View.OnClickListener {
        DLogger.d("mMainNoticePopupClickListener Click")
        if (viewModel.loginManager.isLogin() && !viewModel.accountPref.getNightTimeAgreePushPopup()) {
            EventPushAgreePopupDialog(requireContext(), viewModel.accountPref, mEventPushAgreeListener).show()
        }
    }

    private val mEventPushAgreeListener = object : EventPushAgreeListener {
        override fun onAgree(isOnSwitch: Boolean) {
            viewModel.updateNightTimePushAgree(isOnSwitch)
            // 33 이상인 경우 권한 체크 로직 자동 수행됨
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                if (!viewModel.deviceProvider.isPermissionsCheck(Manifest.permission.POST_NOTIFICATIONS)) {
                    handlePermissions()
                }
            }
        }
    }

    private fun handlePermissions() {
        val permissions = listOf(Manifest.permission.POST_NOTIFICATIONS)
        SPermissions(this)
            .requestPermissions(*permissions.toTypedArray())
            .build { b, _ ->
                if (b) {
//                    Toast.makeText(
//                        requireContext(), "허용", Toast.LENGTH_SHORT
//                    ).show()
                } else {
                    // 권한 거부
                    viewModel.updatePushSetting()
//                    Toast.makeText(
//                        requireContext(), "거부", Toast.LENGTH_SHORT
//                    ).show()
                }
            }
    }

    fun showNotiCheckPopup(){
        CommonDialog(requireContext())
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
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${requireActivity().packageName}")
                            )
                            appDetail.addCategory(Intent.CATEGORY_DEFAULT)
                            appDetail.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(appDetail)

                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                requireContext(), "지원하지 않는 기기입니다.", Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        viewModel.updatePushSetting()
                    }
                }
            }).show()
    }

    /**
     * main current tab refresh
     */
    fun refreshTab() {
        getCurrentFragment<MainFeedFragment>(binding.vpTab)?.refresh(binding.vpTab.currentItem)
    }

    /**
     * main tab All refresh
     */
    fun refreshTabAll() {
        childFragmentManager.fragments.forEachIndexed { index, fragment ->
            (fragment as MainFeedFragment).refresh(index)
        }
    }

    /**
     * 타 페이지 돌아온 후 다시 재생
     */
    fun onResumePlayer() {
        getCurrentFragment<MainFeedFragment>(binding.vpTab)?.onResumePlayer()
    }

    fun getTabPosition(): Int {
        return viewModel.vpMainTabPosition.value.first
    }

    /**
     * ViewPager Fragment
     */
    class MainTabFragmentPagerAdapter(ctx: Fragment, private val viewModel: BaseViewModel?) : BaseChildFragmentPagerAdapter<String>(ctx) {
        override fun onCreateFragment(pos: Int) = initFragment<MainFeedFragment> {
            val item = dataList[pos] as ExoPagerItem
            item.pos = pos
            DLogger.d("item => ${item.name} / ${item.type.name}  / ${item.isFirstPlay} / ${pos}")
            putParcelable(ExtraCode.EXO_PAGE_TYPE, item)
        }
    }
}
