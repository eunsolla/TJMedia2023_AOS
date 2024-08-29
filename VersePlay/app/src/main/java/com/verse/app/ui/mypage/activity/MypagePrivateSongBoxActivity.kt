package com.verse.app.ui.mypage.activity

import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.view.get
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.contants.DeleteRefreshFeedList
import com.verse.app.contants.SortType
import com.verse.app.databinding.ActivityMypagePrivateBoxBinding
import com.verse.app.extension.getFragment
import com.verse.app.extension.initFragment
import com.verse.app.extension.onMain
import com.verse.app.extension.replaceFragment
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.ui.adapter.CommonPagingAdapter
import com.verse.app.ui.dialogfragment.FilterDialogFragment
import com.verse.app.ui.feed.fragment.FeedDetailFragment
import com.verse.app.ui.mypage.fragment.MypagePrivateFragment
import com.verse.app.ui.mypage.viewmodel.MypagePrivateViewModel
import com.verse.app.utility.RxBus
import com.verse.app.utility.RxBusEvent
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Description :  비공개 컨텐츠 Activity
 *
 * Created by jhlee on 2023-06-08
 */
@AndroidEntryPoint
class MypagePrivateSongBoxActivity : BaseActivity<ActivityMypagePrivateBoxBinding, MypagePrivateViewModel>() {

    override val layoutId = R.layout.activity_mypage_private_box
    override val viewModel: MypagePrivateViewModel by viewModels()
    override val bindingVariable = BR.viewModel
    private lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        with(viewModel) {

            startFinish.observe(this@MypagePrivateSongBoxActivity) {
                finish()
            }

            startFilter.observe(this@MypagePrivateSongBoxActivity) {

                FilterDialogFragment()
                    .setBtnOneName(getString(R.string.filter_order_by_latest), (it.name == SortType.DESC.name) || (it.name == SortType.NONE.name))
                    .setBtnTwoName(getString(R.string.filter_order_by_old), it.name == SortType.ASC.name)
                    .setListener(object : FilterDialogFragment.Listener {
                        override fun onClick(which: Int) {
                            val newType = if (which == 1) {
                                SortType.DESC
                            } else {
                                SortType.ASC
                            }
                            if (newType != it) {
                                setFilterType(newType)
                            }
                        }
                    })
                    .show(supportFragmentManager, null)
            }

            startFeedDetailEvent.observe(this@MypagePrivateSongBoxActivity) {

                val fragment = FeedDetailFragment()
                    .setTargetPos(it.first)
                    .setTargetFeedMngCd(it.second)
                    .setPagingData(it.third)
                replaceFragment(
                    containerId = binding.flPrivateRoot.id,
                    fragment = fragment,
                    tagName = "FeedDetailFragment",
                    enterAni = R.anim.in_right_to_left_short,
                    exitAni = R.anim.out_right_to_left_short,
                    popEnterAni = R.anim.out_left_to_right_short,
                    popExitAni = R.anim.in_left_to_right_short
                )

//                val fragment = FeedDetailFragment()
//                    .setTargetPos(it.first)
//                    .setPagingData(it.second)
//                addFragment(
//                    containerId = binding.flPrivateRoot.id,
//                    fragment = fragment,
//                    enterAni = R.anim.in_right_to_left_short,
//                    exitAni = R.anim.out_right_to_left_short,
//                    popEnterAni = R.anim.out_left_to_right_short,
//                    popExitAni = R.anim.in_left_to_right_short
//                )
            }

            start()

            replaceFragment(binding.flPrivateRoot.id, initFragment<MypagePrivateFragment>())
        }
    }

    /**
     * addFragment 사용시.. 일단 주석
     * FeedDetailFragment에서 처리
     */
    private fun setRefreshBus() {

        disposable = RxBus.listen(RxBusEvent.FeedRefreshEvent::class.java).subscribe {

            if (it.deleteRefreshFeedList == DeleteRefreshFeedList.DETAIL) {

                supportFragmentManager.getFragment<MypagePrivateFragment>()?.let {

                    (it.binding.vpPrivate[0] as RecyclerView)?.let { rv ->

                        ((rv[0] as ViewGroup).getChildAt(0) as RecyclerView).adapter?.let { adapter ->

                            if (adapter is ConcatAdapter) {
                                adapter.adapters?.let { conAd ->
                                    (conAd[0] as CommonPagingAdapter<FeedContentsData>)?.let {
                                        onMain {
                                            it.refresh()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}