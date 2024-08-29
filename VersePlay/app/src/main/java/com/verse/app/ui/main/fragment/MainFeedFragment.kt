package com.verse.app.ui.main.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.contants.AppData
import com.verse.app.contants.ExoPageType
import com.verse.app.databinding.FragmentMainFeedBinding
import com.verse.app.extension.onMain
import com.verse.app.extension.viewPagerNotifyPayload
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.mypage.RecommendUserData
import com.verse.app.ui.adapter.CommonListAdapter
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.main.viewmodel.FeedViewModel
import com.verse.app.ui.main.viewmodel.MainViewModel
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import kotlinx.coroutines.delay

/**
 * Description : Feed Fragment
 *
 * Created by jhlee on 2023-05-20
 */
@AndroidEntryPoint
class MainFeedFragment : BaseFragment<FragmentMainFeedBinding, FeedViewModel>() {

    override val layoutId: Int = R.layout.fragment_main_feed
    override val viewModel: FeedViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel
    private val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }
    private val activityViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {

            //VP FEED
            vpState.observe(viewLifecycleOwner) { state ->

                when (state) {
                    ViewPager2.SCROLL_STATE_SETTLING -> {
                        onExoPause()
                        checkPaging()
                        checkLastView(false)
                    }

                    ViewPager2.SCROLL_STATE_IDLE -> {
                        onExoPlay()
                        checkLastView(true)
                    }
                }
            }

            //페이지 전환 후 다시 처음부터 플레이
            RxBus.listen(RxBusEvent.MainTabSwipeEvent::class.java).subscribe({
                if (it.type == _exoPageType.value) {
                    if (it.onPlay) {
                        onExoRePlay()
                    }
                }
            }, {
                //error
            }).addTo(compositeDisposable)

            refreshPosition.observe(viewLifecycleOwner) {
                binding.inUserFeed.inRecomUser.vpFollowing.viewPagerNotifyPayload(it.first, null)
            }

            loadFinished.observe(viewLifecycleOwner) {
                onMain { delay(500)

                    if (_exoPageType.value == ExoPageType.MAIN_FOLLOWING) {
                        if(!recommendUser.value.isNullOrEmpty()){
                            activityViewModel.setEnableMainViewpager(false)
                        }
                    }
                    activityViewModel.setMainLoading(false)
                }
            }

            startOneButtonPopup.observe(viewLifecycleOwner) {
                CommonDialog(requireContext())
                    .setContents(it)
                    .setIcon(AppData.POPUP_WARNING)
                    .setCancelable(true)
                    .setPositiveButton(R.string.str_confirm)
                    .show()
            }

            //갱신
            RxBus.listen(RxBusEvent.FeedRefreshEvent::class.java).subscribe {
                DLogger.d("FeedRefreshEvent MAIN 갱신")
//                if (it.deleteRefreshFeedList == DeleteRefreshFeedList.MAIN) {
                it.feedContentData?.let { refreshData ->
                    if (_feedList.value.isNotEmpty()) {
                        _feedList.value.forEachIndexed { index, data ->
                            if (data.feedMngCd == refreshData.feedMngCd) {
                                _feedList.value[index] = refreshData
                                refreshEvent(index, _feedList.value[index])
                            }
                        }
                    }
                }
//                }
            }.addTo(compositeDisposable)

            RxBus.listen(RxBusEvent.FeedCommentRefreshEvent::class.java).subscribe {
                DLogger.d("FeedCommentRefreshEvent MAIN 갱신")
//                if (it.deleteRefreshFeedList == DeleteRefreshFeedList.MAIN) {
                if (it.feedMngCd.isEmpty()) return@subscribe

                if (_feedList.value.isNotEmpty()) {
                    _feedList.value.forEachIndexed { index, data ->
                        if (data.feedMngCd == it.feedMngCd) {
                            _feedList.value[index].replyCount = it.commentCount
                            refreshEvent(index, _feedList.value[index])
                        }
                    }
                }

            }.addTo(compositeDisposable)


            startAgainFollowingFeed.observe(viewLifecycleOwner) {
                refresh(0)
            }

            //Toast
            showToastStringMsg.observe(viewLifecycleOwner) {
                Toast.makeText(requireActivity(), it, Toast.LENGTH_SHORT).show()
            }

            showToastIntMsg.observe(viewLifecycleOwner) {
                Toast.makeText(requireActivity(), it, Toast.LENGTH_SHORT).show()
            }

            //다음 피드 이동
            moveToNextFeed.observe(viewLifecycleOwner) {
                binding.inRecomFeed.vpFeed.adapter?.let {
                    val nextPos = vpCurPosition.value + 1
                    if (it.itemCount > nextPos) {
                        DLogger.d("다음 피드 이동-> 총 피드: ${it.itemCount} /현재 피드: ${vpCurPosition.value} /다음 피드: ${nextPos} / ${exoPageType.value}")
                        viewModel.setCurPosition(nextPos)
                    }else{
                        DLogger.d("마지막 피드-> 총 피드: ${it.itemCount} /현재 피드: ${vpCurPosition.value} /다음 피드: ${nextPos} / ${exoPageType.value}")
                    }
                }
            }

            //getData
            start()

            lifecycle.addObserver(exoManager)
        }
    }

    private fun refreshEvent(pos: Int, feedContentsData: FeedContentsData) {
        DLogger.d("FeedCommentRefreshEvent refreshEvent ${pos}")
        with(viewModel) {
            if (_exoPageType.value == ExoPageType.MAIN_RECOMMEND) {
                binding.inRecomFeed.vpFeed.viewPagerNotifyPayload(pos, null)
            } else {
                binding.inUserFeed.inFollowFeed.vpFeed.viewPagerNotifyPayload(pos, null)
            }
        }
    }

    fun onResumePlayer() {
        viewModel.onExoPlay()
    }

    /**
     * 갱신처리
     */
    fun refresh(pos: Int) {

        with(viewModel) {

            if (pos == 0 && viewModel.exoPageType.value == ExoPageType.MAIN_FOLLOWING) {

                resetFollowFeed()

                binding.inUserFeed.inRecomUser.vpFollowing.adapter?.let {
                    val adapter = it as CommonListAdapter<RecommendUserData>
                    adapter.submitList(null)
                }

                binding.inUserFeed.inFollowFeed.vpFeed.adapter?.let {
                    val adapter = it as CommonListAdapter<FeedContentsData>
                    adapter.submitList(null)
                }

            } else {

                resetRecommend()

                binding.inRecomFeed.vpFeed.adapter?.let {
                    val adapter = it as CommonListAdapter<FeedContentsData>
                    adapter.submitList(null)
                }
            }

            onRefresh()
        }
    }

    override fun onResume() {
        DLogger.d("### MainFeedFragment onResume ${viewModel._exoPageType.value}")
        super.onResume()
    }

    override fun onPause() {
        DLogger.d("### MainFeedFragment onPause")
        super.onPause()
    }

    override fun onDestroyView() {
        DLogger.d("### MainFeedFragment onDestroyView")
        if (viewModel.exoPageType.value == ExoPageType.MAIN_FOLLOWING) {
            binding.inUserFeed.inRecomUser.vpFollowing?.adapter = null
            binding.inUserFeed.inFollowFeed.vpFeed?.adapter = null
        }else{
            binding.inRecomFeed?.vpFeed?.adapter = null
        }
        super.onDestroyView()
    }

    override fun onDestroy() {
        DLogger.d("### RecommendFragment onDestroy")
//        viewModel.accountPref.setJWTToken(viewModel.accountPref.getJWTToken())
//        viewModel.accountPref.setAuthTypeCd(viewModel.accountPref.getAuthTypeCd())
//        DLogger.d("### viewModel.accountPref.getJWTToken() ${viewModel.accountPref.getJWTToken()}")
//        DLogger.d("### viewModel.accountPref.getAuthTypeCd() ${viewModel.accountPref.getAuthTypeCd()}")

        if (!compositeDisposable.isDisposed) compositeDisposable.dispose()
        super.onDestroy()
    }

}