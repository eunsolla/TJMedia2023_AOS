package com.verse.app.ui.main.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.paging.PagingData
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.FragmentMainRightBinding
import com.verse.app.extension.applyApiScheduler
import com.verse.app.extension.setChildFragmentResultListener
import com.verse.app.model.feed.CurrentFeedData
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.mypage.MyPageIntentModel
import com.verse.app.ui.feed.fragment.FeedDetailFragment
import com.verse.app.ui.main.viewmodel.MainViewModel
import com.verse.app.ui.mypage.my.MyPageFragment
import com.verse.app.ui.mypage.user.UserPageFragment
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

/**
 * Description : 메인 -> 오른쪽 스와이프 시 보이는 유저 마이페이지
 *
 * Created by esna on 2023-05-17
 */
@AndroidEntryPoint
class MainRightFragment : BaseFragment<FragmentMainRightBinding, FragmentViewModel>() {
    override val layoutId: Int = R.layout.fragment_main_right
    override val viewModel: FragmentViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()
    override val bindingVariable: Int = BR.viewModel
    private var disposable: Disposable? = null
    private lateinit var callback: OnBackPressedCallback

    private var tempMemCd: String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveToMainFeed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    @Inject
    lateinit var loginManager: LoginManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeDisposable()
        disposable = RxBus.listen(RxBusEvent.VideoUserInfoEvent::class.java)
            .filter { tempMemCd != it.currentFeedData.ownerMemCd }
            .applyApiScheduler()
            .doOnNext { handleMyPage(it.currentFeedData) }.subscribe()
    }

    private fun handleMyPage(data: CurrentFeedData) {
        if (tempMemCd != data.ownerMemCd) {
            replaceMyFragment(data)
            tempMemCd = data.ownerMemCd
        }
    }

    override fun onDestroyView() {
        closeDisposable()
        super.onDestroyView()
    }

    /**
     * Default
     */
    private fun replaceMyFragment(data: CurrentFeedData) {
        val bundle = bundleOf(ExtraCode.MY_PAGE_DATA to MyPageIntentModel(data))
        val fragment = if (loginManager.getUserLoginData().memCd == data.ownerMemCd) {
            MyPageFragment.newInstance(bundle)
        } else {
            UserPageFragment.newInstance(bundle)
        }

        childFragmentManager.beginTransaction().apply {
            replace(R.id.mainRightContainer, fragment)
            commit()
        }
    }

    /**
     * 마이페이지 API 호출하는 함수
     */
    fun requestMyPage() {
        val fragments = childFragmentManager.fragments
        if (fragments.isEmpty()) return
        for (idx in 0..fragments.size) {
            val fragment = fragments[idx]
            if (fragment is MyPageFragment) {
                fragment.handleMainRightApiCall()
                break
            }
            if (fragment is UserPageFragment) {
                fragment.handleMainRightApiCall()
                break
            }
        }
    }


    /**
     * 피드 상세 이동하는 함수
     * @param index 초기 이동할 위치값
     * @param dataList 피드 페이징 데이터
     */
    fun moveToFeedDetail(index: Int,mngCd:String, dataList: PagingData<FeedContentsData>) {

        //피드 상세 종료 시 메인 vp 활성화
        setChildFragmentResultListener(ExtraCode.FRAGMENT_RESULT) { requestKey, bundle ->
            activityViewModel.setEnableMainViewpager(true)
        }

        val fragment = FeedDetailFragment()
            .setTargetPos(index)
            .setTargetFeedMngCd(mngCd)
            .setPagingData(dataList)
        childFragmentManager.beginTransaction().apply {
            setCustomAnimations(
                R.anim.in_right_to_left_short,
                R.anim.out_right_to_left_short,
                R.anim.out_left_to_right_short,
                R.anim.in_left_to_right_short
            )
            add(R.id.mainRightContainer, fragment)
            addToBackStack("FeedDetailFragment")
            commit()
        }

        //피드 상세 이동 시 메인 vp 비활성화
        activityViewModel.setEnableMainViewpager(false)
    }

    fun moveToMainFeed() {
        activityViewModel.vpMainPosition.value = 0
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    private fun closeDisposable() {
        if (disposable != null) {
            disposable?.dispose()
            disposable = null
        }
    }
}