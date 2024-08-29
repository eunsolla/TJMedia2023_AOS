package com.verse.app.ui.search.fragment

import android.os.Bundle
import android.view.View
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.PagingData
import androidx.recyclerview.widget.ConcatAdapter
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentSearchVideoBinding
import com.verse.app.model.enums.SortDialogType
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.model.sort.SortCacheEntity
import com.verse.app.ui.adapter.CommonPagingAdapter
import com.verse.app.ui.search.activity.SearchResultActivity
import com.verse.app.ui.search.viewmodel.SearchResultVideoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchVideoFragment : BaseFragment<FragmentSearchVideoBinding, SearchResultVideoViewModel>() {
    override val layoutId: Int = R.layout.fragment_search_video
    override val viewModel: SearchResultVideoViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            startFeedDetailEvent.observe(viewLifecycleOwner) {
                handleFeedDetail(it)
            }

            start()
        }
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

    fun onSortConfirm(cacheEntity: SortCacheEntity) {
        binding.rv.adapter?.let {
            if (it is ConcatAdapter) {
                it.adapters?.let { adapters ->
                    if (adapters.size > 0) {
                        val commonPagingAdapter = adapters[0] as CommonPagingAdapter<FeedContentsData>
                        commonPagingAdapter?.let { commonAdapter ->
                            CoroutineScope(Dispatchers.Main).launch {
                                commonAdapter.submitData(lifecycle, PagingData.empty())
                                binding.rv.removeAllViews()
                                viewModel.setSort(cacheEntity)
                            }
                        }
                    }
                }
            }
        } ?: run {
            viewModel.setSort(cacheEntity)
        }
    }

    fun getSortInfo(): Pair<SortDialogType, SortCacheEntity> {
        return viewModel.getSortInfo()
    }

    companion object {
        fun newInstance(bundle: Bundle?): Fragment {
            return SearchVideoFragment().apply {
                arguments = bundle
            }
        }
    }
}