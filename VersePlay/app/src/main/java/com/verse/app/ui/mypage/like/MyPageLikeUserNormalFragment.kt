package com.verse.app.ui.mypage.like

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.PagingData
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FMyPageUserNormalBinding
import com.verse.app.extension.dp
import com.verse.app.extension.onMain
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.ui.decoration.GridDecorator
import com.verse.app.ui.mypage.my.MyPageFragment
import com.verse.app.ui.mypage.user.UserPageFragment
import com.verse.app.utility.DLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

/**
 * Description : 마이페이지 > 좋아요 > [유저일방영상]
 *
 * Created by juhongmin on 2023/05/29
 */
@AndroidEntryPoint
class MyPageLikeUserNormalFragment :
    BaseFragment<FMyPageUserNormalBinding, MyPageLikeUserNormalFragmentViewModel>() {
    override val layoutId: Int = R.layout.f_my_page_user_normal
    override val viewModel: MyPageLikeUserNormalFragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvContents.addItemDecoration(GridDecorator(3, 1.dp, false))
        with(viewModel) {

            startFeedDetailEvent.observe(viewLifecycleOwner) {
                handleFeedDetail(it)
            }

            isRefresh.observe(viewLifecycleOwner) {
                if(it){
                    binding.rvContents.removeAllViews()
                    binding.rvContents.adapter = null
                }
            }

            start()
        }
    }

    /**
     * 피드 상세로 넘어가기 위한 처리 함수
     */
    private fun handleFeedDetail(triple: Triple<Int,String, PagingData<FeedContentsData>>) {
        val rootFragment = requireParentFragment().requireParentFragment()
        DLogger.d("RootFragment $rootFragment")
        if (rootFragment is UserPageFragment) {
            rootFragment.moveToRootFeedDetail(triple)
        } else if (rootFragment is MyPageFragment) {
            rootFragment.moveToRootFeedDetail(triple)
        }
    }

    companion object {
        fun newInstance(data: Bundle?): Fragment {
            return MyPageLikeUserNormalFragment().apply {
                arguments = data
            }
        }
    }
}
