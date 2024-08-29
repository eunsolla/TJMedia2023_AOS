package com.verse.app.ui.song.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import androidx.paging.PagingData
import androidx.recyclerview.widget.ConcatAdapter
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.databinding.ActivityRelatedSoundSourceBinding
import com.verse.app.extension.onMain
import com.verse.app.model.enums.SortEnum
import com.verse.app.model.song.SongMainData
import com.verse.app.ui.adapter.CommonPagingAdapter
import com.verse.app.ui.dialogfragment.FilterDialogFragment
import com.verse.app.ui.song.viewmodel.RelatedSoundSourceViewModel
import com.verse.app.utility.moveToSingAct
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 연관 음원
 *
 * Created by jhlee on 2023-05-26
 */
@AndroidEntryPoint
class RelatedSoundSourceActivity : BaseActivity<ActivityRelatedSoundSourceBinding, RelatedSoundSourceViewModel>() {

    override val layoutId = R.layout.activity_related_sound_source
    override val viewModel: RelatedSoundSourceViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(viewModel) {

            _startSortDialogEvent.observe(this@RelatedSoundSourceActivity) {
                showFilter()
            }
            _startFinishEvent.observe(this@RelatedSoundSourceActivity) {
                finish()
            }

            // 파트선택 & 부르기 이동
            moveToSing.observe(this@RelatedSoundSourceActivity){
                moveToSingAct(it.first,it.second,it.third)
            }

            refresh.observe(this@RelatedSoundSourceActivity) { state ->
                binding.rvSongList.adapter?.let {
                    if (it is ConcatAdapter) {
                        it.adapters?.let { adapters ->
                            val commonPagingAdapter = adapters[0] as CommonPagingAdapter<SongMainData>
                            commonPagingAdapter?.let { commonAdapter ->
                                onMain {
                                    commonAdapter.submitData(lifecycle, PagingData.empty())
                                    binding.rvSongList.removeAllViews()
                                    fetchSongList()
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



