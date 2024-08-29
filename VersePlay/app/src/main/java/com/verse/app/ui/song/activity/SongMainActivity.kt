package com.verse.app.ui.song.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.library.baseAdapters.BR
import com.verse.app.R
import com.verse.app.base.activity.BaseActivity
import com.verse.app.base.adapter.BaseFragmentPagerAdapter
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.FragmentType
import com.verse.app.databinding.ActivitySongMainBinding
import com.verse.app.extension.initFragment
import com.verse.app.extension.recyclerViewNotifyAll
import com.verse.app.extension.recyclerViewNotifyPayload
import com.verse.app.extension.startAct
import com.verse.app.model.song.SongDataList
import com.verse.app.ui.song.SongPRFragment
import com.verse.app.ui.song.viewmodel.SongMainViewModel
import com.verse.app.utility.moveToSingAct
import dagger.hilt.android.AndroidEntryPoint


/**
 * 노래 콘텐츠 업로드 -> 반주 목록
 *
 * Created by jhlee on 2023-04-05
 */
@AndroidEntryPoint
class SongMainActivity : BaseActivity<ActivitySongMainBinding, SongMainViewModel>() {

    override val layoutId = R.layout.activity_song_main
    override val viewModel: SongMainViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(viewModel) {


            //신곡/인기곡 갱신
            refresh.observe(this@SongMainActivity) {
                binding.songMainRecyclerView.recyclerViewNotifyPayload(it,null)
            }

            //더보기 (최근불렀던노래 or 인기곡 or 신곡) -> 리스트 이동
            moveToMore.observe(this@SongMainActivity){
                startAct<SongListActivity>(){
                    putExtra(ExtraCode.SONG_MORE_TYPE,it)
                }
            }

            // 파트선택 & 부르기 이동
            moveToSing.observe(this@SongMainActivity){
                moveToSingAct(it.first,it.second,it.third)
            }

            startRefresh.observe(this@SongMainActivity){
                binding.songMainRecyclerView.recyclerViewNotifyAll()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.requestSongMain()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * 인기곡/신곡
     * ViewPager Fragment
     */
    class SongFragmentPagerAdapter(ctx: Context, private val viewModel: BaseViewModel?) : BaseFragmentPagerAdapter<SongDataList>(ctx) {

        override fun onCreateFragment(pos: Int) = when (pos) {
            0 -> {
                initFragment<SongPRFragment>() {
                    this.putParcelableArrayList(ExtraCode.SONG_MAIN_ITEM, ArrayList(dataList[pos].dataList))
                }
            }
            else -> {
                initFragment<SongPRFragment>() {
                    this.putParcelableArrayList(ExtraCode.SONG_MAIN_ITEM, ArrayList(dataList[pos].dataList))
                }
            }
        }


        override fun containsItem(itemId: Long): Boolean {
            val tmpCount = itemCount.plus(FragmentType.POPULAR_RECENT.uniqueId)
            return itemId < tmpCount && itemId >= FragmentType.POPULAR_RECENT.uniqueId
        }

        override fun getItemId(pos: Int) = (FragmentType.POPULAR_RECENT.uniqueId + pos).toLong()
    }
}