package com.verse.app.ui.singpass.fragment

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.LevelType
import com.verse.app.databinding.FragmentSingPassBinding
import com.verse.app.extension.getActivity
import com.verse.app.extension.startAct
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.main.viewmodel.MainViewModel
import com.verse.app.ui.mypage.activity.MemberShipActivity
import com.verse.app.ui.singpass.acivity.SingPassDashBoardActivity
import com.verse.app.ui.singpass.acivity.SingPassRankingListActivity
import com.verse.app.ui.singpass.acivity.SingPassUserInfoActivity
import com.verse.app.ui.singpass.viewmodel.SingPassViewModel
import com.verse.app.ui.song.activity.SongMainActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.moveToLoginAct
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 씽패스 fragment
 *
 * Created by jhlee on 2023-01-31
 */
@AndroidEntryPoint
class SingPassFragment() : BaseFragment<FragmentSingPassBinding, SingPassViewModel>() {

    override val layoutId: Int = R.layout.fragment_sing_pass
    override val viewModel: SingPassViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel
    private val activityViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycle.addObserver(binding.indicatorView)

        viewModel.deviceProvider.setDeviceStatusBarColor(context.getActivity()!!.window, R.color.black)

        viewModel.initStartRefresh()
        viewModel.requestSingPass()

        with(viewModel) {
            singPassDataList.observe(viewLifecycleOwner) {

                if (it?.seasonInfo == null) {
                    binding.ivLevel.visibility = View.GONE
                    binding.clSingPassNewSeason.visibility = View.GONE
                    binding.llSingPassEmptySeason.visibility = View.VISIBLE
                    exoPlayerLock(true)
                } else if (it.seasonInfo?.genrePageCnt!! > 0) {

                    if (!loginManager.isLogin()) {
                        binding.ivLevel.visibility = View.GONE
                    }else{
                        binding.ivLevel.visibility = View.VISIBLE
                    }

                    binding.clSingPassNewSeason.visibility = View.GONE
                    binding.llSingPassEmptySeason.visibility = View.GONE
                } else {
                    binding.ivLevel.visibility = View.GONE
                    binding.clSingPassNewSeason.visibility = View.VISIBLE
                    binding.llSingPassEmptySeason.visibility = View.GONE
                    exoPlayerLock(true)
                }

                if (it?.seasonInfo != null) {
                    when (it.seasonInfo?.seaLevelNm) {
                        LevelType.LEVEL_1.code -> {
                            binding.ivLevel.setImageResource(R.drawable.ic_sing_pass_level_1)
                        }

                        LevelType.LEVEL_2.code -> {
                            binding.ivLevel.setImageResource(R.drawable.ic_sing_pass_level_2)
                        }

                        LevelType.LEVEL_3.code -> {
                            binding.ivLevel.setImageResource(R.drawable.ic_sing_pass_level_3)
                        }

                        LevelType.LEVEL_4.code -> {
                            binding.ivLevel.setImageResource(R.drawable.ic_sing_pass_level_4)
                        }

                        LevelType.LEVEL_5.code -> {
                            binding.ivLevel.setImageResource(R.drawable.ic_sing_pass_level_5)
                        }
                    }
                }
            }

            vpState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    ViewPager2.SCROLL_STATE_SETTLING -> {
                        onExoPause()
                    }

                    ViewPager2.SCROLL_STATE_IDLE -> {
                        onExoPlay()
                    }
                }
            }

            requestDetailMyInfo.observe(viewLifecycleOwner) {
                if (loginManager.isLogin()) {
                    requireActivity().startAct<SingPassUserInfoActivity>(
                        enterAni = R.anim.slide_up,
                        exitAni = R.anim.slide_down
                    ) {
                        putExtra(ExtraCode.SING_PASS_USER_INFO, it.memCd)
                        putExtra(ExtraCode.SING_PASS_GENRE_INFO, it.genreCd)
                        putExtra(ExtraCode.SING_PASS_SEASON_INFO, viewModel.singPassDataList.value!!.seasonInfo!!.svcSeaMngCd)
                    }
                } else {
                    requireActivity().moveToLoginAct()
                }
            }

            requestDetailUserInfo.observe(viewLifecycleOwner) {
                if (loginManager.isLogin()) {
                    requireActivity().startAct<SingPassUserInfoActivity>(
                        enterAni = R.anim.slide_up,
                        exitAni = R.anim.slide_down
                    ) {
                        putExtra(ExtraCode.SING_PASS_USER_INFO, it.memCd)
                        putExtra(ExtraCode.SING_PASS_GENRE_INFO, it.genreCd)
                        putExtra(ExtraCode.SING_PASS_SEASON_INFO, viewModel.singPassDataList.value!!.seasonInfo!!.svcSeaMngCd)
                    }
                } else {
                    requireActivity().moveToLoginAct()
                }
            }

            requestMoreRanking.observe(viewLifecycleOwner) {
                if (loginManager.isLogin()) {
                    requireActivity().startAct<SingPassRankingListActivity>(
                    ) {
                        putExtra(ExtraCode.SING_PASS_GENRE_INFO, it)
                        putExtra(ExtraCode.SING_PASS_SEASON_INFO, viewModel.singPassDataList.value!!.seasonInfo!!.svcSeaMngCd)
                    }
                } else {
                    requireActivity().moveToLoginAct()
                }
            }

            requestSingPassDashBoard.observe(viewLifecycleOwner) {
                if (loginManager.isLogin()) {
                    requireActivity().startAct<SingPassDashBoardActivity>(
                        enterAni = R.anim.slide_up,
                        exitAni = R.anim.slide_down
                    ) {
                        putExtra(ExtraCode.SING_PASS_USER_INFO, loginManager.getUserLoginData().memCd)
                    }
                }
            }

            requestSingMain.observe(viewLifecycleOwner) {
                requireActivity().startAct<SongMainActivity>()
            }

            requestShowMembershipPopup.observe(viewLifecycleOwner) {
                if (loginManager.isLogin()) {
                    CommonDialog(requireActivity())
                        .setIcon("")
                        .setContents(resources.getString(R.string.singing_popup_vip))
                        .setPositiveButton(R.string.membership_title)
                        .setNegativeButton(getString(R.string.str_confirm))
                        .setListener(
                            object : CommonDialog.Listener {
                                override fun onClick(which: Int) {
                                    if (which == CommonDialog.POSITIVE) {
                                        requireActivity().startAct<MemberShipActivity>()
                                    }
                                }
                            })
                        .show()
                } else {
                    requireActivity().moveToLoginAct()
                }
            }

            lifecycle.addObserver(exoManager)
            activityViewModel.setEnableMainViewpager(false)
            activityViewModel.setEnableRefresh(false)
        }
    }

    override fun onResume() {
        viewModel.onLoadingDismiss()
        viewModel.deviceProvider.setDeviceStatusBarColor(context.getActivity()!!.window, R.color.black)
        super.onResume()
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.onLoadingDismiss()
    }

    override fun onDestroy() {
        viewModel.onLoadingDismiss()
        setFragmentResult(ExtraCode.FRAGMENT_RESULT, bundleOf(ExtraCode.FRAGMENT_RESULT to true))
        super.onDestroy()
    }
}