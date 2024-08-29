package com.verse.app.ui.sing.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.adapter.BaseChildFragmentPagerAdapter
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.contants.ExtraCode
import com.verse.app.contants.SingType
import com.verse.app.databinding.FragmentPrepareToSingBinding
import com.verse.app.extension.initFragment
import com.verse.app.extension.onMain
import com.verse.app.extension.setChildFragmentResultListener
import com.verse.app.ui.sing.viewmodel.SingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Description : 부르기 전  준비 (파트,솔로,듀엣 선택)
 *
 * Created by jhlee on 2023-04-06
 */
@AndroidEntryPoint
class PrepareSingFragment : BaseFragment<FragmentPrepareToSingBinding, SingViewModel>() {

    override val layoutId: Int = R.layout.fragment_prepare_to_sing
    override val viewModel: SingViewModel by activityViewModels()
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
        binding.setVariable(BR.requestManager, Glide.with(this))
        with(viewModel) {

            currentState.observe(viewLifecycleOwner) { state ->
                if (currentPos.value != prevPos.value) {
                    onSingType(SingType.values()[currentPos.value])
                }
                if (currentPos.value != prevPos.value) {
                    prevPos.value = currentPos.value
                }
            }

            songMainData.value?.let {
                //초기화 진행
                startPrepare()
            }

            setChildFragmentResultListener(ExtraCode.FRAGMENT_RESULT) { requestKey, bundle ->
                if (isInitContents.value) {
                    onMain {
                        launch { onClearSing() }.join()
                    }
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
    }

    /**
     * ViewPager Fragment
     */
    class PartInfoFragmentPagerAdapter(ctx: Fragment, private val viewModel: BaseViewModel?) : BaseChildFragmentPagerAdapter<SingType>(ctx) {
        override fun onCreateFragment(pos: Int) = initFragment<PartInfoFragment> {
            putSerializable(ExtraCode.SING_TYPE, dataList[pos])
        }
    }

    override fun onDestroyView() {
        binding.vpPart?.adapter = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        this.callback.remove()
        super.onDestroy()
    }
}