package com.verse.app.ui.main.fragment

import android.os.Bundle
import android.view.View
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.contants.ExoPageType
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.NaviType
import com.verse.app.databinding.FragmentMainBinding
import com.verse.app.extension.addChildFragment
import com.verse.app.extension.getActivity
import com.verse.app.extension.initFragment
import com.verse.app.extension.replaceChildFragment
import com.verse.app.extension.setChildFragmentResultListener
import com.verse.app.model.mypage.MyPageIntentModel
import com.verse.app.ui.community.CommunityFragment
import com.verse.app.ui.dialogfragment.SingDialogFragment
import com.verse.app.ui.feed.fragment.FeedDetailFragment
import com.verse.app.ui.main.viewmodel.MainViewModel
import com.verse.app.ui.mypage.my.MyPageFragment
import com.verse.app.ui.singpass.fragment.SingPassFragment
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : MainFragment
 *
 * Created by jhlee on 2023-01-31
 */
@AndroidEntryPoint
class MainFragment : BaseFragment<FragmentMainBinding, MainViewModel>() {

    override val layoutId: Int = R.layout.fragment_main
    override val viewModel: MainViewModel by activityViewModels()
    override val bindingVariable: Int = BR.viewModel
    private lateinit var mainTabFragment: MainTabFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.deviceProvider.setDeviceStatusBarColor(
            context.getActivity()!!.window,
            R.color.black
        )

