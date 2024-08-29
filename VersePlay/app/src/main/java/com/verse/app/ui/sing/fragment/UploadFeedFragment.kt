package com.verse.app.ui.sing.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentUploadFeedCompletedBinding
import com.verse.app.ui.sing.viewmodel.SingViewModel
import com.verse.app.utility.DLogger
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 부르기 전  준비 (파트,솔로,듀엣 선택) -> 부르기 -> 싱크 -> 업로드
 *
 * Created by jhlee on 2023-04-06
 */
@AndroidEntryPoint
class UploadFeedFragment : BaseFragment<FragmentUploadFeedCompletedBinding, SingViewModel>() {

    override val layoutId: Int = R.layout.fragment_upload_feed_completed
    override val viewModel: SingViewModel by activityViewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {

        }
    }

    override fun onDestroy() {
        DLogger.d("### PrepareSingFragment onDestroy")
        super.onDestroy()
    }
}