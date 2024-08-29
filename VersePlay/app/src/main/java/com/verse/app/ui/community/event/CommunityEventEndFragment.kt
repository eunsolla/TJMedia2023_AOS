package com.verse.app.ui.community.event

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentCommunityEventEndBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 커뮤니티 > 이벤트 > [종료]
 *
 * Created by juhongmin on 2023/05/17
 */
@AndroidEntryPoint
class CommunityEventEndFragment :
    BaseFragment<FragmentCommunityEventEndBinding, CommunityEventEndFragmentViewModel>() {
    override val layoutId: Int = R.layout.fragment_community_event_end
    override val viewModel: CommunityEventEndFragmentViewModel by viewModels()
    override val bindingVariable: Int = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.start()
    }

    companion object {
        fun newInstance(): Fragment = CommunityEventEndFragment()
    }
}
