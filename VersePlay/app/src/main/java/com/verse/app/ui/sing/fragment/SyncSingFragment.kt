package com.verse.app.ui.sing.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentSyncSingBinding
import com.verse.app.extension.recyclerViewNotifyAll
import com.verse.app.ui.sing.viewmodel.SingViewModel
import com.verse.app.utility.DLogger
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 부르기 전  준비 (파트,솔로,듀엣 선택) -> 부르기 -> 싱크
 *
 * Created by jhlee on 2023-04-06
 */
@AndroidEntryPoint
class SyncSingFragment : BaseFragment<FragmentSyncSingBinding, SingViewModel>() {

    override val layoutId: Int = R.layout.fragment_sync_sing
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

            DLogger.d("[Sing]SyncSingFragment")
            with(binding) {


                //superpowered filter 업데이트
                curSoundFilter.observe(viewLifecycleOwner) {
                    llEffectsSync.inFilter.rvSoundFilter.recyclerViewNotifyAll()
                }

                if(curSoundFilter.value > -1){
                    binding.llEffectsSync.inFilter.rvSoundFilter.scrollToPosition(curSoundFilter.value)
                }

                //MR 볼륨
                llEffectsSync.llVolume.sbInst.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        onChangeMRVolume(progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
                //마이크 볼륨
                llEffectsSync.llVolume.sbMic.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        onChangeMikeVolume(progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
                //seekbar playing duration
                llBtnSync.sbBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        if (isEditing) {
                            DLogger.d("setDuration onProgressChanged")
                            onChangePlayingDuration(progress)
                        }
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar) {
                        DLogger.d("setDuration onStartTrackingTouch")
                        isEditing = true
                        pauseSync()
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        DLogger.d("setDuration onStopTrackingTouch")
                        isEditing = false
                        pauseSyncToggle()
                    }
                })
            }
            prepareSync(binding.exo)
        }
    }

    override fun onDestroy() {
        DLogger.d("### PrepareSingFragment onDestroy")
        super.onDestroy()
        callback.remove()
    }
}