        with(viewModel) {
            /**
             * 하위 Fragment -> destroy 시  호출
             * 탭 포지션에 따라 재생처리
             * FRAGMENT_RESULT_CALL_BACK true : 처음부터, false :이어서 재생
             * 씽패스를 제외한 Fragment들은 onResumePlayer 처리
             */
            setChildFragmentResultListener(ExtraCode.FRAGMENT_RESULT) { requestKey, bundle ->
                //singpass에서 메인이 아닌 타 페이지 이동시 리턴

                if (curNaviPage.value?.type != NaviType.MAIN) {
                    return@setChildFragmentResultListener
                }

                val result = bundle.getBoolean(ExtraCode.FRAGMENT_RESULT_CALL_BACK)

                if (result) {
                    if (mainTabFragment.getTabPosition() == 0) {
                        RxBus.publish(
                            RxBusEvent.MainTabSwipeEvent(
                                type = ExoPageType.MAIN_FOLLOWING,
                                onPlay = true
                            )
                        )
                    } else {
                        RxBus.publish(
                            RxBusEvent.MainTabSwipeEvent(
                                type = ExoPageType.MAIN_RECOMMEND,
                                onPlay = true
                            )
                        )
                    }
                } else {
                    mainTabFragment.onResumePlayer()
                }
            }

            /**
             * 하단 네비 동작
             */
            curNaviPage.observe(viewLifecycleOwner) {


                when (it.type) {
                    NaviType.NONE,
                    NaviType.MAIN -> {
                        viewModel.onLoadingDismiss()
                        exoPlayerLock(false)
                        setEnableMainViewpager(true)
                        setEnableRefresh(true)

                        if (it.isRefresh) {
                            setMainLoading(true)
                            createDefaultFragment()
                        } else {
                            if (prevNaviPage.value == it.type || prevNaviPage.value == null) {
                                refreshTab()
                            } else {
                                popChildFragment()
                                //재생
                                mainTabFragment.onResumePlayer()
                            }
                        }
                    }

                    NaviType.SING_PASS -> {
                        onLoadingDismiss()
                        exoPlayerLock(true)
                        addChildFragment(initFragment<SingPassFragment>(), NaviType.SING_PASS.name)
                    }

                    NaviType.SING -> {

                        onLoadingDismiss()

                        if(isExoPlaying.value){
                            exoPlayerLock(true)
                        }
                        setShowSingDialog(true)
                        SingDialogFragment()
                            .setListener(object : SingDialogFragment.Listener{
                                override fun onDismiss() {
                                    if(isExoPlaying.value){
                                        exoPlayerLock(false)
                                        exoProvider.onResume()
                                        setShowSingDialog(false)
                                    }
                                }
                                override fun onMoveToSing() {
                                    exoPlayerLock(false)
                                    setShowSingDialog(false)
                                }
                            })
                            .show(childFragmentManager)
                    }

                    NaviType.COMMUNITY -> {
                        onLoadingDismiss()
                        exoPlayerLock(true)
                        addChildFragment(initFragment<CommunityFragment>() {
                            putSerializable(
                                ExtraCode.COMMUNITY_ENTER_TYPE,
                                it.communityEnterTabType
                            )
                        }, NaviType.COMMUNITY.name)
                    }

                    NaviType.MY -> {
                        viewModel.onLoadingDismiss()
                        exoPlayerLock(true)
                        setEnableMainViewpager(false)
                        setEnableRefresh(false)
                        addChildFragment(initFragment<MyPageFragment> {
                            putParcelable(
                                ExtraCode.MY_PAGE_DATA,
                                MyPageIntentModel(loginManager.getUserLoginData().memCd)
                            )
                        }, NaviType.MY.name)
                    }
                }

                if (it.type != NaviType.SING) {
                    setPrevNaviPage(it.type)
                    binding.bottomNaviView.setSelectPage(it.type)
                }
            }


            //상세 이동
            startFeedDetailEvent.observe(viewLifecycleOwner) {
                val fragment = FeedDetailFragment()
                    .setTargetPos(it.first)
                    .setTargetFeedMngCd(it.second)
                    .setPagingData(it.third)
                addChildFragment(
                    binding.flRoot.id,
                    fragment, "FeedDetailFragment",
                    enterAni = R.anim.in_right_to_left_short,
                    exitAni = R.anim.out_right_to_left_short,
                    popEnterAni = R.anim.out_left_to_right_short,
                    popExitAni = R.anim.in_left_to_right_short
                )
            }

            requireActivity().lifecycle.addObserver(binding.bottomNaviView)
            startInitNaviListen()
        }
    }

    /**
     * FirstFragment
     */
    fun createDefaultFragment() {
        DLogger.d("CREATE MAIN DEFAULT FRAGMENT")
        if (binding.mainContainerFrameLayout.childCount > 0) {
            binding.mainContainerFrameLayout.removeAllViews()
        }

        binding.bottomNaviView.setSelectPage(NaviType.MAIN)
        viewModel.setEnableMainViewpager(true)
        viewModel.exoPlayerLock(false)
        mainTabFragment = initFragment()
        replaceChildFragment(
            binding.mainContainerFrameLayout.id,
            mainTabFragment,
            NaviType.MAIN.name
        )
    }

    /**
     * 메인 하위 프래그먼트 Show
     * 씽패스,커뮤니티,마이
     */
    private fun addChildFragment(fragment: Fragment, tagName: String) {
        popChildFragment()
        addChildFragment(binding.mainContainerFrameLayout.id, fragment, tagName)
    }

    /**
     * 메인 하위 프래그먼트 제거
     * 씽패스,커뮤니티,마이
     */
    private fun popChildFragment() {
        childFragmentManager.popBackStack()
//        childFragmentManager.fragments?.let {
//            if (it.size > 1) {
//                DLogger.d("Target Pop Fragment==>  ${it.last().javaClass.simpleName} / ${it.last().javaClass.simpleName}")
//                it.last().popBackStackParentFragment()
//            }
//        }
    }

    /**
     * 메인 -> 현재 탭  갱신
     */
    fun refreshTab() {
        if (mainTabFragment != null || ::mainTabFragment.isInitialized) {
            viewModel.setMainLoading(true)
            mainTabFragment.refreshTab()
        } else {
            DLogger.d("mainTab is null")
        }
    }

    override fun onPause() {
        viewModel.onLoadingDismiss()
        super.onPause()
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.onLoadingDismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onLoadingDismiss()
    }
}
