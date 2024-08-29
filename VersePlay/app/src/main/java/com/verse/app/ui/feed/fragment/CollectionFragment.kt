package com.verse.app.ui.feed.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.activityViewModels
import androidx.paging.PagingData
import androidx.recyclerview.widget.ConcatAdapter
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentCollectionBinding
import com.verse.app.model.enums.SortEnum
import com.verse.app.model.feed.FeedContentsData
import com.verse.app.ui.adapter.CommonPagingAdapter
import com.verse.app.ui.dialogfragment.FilterDialogFragment
import com.verse.app.ui.feed.viewmodel.CollectionFeedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Description : Feed Fragment
 *
 * Created by jhlee on 2023-05-20
 */
@AndroidEntryPoint
class CollectionFragment : BaseFragment<FragmentCollectionBinding, CollectionFeedViewModel>() {

    override val layoutId: Int = R.layout.fragment_collection
    override val viewModel: CollectionFeedViewModel by activityViewModels()
    override val bindingVariable: Int = BR.viewModel

    private lateinit var callback: OnBackPressedCallback
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            updateFavorite.observe(viewLifecycleOwner) {
                updateFavorite(it)
            }

            //positon 이동 (vp와 맞춤)
            vpCurPosition.observe(viewLifecycleOwner) {
                binding.recyclerView.layoutManager?.scrollToPosition(it)
            }


            showFilter.observe(viewLifecycleOwner) {
                //필터
                FilterDialogFragment()
                    .setBtnOneName(getString(R.string.filter_order_by_latest), currentSortType.value == SortEnum.DESC.query)
                    .setBtnTwoName(getString(R.string.filter_order_by_old), currentSortType.value == SortEnum.ASC.query)
                    .setListener(object : FilterDialogFragment.Listener {
                        override fun onClick(which: Int) {
                            if (which == 1) {
                                currentSortType.value = SortEnum.DESC.query
                            } else if (which == 2) {
                                currentSortType.value = SortEnum.ASC.query
                            }
                            binding.recyclerView.adapter?.let { adapter ->
                                if (adapter is ConcatAdapter) {
                                    adapter?.let {
                                        val commonPagingAdapter = it.adapters[0] as CommonPagingAdapter<FeedContentsData>
                                        commonPagingAdapter?.let { commonAdapter ->
                                            CoroutineScope(Dispatchers.Main).launch {
                                                commonAdapter.submitData(lifecycle, PagingData.empty())
                                                binding.recyclerView.adapter = null
                                                binding.recyclerView.removeAllViews()
                                                requestCollectionList()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }).show(parentFragmentManager, null)
            }
        }
    }

    private fun updateFavorite(state: Boolean) {
        if (state) {
            binding.btnFavorite.setImageResource(R.drawable.ic_favorite_on)
        } else {
            binding.btnFavorite.setImageResource(R.drawable.ic_favorite_off)
        }
    }

    override fun onDetach() {
        callback.remove()
        super.onDetach()
    }
}