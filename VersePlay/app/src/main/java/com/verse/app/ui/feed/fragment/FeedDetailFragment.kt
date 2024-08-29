package com.verse.app.ui.feed.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.FragmentFeedDetailBinding
import com.verse.app.extension.onIO
import com.verse.app.extension.onWithContextMain
import com.verse.app.extension.popBackStackParentFragment
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.ui.adapter.CommonPagingAdapter
import com.verse.app.ui.feed.activity.CollectionFeedActivity
import com.verse.app.ui.feed.activity.FeedDetailActivity
import com.verse.app.ui.feed.viewmodel.FeedDetailFragmentViewModel
import com.verse.app.ui.main.fragment.MainFragment
import com.verse.app.ui.main.fragment.MainRightFragment
import com.verse.app.ui.mypage.activity.MyPageRootActivity
import com.verse.app.ui.search.activity.SearchResultActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo

/**
 * Description : Feed Fragment
 *
 * Created by jhlee on 2023-05-20
 */
@AndroidEntryPoint
class FeedDetailFragment : BaseFragment<FragmentFeedDetailBinding, FeedDetailFragmentViewModel>() {

    override val layoutId: Int = R.layout.fragment_feed_detail
    override val viewModel: FeedDetailFragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel
    private lateinit var callback: OnBackPressedCallback
    val disposable: CompositeDisposable by lazy { CompositeDisposable() }

    private var targetPos: Int = 0
    private var targetMngCd: String = ""
    private var pagingData: PagingData<FeedContentsData>? = null
    private var listener: Listener? = null

    interface Listener {
        fun onCurPosition(pos: Int)
    }


    fun setTargetPos(pos: Int): FeedDetailFragment {
        targetPos = pos
        return this
    }

    fun setTargetFeedMngCd(mngCd: String): FeedDetailFragment {
        targetMngCd = mngCd
        return this
    }

    fun setListener(listener: Listener): FeedDetailFragment {
        this.listener = listener
        return this
    }

    fun setPagingData(pagingData: PagingData<FeedContentsData>): FeedDetailFragment {
        this.pagingData = pagingData
        return this
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                DLogger.d("~~~~~~~ FeedDetailFragment backPressedCallback ")
                handleBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {

            //VP FEED
            vpState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    ViewPager2.SCROLL_STATE_SETTLING -> {
                        onExoPause()
                    }
                    ViewPager2.SCROLL_STATE_IDLE -> {
                        DLogger.d("FeedDetail SCROLL_STATE_IDLE -> ${vpCurPosition.value}")
                        onExoPlay()
                        listener?.onCurPosition(vpCurPosition.value)
                    }
                }
            }

            startBackEvent.observe(viewLifecycleOwner) {
                callback.handleOnBackPressed()
            }

            //다음 피드 이동
            moveToNextFeed.observe(viewLifecycleOwner) {
                binding.vpFeed.adapter?.let {
                    val nextPos = vpCurPosition.value + 1
                    if (it.itemCount > nextPos) {
                        DLogger.d("다음 피드 이동-> 총 피드: ${it.itemCount} /현재 피드: ${vpCurPosition.value} /다음 피드: ${nextPos} / ${exoPageType.value}")
                        viewModel.setCurPosition(nextPos)
                    } else {
                        DLogger.d("마지막 피드-> 총 피드: ${it.itemCount} /현재 피드: ${vpCurPosition.value} /다음 피드: ${nextPos} / ${exoPageType.value}")
                    }
                }
            }

            if (pagingData == null) {
                callback.handleOnBackPressed()
            } else {
                start(targetPos, targetMngCd, pagingData!!)
            }

            lifecycle.addObserver(exoManager)
        }

        initListenRefresh()
    }


    private fun initListenRefresh() {
        RxBus.listen(RxBusEvent.FeedRefreshEvent::class.java).subscribe {
            DLogger.d("FeedDetail setListenRefresh")
            (binding.vpFeed[0] as RecyclerView).adapter?.let { adapter ->
                if (adapter is CommonPagingAdapter<*>) {
                    onIO {
                        if (it.feedContentData.isDeleted) {
                            onWithContextMain {
                                viewModel.togglePlayAndPause()
                            }
                        }
                        if (requireActivity() !is FeedDetailActivity) {
                            DLogger.d("FeedDetail 갱신 ${requireActivity()}")
                            adapter.refresh()
                        }
                    }
                }
            }
        }.addTo(disposable)
    }

    private fun handleBack() {
        val parentFragment = parentFragment

        parentFragment?.let {
            DLogger.d("피드 디테일 이동입니다. $parentFragment")
            if (parentFragment is MainRightFragment) {
                parentFragment.childFragmentManager.popBackStack()
            } else if (parentFragment is MainFragment) {
                parentFragment.childFragmentManager.popBackStack()
            } else {
                popBackStackParentFragment()
            }
        } ?: run {

            if (requireActivity() is CollectionFeedActivity || requireActivity() is MyPageRootActivity || requireActivity() is SearchResultActivity) {
                popBackStackParentFragment()
            } else {
                requireActivity().finish()
            }
        }

    }

    override fun onDetach() {
        super.onDetach()
        setFragmentResult(ExtraCode.FRAGMENT_RESULT, bundleOf(ExtraCode.FRAGMENT_RESULT_DETAIL_CALL_BACK to false))
    }

    override fun onDestroyView() {
        binding.vpFeed.adapter = null
        super.onDestroyView()
    }
    override fun onDestroy() {
        callback.remove()
        if (disposable != null && !disposable.isDisposed) disposable.dispose()
        super.onDestroy()
    }
}