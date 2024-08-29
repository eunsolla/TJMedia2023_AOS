package com.verse.app.ui.search.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FSearchResultBinding
import com.verse.app.extension.getCurrentFragment
import com.verse.app.extension.setViewPagerCache
import com.verse.app.model.enums.SortDialogType
import com.verse.app.model.enums.SortEnum
import com.verse.app.model.sort.SortCacheEntity
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.dialogfragment.SortDialogFragment
import com.verse.app.ui.search.viewmodel.SearchResultFragmentViewModel
import com.verse.app.utility.DLogger
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 검색 결과 페이지
 *
 * Created by juhongmin on 2023/06/08
 */
@AndroidEntryPoint
class SearchResultFragment : BaseFragment<FSearchResultBinding, SearchResultFragmentViewModel>() {
    override val layoutId: Int = R.layout.f_search_result
    override val viewModel: SearchResultFragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    private var adapter: PagerAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (adapter == null) {
            binding.vp.adapter = PagerAdapter()
            binding.vp.setViewPagerCache(5)
        }
        with(viewModel) {
            startFinishEvent.observe(viewLifecycleOwner) {
                requireActivity().finish()
            }

            startInvalidIntentEvent.observe(viewLifecycleOwner) {
                CommonDialog(requireContext())
                    .setContents(R.string.search_result_invalid_msg)
                    .setPositiveButton(R.string.str_confirm)
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            requireActivity().finish()
                        }
                    })
                    .show()
            }

            startSortDialogEvent.observe(viewLifecycleOwner) {
                showSortDialog()
            }

            start()
        }
    }

    override fun onDestroyView() {
        binding.vp.adapter = null
        super.onDestroyView()
    }

    private fun showSortDialog() {

        val sortInfo: Pair<SortDialogType, SortCacheEntity> =
            when (viewModel.tabPosition.value ?: 0) {
                0 -> getCurrentFragment<SearchPopularFragment>(binding.vp)?.getSortInfo()
                1 -> getCurrentFragment<SearchVideoFragment>(binding.vp)?.getSortInfo()
                2 -> getCurrentFragment<SearchMrFragment>(binding.vp)?.getSortInfo()
                3 -> getCurrentFragment<SearchTagFragment>(binding.vp)?.getSortInfo()
                4 -> getCurrentFragment<SearchUserFragment>(binding.vp)?.getSortInfo()
                else -> null
            } ?: return

        SortDialogFragment()
            .setType(sortInfo.first)
            .setCacheData(sortInfo.second)
            .setListener(object : SortDialogFragment.Listener {
                override fun onSortConfirm(selectSort: SortEnum, cacheEntity: SortCacheEntity) {
                    DLogger.d("onSortConfirm $selectSort $cacheEntity")
                    handleOnSortConfirm(cacheEntity)
                }
            })
            .simpleShow(childFragmentManager)
    }

    private fun handleOnSortConfirm(cacheEntity: SortCacheEntity) {
        when (viewModel.tabPosition.value ?: 0) {
            0 -> {
                getCurrentFragment<SearchPopularFragment>(binding.vp)
                    ?.onSortConfirm(cacheEntity)
            }

            1 -> {
                getCurrentFragment<SearchVideoFragment>(binding.vp)
                    ?.onSortConfirm(cacheEntity)
            }

            2 -> {
                getCurrentFragment<SearchMrFragment>(binding.vp)
                    ?.onSortConfirm(cacheEntity)
            }

            3 -> {
                getCurrentFragment<SearchTagFragment>(binding.vp)
                    ?.onSortConfirm(cacheEntity)
            }

            4 -> {
                getCurrentFragment<SearchUserFragment>(binding.vp)
                    ?.onSortConfirm(cacheEntity)
            }
        }
    }

    inner class PagerAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int {
            return 5
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SearchPopularFragment.newInstance(arguments)
                1 -> SearchVideoFragment.newInstance(arguments)
                2 -> SearchMrFragment.newInstance(arguments)
                3 -> SearchTagFragment.newInstance(arguments)
                else -> SearchUserFragment.newInstance(arguments)
            }
        }
    }

    companion object {
        fun newInstance(bundle: Bundle?): Fragment {
            return SearchResultFragment().apply {
                arguments = bundle
            }
        }
    }
}
