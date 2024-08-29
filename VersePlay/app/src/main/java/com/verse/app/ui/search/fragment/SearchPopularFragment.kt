package com.verse.app.ui.search.fragment

import android.os.Bundle
import android.view.View
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.PagingData
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentSearchPopularBinding
import com.verse.app.model.enums.SortDialogType
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.sort.SortCacheEntity
import com.verse.app.ui.search.activity.SearchResultActivity
import com.verse.app.ui.search.viewmodel.SearchResultFragmentViewModel
import com.verse.app.ui.search.viewmodel.SearchResultPopularViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchPopularFragment :
    BaseFragment<FragmentSearchPopularBinding, SearchResultPopularViewModel>() {
    override val layoutId: Int = R.layout.fragment_search_popular
    override val viewModel: SearchResultPopularViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    private val parentViewModel: SearchResultFragmentViewModel by viewModels({ requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {

            startMoveTabEvent.observe(viewLifecycleOwner) {
                parentViewModel.moveToTab(it)
            }

            // 인기 탭 관련 게시물 피드 상세 페이지 이동 처리
            startFeedDetailEvent.observe(viewLifecycleOwner) {
                handleFeedDetail(it)
            }

            start()
        }
    }

    fun onSortConfirm(cacheEntity: SortCacheEntity) {
        viewModel.setSort(cacheEntity)
    }

    fun getSortInfo(): Pair<SortDialogType, SortCacheEntity> {
        return viewModel.getSortInfo()
    }

    /**
     * 피드 상세로 넘어가기 위한 처리 함수
     */
    private fun handleFeedDetail(triple: Triple<Int, String,PagingData<FeedContentsData>>) {
        val activity = requireActivity()
        if (activity is SearchResultActivity) {
            activity.moveToFeedDetail(triple)
        }
    }

    companion object {
        fun newInstance(bundle: Bundle?): Fragment {
            return SearchPopularFragment().apply {
                arguments = bundle
            }
        }
    }
}