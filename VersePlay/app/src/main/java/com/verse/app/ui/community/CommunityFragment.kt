package com.verse.app.ui.community

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.LinkMenuTypeCode
import com.verse.app.databinding.FragmentCommunityBinding
import com.verse.app.extension.changeVisible
import com.verse.app.extension.getActivity
import com.verse.app.extension.openBrowser
import com.verse.app.extension.setViewPagerCache
import com.verse.app.ui.community.event.CommunityEventFragment
import com.verse.app.ui.community.lounge.CommunityLoungeFragment
import com.verse.app.ui.community.vote.CommunityVoteFragment
import com.verse.app.ui.main.viewmodel.MainViewModel
import com.verse.app.utility.moveToLinkPage
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : CommunityFragment
 *
 * Created by jhlee on 2023-01-31
 */
@AndroidEntryPoint
class CommunityFragment : BaseFragment<FragmentCommunityBinding, CommunityViewModel>() {

    override val layoutId: Int = R.layout.fragment_community
    override val viewModel: CommunityViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel
    private val adapter: PagerAdapter by lazy { PagerAdapter() }
    private val activityViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            vp.adapter = adapter
            vp.setViewPagerCache(adapter.itemCount)
            dotIndicator.viewPager = vpBanner
            abl.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
                if (appBarLayout.totalScrollRange == Math.abs(verticalOffset)) {
                    vCollapse.changeVisible(true)
                } else {
                    vCollapse.changeVisible(false)
                }
            }
        }

        with(viewModel) {
            deviceProvider.setDeviceStatusBarColor(context.getActivity()!!.window, R.color.black)

            clickBannerEvent.observe(viewLifecycleOwner) {
                var linkCd = it.bannerCode
                var linkUrl = ""

                it.url?.let {
                    linkUrl = it
                }

                if (linkCd != null) {
                    if (linkCd == LinkMenuTypeCode.LINK_URL.code) {
                        requireActivity().openBrowser(linkUrl)
                    } else {
                        requireActivity().moveToLinkPage(linkCd, linkUrl)
                    }
                }
            }

            start()
        }

        activityViewModel.setEnableMainViewpager(false)
        activityViewModel.setEnableRefresh(false)
    }

    inner class PagerAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> CommunityLoungeFragment.newInstance()
                1 -> CommunityEventFragment.newInstance()
                else -> CommunityVoteFragment.newInstance()
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.onLoadingDismiss()
        setFragmentResult(ExtraCode.FRAGMENT_RESULT, bundleOf(ExtraCode.FRAGMENT_RESULT_CALL_BACK to false))
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onLoadingDismiss()
    }

}