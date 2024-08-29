package com.verse.app.ui.song

import android.os.Bundle
import android.view.View
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.activityViewModels
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.FragmentSongPrBinding
import com.verse.app.extension.parcelableArrayList
import com.verse.app.model.song.SongData
import com.verse.app.ui.song.viewmodel.SongMainViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 인기곡/신곡 Fragment
 *
 * Created by jhlee on 2023-01-31
 */
@AndroidEntryPoint
class SongPRFragment : BaseFragment<FragmentSongPrBinding, SongMainViewModel>() {
    override val layoutId: Int = R.layout.fragment_song_pr
    override val viewModel: SongMainViewModel by activityViewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            arguments?.parcelableArrayList<SongData>(ExtraCode.SONG_MAIN_ITEM)?.let {
                setPRDataList(it)
            }
        }
    }
}