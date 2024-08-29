package com.verse.app.ui.sing.fragment

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.bumptech.glide.Glide
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.FragmentSingingVideoBinding
import com.verse.app.extension.recyclerViewNotifyAll
import com.verse.app.permissions.SPermissions
import com.verse.app.ui.adapter.CommonListAdapter
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.sing.viewmodel.SingViewModel
import dagger.hilt.android.AndroidEntryPoint


/**
 * Description : 부르기 전  준비 (파트,솔로,듀엣 선택) -> 부르기
 *
 * Created by jhlee on 2023-04-06
 */
@AndroidEntryPoint
class SingingVideoFragment : BaseFragment<FragmentSingingVideoBinding, SingViewModel>() {

    override val layoutId: Int = R.layout.fragment_singing_video
    override val viewModel: SingViewModel by activityViewModels()
    override val bindingVariable: Int = BR.viewModel
    private lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel?.let {
                    viewModel.checkPlaying()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setVariable(BR.requestManager, Glide.with(this))

        with(viewModel) {

            //가사 rv state
            refreshLyricsView.observe(viewLifecycleOwner) {
                val rv = binding.inSingingLyrics.rvSinging
                when (it.first) {
                    SingViewModel.TJ_RV_STATE.RESIZE -> {
                        if (it.second > 0) {
                            rv.layoutParams.height = it.second
                            rv.recyclerViewNotifyAll()
                        }
                    }

                    SingViewModel.TJ_RV_STATE.REMOVE -> {
                        rv.removeAllViews()
                    }

                    SingViewModel.TJ_RV_STATE.LINE_FINISHED,
                    SingViewModel.TJ_RV_STATE.DRAW -> {
                        val holder = rv.findViewHolderForAdapterPosition(it.second) as CommonListAdapter.LyricsViewHolder
                        holder?.let {
                            holder.binding.lyricsTextView.invalidate()
                        }
                    }

                    SingViewModel.TJ_RV_STATE.NONE -> {}
                }
            }

            //Mr 볼륨 값
            binding.llEffects.llVolume.sbInst.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    onChangeMRVolume(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            //superpowered filter 업데이트
            curSoundFilter.observe(viewLifecycleOwner) {
                binding.llEffects.inFilter.rvSoundFilter.recyclerViewNotifyAll()
            }

            curOrgVideoAndAudioPath.value?.let {
                initPlayer(binding.exoJoin, it.second)
            }
            //부르기 준비
            prepareSong()
        }
    }


    private fun handlePermissions() {

        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        } else {
            listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        }
        SPermissions(this)
            .requestPermissions(*permissions.toTypedArray())
            .build { b, _ ->
                if (b) {
                    //openCamera()
                } else {
                    // 권한 거부 팝업
                    CommonDialog(requireContext())
                        .setContents(resources.getString(R.string.singing_permission_need_all))
                        .setPositiveButton(R.string.str_confirm)
                        .show()
                }
            }
    }

    override fun onDestroyView() {
        binding.inSingingLyrics.rvSinging?.adapter = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        this.callback.remove()
        setFragmentResult(ExtraCode.FRAGMENT_RESULT, bundleOf(ExtraCode.FRAGMENT_RESULT to true))
        super.onDestroy()
    }
}