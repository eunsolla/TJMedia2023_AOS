package com.verse.app.ui.song.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import androidx.paging.PagingData
import androidx.recyclerview.widget.ConcatAdapter
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.databinding.ActivitySongListBinding
import com.verse.app.extension.onMain
import com.verse.app.model.enums.SortEnum
import com.verse.app.model.song.SongMainData
import com.verse.app.ui.adapter.CommonPagingAdapter
import com.verse.app.ui.dialogfragment.FilterDialogFragment
import com.verse.app.ui.song.viewmodel.SongListViewModel
import com.verse.app.utility.moveToSingAct
import dagger.hilt.android.AndroidEntryPoint


/**
 * 노래 콘텐츠 업로드 -> 노래 목록
 *
 * Created by jhlee on 2023-04-05
 */
@AndroidEntryPoint
class SongListActivity : BaseActivity<ActivitySongListBinding, SongListViewModel>() {

    override val layoutId = R.layout.activity_song_list
    override val viewModel: SongListViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(viewModel) {


            headerInfo.observe(this@SongListActivity) {
                if (!it.dynamicText.isNullOrEmpty()) {
                    binding.headerView.setHeaderTitle(it.dynamicText)
                } else {
                    binding.headerView.setHeaderTitle(getString(it.defineText))
                }
            }

            moveToSing.observe(this@SongListActivity) {
                moveToSingAct(it.first, it.second, it.third)
            }

            refresh.observe(this@SongListActivity) { state ->
                binding.rvSongList.adapter?.let {
                    if (it is ConcatAdapter) {
                        it.adapters?.let { adapters ->
                            val commonPagingAdapter = adapters[0] as CommonPagingAdapter<SongMainData>
                            commonPagingAdapter?.let { commonAdapter ->
                                onMain {
                                    commonAdapter.submitData(lifecycle, PagingData.empty())
                                    binding.rvSongList.adapter = null
                                    requestSongList()
                                }
                            }
                        }
                    }
                }
            }
            start()
        }
    }

    /**
     * Show Filter
     */
    fun showFilter() {
        //필터
        FilterDialogFragment()
            .setBtnOneName(getString(R.string.filter_order_by_latest), viewModel.songListBody.sortType == SortEnum.DESC.query)
            .setBtnTwoName(getString(R.string.filter_order_by_old), viewModel.songListBody.sortType == SortEnum.ASC.query)
            .setListener(object : FilterDialogFragment.Listener {
                override fun onClick(which: Int) {
                    viewModel.onRefresh(which)
                }
            }).show(supportFragmentManager, null)
    }
}