package com.verse.app.ui.sing.fragment

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseBottomSheetDialogFragment
import com.verse.app.contants.ExtraCode
import com.verse.app.databinding.FragmentSectionBinding
import com.verse.app.ui.sing.viewmodel.SectionViewModel
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Description : 부르기 전  준비 (파트,솔로,듀엣 선택) -> 구간 설정
 *
 * Created by jhlee on 2023-04-06
 */
@AndroidEntryPoint
class SectionFragment : BaseBottomSheetDialogFragment<FragmentSectionBinding, SectionViewModel>() {

    override val layoutId: Int = R.layout.fragment_section
    override val viewModel: SectionViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    private var sectionConfirmListener: Listener? = null

    @Inject
    lateinit var deviceProvider: DeviceProvider

    interface Listener {
        fun onSectionConfirm(sectionIndexInfo: Pair<Int, Int>, sectionTimeInfo: Pair<Int, Int>)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        val behavior = setBottomSheetReHeight(dialog as BottomSheetDialog, getRealContentsHeight())
        behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        behavior?.skipCollapsed = true
        behavior?.isDraggable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {

            startConfirm.observe(viewLifecycleOwner) {

                val sectionIndexInfo = binding.vSection.getSectionIndexInfo()
                val sectionTimeInfo = binding.vSection.getSectionTimeInfo()

                val firstLyricsfirstEvent = binding.vSection.getFirstLyricsFirstEvent()
                val endPlusMs = binding.vSection.getEndPlusMs()
                val endTime = sectionTimeInfo.second

                if ((endTime -endPlusMs) - firstLyricsfirstEvent < binding.vSection.MINIMUM_MS) {
                    Toast.makeText(context, context.resources.getString(R.string.singing_section_valid_minimum_ms), Toast.LENGTH_SHORT).show()
                    return@observe
                }

                sectionConfirmListener?.onSectionConfirm(sectionIndexInfo, sectionTimeInfo)
                dismiss()
            }

            startCloseEvent.observe(viewLifecycleOwner) {
                dismiss()
            }

            lyricsData.observe(viewLifecycleOwner) {
                binding.vSection.setLyricsData(it, curIndexInfo.value ?: Pair(0, 0))
            }

            start()
        }

        lifecycle.addObserver(binding.vSection)
    }

    fun setDto(xtfPath: String, curIndexInfo: Pair<Int, Int>, songInfo: Pair<String?, String?>, bg: String?): SectionFragment {
        if (arguments == null) {
            arguments = Bundle().apply {
                putString(ExtraCode.SECTION_DTO_DATA, xtfPath)
                putSerializable(ExtraCode.SECTION_INDEX_INFO, curIndexInfo)
                putSerializable(ExtraCode.SECTION_SONG_INFO, songInfo)
                putString(ExtraCode.SECTION_SONG_BG, bg)
            }
        } else {
            arguments?.putString(ExtraCode.SECTION_DTO_DATA, xtfPath)
        }
        return this
    }

    fun show(fm: FragmentManager) {
        try {
            if (!isAdded) {
                super.show(fm, "SectionFragment")
            }
        } catch (ex: Exception) {
            DLogger.d("Exception ${ex}")
        }
    }

    fun setListener(listener: Listener): SectionFragment {
        sectionConfirmListener = listener
        return this
    }

    override fun onClick(v: View?) {}


    /**
     * 상태바, 네비게이션바를 제외한 실제 콘텐츠 높이
     */
    private fun getRealContentsHeight(): Int {
        var deviceRealHeight = deviceProvider.getDeviceHeight()
        if (deviceProvider.isNavigationBar()) {
            deviceRealHeight -= deviceProvider.getNavigationBarHeight()
        }
        deviceRealHeight -= deviceProvider.getStatusBarHeight()
        return deviceRealHeight
    }
}