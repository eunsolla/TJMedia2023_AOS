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
import com.verse.app.databinding.FragmentSearchMrBinding
import com.verse.app.model.enums.SortDialogType
import com.verse.app.model.search.SearchResultSongData
import com.verse.app.model.sort.SortCacheEntity
import com.verse.app.ui.adapter.CommonPagingAdapter
import com.verse.app.ui.search.viewmodel.SearchResultMrViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchMrFragment : BaseFragment<FragmentSearchMrBinding, SearchResultMrViewModel>() {
    override val layoutId: Int = R.layout.fragment_search_mr
    override val viewModel: SearchResultMrViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            start()
        }
    }

    fun onSortConfirm(cacheEntity: SortCacheEntity) {
        binding.rv.adapter?.let {
            if (it is ConcatAdapter) {
                it.adapters?.let { adapters ->
                    if (adapters.size > 0) {
                        val commonPagingAdapter =
                            adapters[0] as CommonPagingAdapter<SearchResultSongData>
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
            return SearchMrFragment().apply {
                arguments = bundle
            }
        }
    }
}